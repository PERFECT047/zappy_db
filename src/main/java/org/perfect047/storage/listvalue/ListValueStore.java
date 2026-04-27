package org.perfect047.storage.listvalue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ListValueStore implements IListValueStore {

    private final ConcurrentMap<String, List<String>> store = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Condition> conditions = new ConcurrentHashMap<>();

    @Override
    public Integer leftAdd(String listName, List<String> values) {
        ReentrantLock lock = locks.computeIfAbsent(listName, k -> new ReentrantLock());
        Condition condition = conditions.computeIfAbsent(listName, k -> lock.newCondition());

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
        ReentrantLock lock = locks.computeIfAbsent(listName, k -> new ReentrantLock());
        Condition condition = conditions.computeIfAbsent(listName, k -> lock.newCondition());

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
    public List<String> leftPop(String listName, Integer repetations) {
        ReentrantLock lock = locks.get(listName);

        if(lock == null) return null;

        lock.lock();

        try{
            List<String> list = store.get(listName);

            if (list == null || list.isEmpty()) return null;

            int n = Math.min(repetations, list.size());
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
        ReentrantLock lock = locks.computeIfAbsent(listName, k -> new ReentrantLock());
        Condition condition = conditions.computeIfAbsent(listName, k -> lock.newCondition());

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
        if(!store.containsKey(listName)) return List.of();

        List<String> keyStore = store.get(listName);

        if(keyStore.isEmpty()) return List.of();

        int iniSize = keyStore.size();

        // Normalize negative indices

        if (startIndex < 0) startIndex += iniSize;
        if (endIndex < 0) endIndex += iniSize;

        // Clamp to bounds

        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(iniSize - 1, endIndex);

        System.out.println("====== After Normalisation =======");
        System.out.print("Start index: " + startIndex);
        System.out.println(", end index: " + endIndex);

        //Empty Range check

        if(startIndex > endIndex) return List.of();

        System.out.print("Start index: " + startIndex);
        System.out.println(", end index: " + endIndex);

        return new ArrayList<>(keyStore.subList(startIndex, endIndex + 1));
    }

    @Override
    public Integer getSize(String listName) {
        if(!store.containsKey(listName)) return null;

        return store.get(listName).size();
    }
}
