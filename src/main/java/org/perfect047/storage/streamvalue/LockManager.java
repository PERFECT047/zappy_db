package org.perfect047.storage.streamvalue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages locks for individual keys.
 * Ensures thread-safe access to key data.
 */
public class LockManager {

    private final ConcurrentMap<String, LockEntry> lockMap = new ConcurrentHashMap<>();

    /**
     * Gets or creates a lock for the given key.
     * @param key The key to get the lock for
     * @return The LockEntry for this key
     */
    public LockEntry getLockEntry(String key) {
        return lockMap.computeIfAbsent(key, k -> new LockEntry());
    }

    /**
     * Removes the lock for a key (should only be called when key is deleted).
     * @param key The key to remove the lock for
     */
    public void remove(String key) {
        lockMap.remove(key);
    }

    /**
     * Clears all locks.
     */
    public void clear() {
        lockMap.clear();
    }
}

