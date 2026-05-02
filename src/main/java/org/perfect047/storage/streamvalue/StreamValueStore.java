package org.perfect047.storage.streamvalue;

import org.perfect047.storage.streamvalue.cursor.IdCursor;
import org.perfect047.storage.streamvalue.entry.StreamEntryBuilder;
import org.perfect047.storage.streamvalue.formatter.StreamEntryFormatter;
import org.perfect047.storage.streamvalue.idgenerator.IStreamIdGenerator;
import org.perfect047.storage.streamvalue.idgenerator.StreamIdGeneratorFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;

public class StreamValueStore implements IStreamValueStore {

    private static final Logger LOGGER = Logger.getLogger(StreamValueStore.class.getName());
    private final ConcurrentMap<String, ConcurrentSkipListMap<Long, Deque<Map<String, String>>>> store = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> lastMsMap = new ConcurrentHashMap<>();
    private final LockManager lockManager = new LockManager();
    private final IStreamIdGenerator idGenerator = StreamIdGeneratorFactory.getStreamIdGenerator();


    @Override
    public String add(String streamName, List<String> args) {

        LockEntry lockEntry = lockManager.getLockEntry(streamName);

        lockEntry.lock.lock();
        try {
            ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap =
                    store.computeIfAbsent(streamName,
                            k -> new ConcurrentSkipListMap<>());

            // generator now returns IdCursor
            IdCursor idCursor = idGenerator.generate(
                    streamName,
                    args,
                    lastMsMap,
                    store
            );

            Deque<Map<String, String>> deque =
                    timeMap.computeIfAbsent(idCursor.ms,
                            k -> new ConcurrentLinkedDeque<>());

            Map<String, String> entry =
                    StreamEntryBuilder.build(idCursor.toId(), args);

            deque.addLast(entry);

            lastMsMap.put(streamName, idCursor.ms);

            lockEntry.condition.signalAll();

            return idCursor.toId();

        } finally {
            lockEntry.lock.unlock();
        }
    }

    @Override
    public List<Object> read(String streamName, String startId) {

        ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap =
                store.get(streamName);

        if (timeMap == null || timeMap.isEmpty()) {
            return List.of();
        }

        IdCursor cursor = IdCursor.parse(startId);

        // fast-path
        Map.Entry<Long, Deque<Map<String, String>>> last = timeMap.lastEntry();
        if (last != null) {
            Map<String, String> lastEntry = last.getValue().peekLast();
            if (lastEntry != null) {
                IdCursor lastId = IdCursor.parse(lastEntry.get("id"));

                if (lastId.ms < cursor.ms ||
                        (lastId.ms == cursor.ms && lastId.seq <= cursor.seq)) {
                    return List.of();
                }
            }
        }

        List<Object> result = new ArrayList<>();

        // 1. same timestamp bucket
        Deque<Map<String, String>> sameBucket = timeMap.get(cursor.ms);
        if (sameBucket != null) {
            for (Map<String, String> entry : sameBucket) {
                IdCursor entryId = IdCursor.parse(entry.get("id"));

                if (entryId.seq > cursor.seq) {
                    result.add(StreamEntryFormatter.format(entry));
                }
            }
        }

        // 2. future timestamps
        NavigableMap<Long, Deque<Map<String, String>>> tail =
                timeMap.tailMap(cursor.ms, false);

        for (Deque<Map<String, String>> bucket : tail.values()) {
            for (Map<String, String> entry : bucket) {
                result.add(StreamEntryFormatter.format(entry));
            }
        }

        return result;
    }


    @Override
    public List<Object> range(String streamName, String start, String end) {

        ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap =
                store.get(streamName);

        if (timeMap == null || timeMap.isEmpty()) {
            return List.of();
        }

        IdCursor startCursor = IdCursor.parseStart(start);
        IdCursor endCursor = IdCursor.parseEnd(end);

        NavigableMap<Long, Deque<Map<String, String>>> subMap;

        try {
            subMap = timeMap.subMap(startCursor.ms, true, endCursor.ms, true);
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        List<Object> result = new ArrayList<>();

        for (Map.Entry<Long, Deque<Map<String, String>>> bucket : subMap.entrySet()) {

            long ms = bucket.getKey();

            for (Map<String, String> entry : bucket.getValue()) {

                IdCursor entryId = IdCursor.parse(entry.get("id"));

                boolean afterStart =
                        (ms > startCursor.ms) ||
                                (ms == startCursor.ms && entryId.seq >= startCursor.seq);

                boolean beforeEnd =
                        (ms < endCursor.ms) ||
                                (ms == endCursor.ms && entryId.seq <= endCursor.seq);

                if (!afterStart || !beforeEnd) continue;

                result.add(StreamEntryFormatter.format(entry));
            }
        }

        return result;
    }

    @Override
    public String getLastId(String streamName) {

        ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap =
                store.get(streamName);

        if (timeMap == null || timeMap.isEmpty()) return null;

        Deque<Map<String, String>> deque = timeMap.lastEntry().getValue();
        if (deque == null || deque.isEmpty()) return null;

        Map<String, String> last = deque.peekLast();
        return last != null ? last.get("id") : null;
    }

    @Override
    public List<Object> readBlocking(String streamName, String startId, long timeoutMs) {

        LockEntry lockEntry = lockManager.getLockEntry(streamName);

        long nanos = timeoutMs > 0
                ? java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(timeoutMs)
                : 0;

        lockEntry.lock.lock();
        try {
            while (true) {

                List<Object> result = read(streamName, startId);
                if (result != null && !result.isEmpty()) {
                    return result;
                }

                if (timeoutMs == 0) {
                    lockEntry.condition.await();
                } else {
                    if (nanos <= 0) return null;
                    nanos = lockEntry.condition.awaitNanos(nanos);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            lockEntry.lock.unlock();
        }
    }

    @Override
    public String type(String key) {
        return store.get(key) != null ? "stream" : null;
    }
}