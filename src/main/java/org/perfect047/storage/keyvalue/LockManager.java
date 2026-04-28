package org.perfect047.storage.keyvalue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages read-write locks for individual keys.
 * Ensures thread-safe access to key data.
 */
public class LockManager {

    private final ConcurrentMap<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    /**
     * Gets or creates a read-write lock for the given key.
     * @param key The key to get the lock for
     * @return The ReentrantReadWriteLock for this key
     */
    public ReentrantReadWriteLock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
    }

    /**
     * Checks if a lock exists for the given key.
     * @param key The key to check
     * @return true if a lock exists, false otherwise
     */
    public boolean hasLock(String key) {
        return lockMap.containsKey(key);
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
