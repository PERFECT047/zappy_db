package org.perfect047.storage.keyvalue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages key expiration logic for the key-value store.
 * Responsible for tracking expiry times and checking if keys have expired.
 */
public class ExpiryManager {

    private final ConcurrentMap<String, Long> expiry = new ConcurrentHashMap<>();

    /**
     * Sets the expiry time for a key.
     * @param key The key to set expiry for
     * @param millis Milliseconds from now when the key should expire
     */
    public void setExpiry(String key, Long millis) {
        if (millis != null) {
            expiry.put(key, System.currentTimeMillis() + millis);
            return;
        }

        expiry.remove(key);
    }

    /**
     * Checks if a key has expired.
     * @param key The key to check
     * @return true if the key has expired, false otherwise
     */
    public boolean isExpired(String key) {
        Long expiryTime = expiry.get(key);
        if (expiryTime == null) {
            return false;
        }
        return expiryTime <= System.currentTimeMillis();
    }

    /**
     * Removes the expiry time for a key.
     * @param key The key to remove expiry for
     */
    public void removeExpiry(String key) {
        expiry.remove(key);
    }

    /**
     * Clears all expiry entries.
     */
    public void clear() {
        expiry.clear();
    }
}
