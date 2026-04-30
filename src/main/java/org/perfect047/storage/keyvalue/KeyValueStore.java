package org.perfect047.storage.keyvalue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory key-value store with support for key expiration.
 * Handles storage operations with thread-safe access using locks and manages expiry.
 */
public class KeyValueStore implements IKeyValueStore {

    private final ConcurrentMap<String, String> store = new ConcurrentHashMap<>();
    private final LockManager lockManager = new LockManager();
    private final ExpiryManager expiryManager = new ExpiryManager();

    @Override
    public boolean set(String key, String value, Long millis) {
        ReentrantReadWriteLock lock = lockManager.getLock(key);
        lock.writeLock().lock();

        try {
            store.put(key, value);
            expiryManager.setExpiry(key, millis);
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String get(String key) {
        ReentrantReadWriteLock lock = lockManager.getLock(key);

        if (lock == null) {
            return null;
        }

        lock.readLock().lock();
        try {
            if(expiryManager.isExpired(key)){
                handleExpiredKey(key);
                return null;
            }

            return store.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Handles cleanup when a key has expired.
     * @param key The expired key to remove
     */
    private void handleExpiredKey(String key) {
        // Need to acquire write lock for cleanup
        ReentrantReadWriteLock lock = lockManager.getLock(key);
        lock.readLock().unlock();
        lock.writeLock().lock();
        
        try {
            store.remove(key);
            expiryManager.removeExpiry(key);
        } finally {
            lock.writeLock().unlock();
            lock.readLock().lock();
        }
    }
}
