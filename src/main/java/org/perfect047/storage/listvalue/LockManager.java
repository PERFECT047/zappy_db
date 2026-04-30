package org.perfect047.storage.listvalue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages locks for individual keys.
 * Ensures thread-safe access to key data.
 */
public class LockManager {

    private final ConcurrentMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * Gets or creates a lock for the given key.
     * @param key The key to get the lock for
     * @return The ReentrantLock for this key
     */
    public ReentrantLock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }

    /**
     * Checks if a lock exists for the given key.
     * @param key The key to check
     * @return lock if a lock exists, null otherwise
     */
    public ReentrantLock getAlreadyPresentLock(String key) {
        return lockMap.get(key);
    }

    /**
     * Removes the lock for a key (should only be called when key is deleted).
     * @param key The key to remove the lock for
     */
    public void removeLock(String key) {
        lockMap.remove(key);
    }

    /**
     * Clears all locks.
     */
    public void clear() {
        lockMap.clear();
    }
}
