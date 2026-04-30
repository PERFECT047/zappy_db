package org.perfect047.storage.listvalue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ListValueStore implements IListValueStore {

    private static final Logger LOGGER = Logger.getLogger(ListValueStore.class.getName());
    private final ConcurrentMap<String, List<String>> store = new ConcurrentHashMap<>();
    private final LockManager lockManager = new LockManager();
    private final ConditionManager conditionManager = new ConditionManager();

    @Override
    public Integer leftAdd(String listName, List<String> values) {
        ReentrantLock lock = lockManager.getLock(listName);
        Condition condition = conditionManager.getCondition(listName, lock);

        List<String> reversed = new ArrayList<>(values);
        Collections.reverse(reversed);

        lock.lock();

        try{
            List<String> keyStore = store.computeIfAbsent(listName, k -> new ArrayList<>());
            keyStore.addAll(0, reversed);

            condition.signal();
            return keyStore.size();
        }
        finally {
            lock.unlock();
        }

    }

    @Override
    public Integer rightAdd(String listName, List<String> values) {
        ReentrantLock lock = lockManager.getLock(listName);
        Condition condition = conditionManager.getCondition(listName, lock);

        lock.lock();

        try{
            List<String> keyStore = store.computeIfAbsent(listName, k -> new ArrayList<>());
            keyStore.addAll(values);

            condition.signal();
            return keyStore.size();
        }
        finally {
            lock.unlock();
        }

    }

    @Override
    public List<String> leftPop(String listName, Integer repetitions) {
        ReentrantLock lock = lockManager.getAlreadyPresentLock(listName);

        if(lock == null) return null;

        lock.lock();

        try{
            List<String> list = store.get(listName);

            if (list == null || list.isEmpty()) return null;

            int n = Math.min(repetitions, list.size());
            List<String> removed = new ArrayList<>(list.subList(0, n));

            list.subList(0, n).clear();

            return removed;
        }
        finally {
            lock.unlock();
        }

    }

    @Override
    public List<String> blockingLeftPop(String listName, Float seconds) throws InterruptedException {
        ReentrantLock lock = lockManager.getLock(listName);
        Condition condition = conditionManager.getCondition(listName, lock);

        long nanos = seconds > 0 ? (long) (seconds * 1_000_000_000L) : 0;

        lock.lock();

        try{
            while(true){
                List<String> keyStore = store.computeIfAbsent(listName,  k -> new ArrayList<>());

                if(!keyStore.isEmpty()) {
                    String value = keyStore.remove(0);

                    return List.of(value);
                }

                if(seconds == 0){
                    condition.await();
                }
                else{
                    if(nanos <= 0) return null;
                    nanos = condition.awaitNanos(nanos);
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> get(String listName, Integer startIndex, Integer endIndex) {
        ReentrantLock lock = lockManager.getAlreadyPresentLock(listName);

        if(lock == null) return List.of();

        lock.lock();

        try{
            List<String> keyStore = store.get(listName);

            if(keyStore == null || keyStore.isEmpty()) return List.of();

            int iniSize = keyStore.size();

            // Normalize negative indices

            if (startIndex < 0) startIndex += iniSize;
            if (endIndex < 0) endIndex += iniSize;

            // Clamp to bounds

            startIndex = Math.max(0, startIndex);
            endIndex = Math.min(iniSize - 1, endIndex);

            LOGGER.fine("After index normalization - start: " + startIndex + ", end: " + endIndex);

            //Empty Range check

            if(startIndex > endIndex) return List.of();

            LOGGER.fine("Final indices - start: " + startIndex + ", end: " + endIndex);

            return new ArrayList<>(keyStore.subList(startIndex, endIndex + 1));
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Integer getSize(String listName) {
        ReentrantLock lock = lockManager.getAlreadyPresentLock(listName);

        if(lock == null) return null;

        lock.lock();

        try{
            List<String> keyStore = store.get(listName);
            return keyStore == null ? null : keyStore.size();
        }
        finally {
            lock.unlock();
        }
    }
}
