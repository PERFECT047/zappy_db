package org.perfect047.storage.streamvalue.idgenerator;

import org.perfect047.storage.streamvalue.cursor.IdCursor;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class DefaultStreamIdGenerator implements IStreamIdGenerator {

    @Override
    public IdCursor generate(
            String streamName,
            List<String> args,
            ConcurrentMap<String, Long> lastMsMap,
            ConcurrentMap<String, ConcurrentSkipListMap<Long, Deque<Map<String, String>>>> store) {

        String inputId = args.get(0);

        ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap =
                store.computeIfAbsent(streamName, k -> new ConcurrentSkipListMap<>());

        if ("*".equals(inputId)) {
            return generateAutoId(streamName, lastMsMap, timeMap);
        } else {
            return generateCustomId(streamName, inputId, timeMap, lastMsMap);
        }
    }

    private IdCursor generateAutoId(
            String streamName,
            ConcurrentMap<String, Long> lastMsMap,
            ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap) {

        long newMs = System.currentTimeMillis();
        Long lastMs = lastMsMap.get(streamName);

        if (lastMs != null && newMs < lastMs) {
            newMs = lastMs;
        }

        Deque<Map<String, String>> deque =
                timeMap.computeIfAbsent(newMs, k -> new ConcurrentLinkedDeque<>());

        long newSeq = extractNextSeq(deque);

        return IdCursor.of(newMs, newSeq);
    }

    private IdCursor generateCustomId(
            String streamName,
            String inputId,
            ConcurrentSkipListMap<Long, Deque<Map<String, String>>> timeMap,
            ConcurrentMap<String, Long> lastMsMap) {

        IdCursor parsed = IdCursor.parse(inputId);

        validateCustomId(streamName, parsed, lastMsMap);

        Deque<Map<String, String>> deque =
                timeMap.computeIfAbsent(parsed.ms, k -> new ConcurrentLinkedDeque<>());

        long newSeq = resolveSequence(inputId, parsed, deque);

        return IdCursor.of(parsed.ms, newSeq);
    }

    private void validateCustomId(
            String streamName,
            IdCursor cursor,
            ConcurrentMap<String, Long> lastMsMap) {

        if (cursor.ms == 0 && cursor.seq == 0) {
            throw new RuntimeException("ERR The ID specified in XADD must be greater than 0-0");
        }

        Long lastMs = lastMsMap.get(streamName);
        if (lastMs != null && cursor.ms < lastMs) {
            throw new RuntimeException("ERR The ID specified in XADD is equal or smaller than the target stream top item");
        }
    }

    private long resolveSequence(
            String rawInput,
            IdCursor parsed,
            Deque<Map<String, String>> deque) {

        Map<String, String> lastEntry = deque.peekLast();

        // handle "*"
        if (rawInput.endsWith("-*")) {
            return (lastEntry == null)
                    ? (parsed.ms == 0 ? 1 : 0)
                    : extractNextSeq(deque);
        }

        long newSeq = parsed.seq;

        if (lastEntry != null) {
            long lastSeq = extractLastSeq(lastEntry);
            if (newSeq <= lastSeq) {
                throw new RuntimeException("ERR The ID specified in XADD is equal or smaller than the target stream top item");
            }
        }

        return newSeq;
    }

    private long extractNextSeq(Deque<Map<String, String>> deque) {
        Map<String, String> lastEntry = deque.peekLast();
        return (lastEntry == null) ? 0 : extractLastSeq(lastEntry) + 1;
    }

    private long extractLastSeq(Map<String, String> entry) {
        return IdCursor.parse(entry.get("id")).seq;
    }
}