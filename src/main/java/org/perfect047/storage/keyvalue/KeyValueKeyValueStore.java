package org.perfect047.storage.keyvalue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO: Active expired keys cleanup
public class KeyValueKeyValueStore implements IKeyValueStore {

    private final ConcurrentMap<String,String> store = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantReadWriteLock> lockManager = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> expiry = new ConcurrentHashMap<>();

    @Override
    public boolean set(String key, String value, Long millis) {
        lockManager.putIfAbsent(key, new ReentrantReadWriteLock());
        lockManager.get(key).writeLock().lock();

        try {
            store.put(key,value);

            if(millis != null) expiry.put(key,System.currentTimeMillis() + millis);

        }
        finally {
            lockManager.get(key).writeLock().unlock();
        }

        return true;
    }

    @Override
    public String get(String key) {
        ReentrantReadWriteLock lock = lockManager.get(key);

        if (lock == null) {
            return null;
        }

        lock.readLock().lock();
        try {

            if(expiry.get(key) != null && expiry.get(key) <= System.currentTimeMillis() ){
                store.remove(key);
                expiry.remove(key);

                return null;
            }

            return store.get(key);
        } finally {
            lock.readLock().unlock();
        }

    }
}
