package org.perfect047.storage.keyvalue;

public interface IKeyValueStore {

    boolean set(String key, String value, Long millis);

    String get(String key);

}
