package org.perfect047.storage.listvalue;

public class ConditionManager {

    private final java.util.concurrent.ConcurrentMap<String, java.util.concurrent.locks.Condition> conditions = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Gets or creates a condition for the given key using the provided lock.
     * @param key The key to get the condition for
     * @param lock The lock to associate the condition with
     * @return The Condition for this key
     */
    public java.util.concurrent.locks.Condition getCondition(String key, java.util.concurrent.locks.ReentrantLock lock) {
        return conditions.computeIfAbsent(key, k -> lock.newCondition());
    }

    /**
     * Removes the condition for a key.
     * @param key The key to remove the condition for
     */
    public void removeCondition(String key) {
        conditions.remove(key);
    }

    /**
     * Clears all conditions.
     */
    public void clear() {
        conditions.clear();
    }

}
