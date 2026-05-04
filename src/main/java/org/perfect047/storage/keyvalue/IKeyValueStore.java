package org.perfect047.storage.keyvalue;

import org.perfect047.storage.ITypeStore;

public interface IKeyValueStore extends ITypeStore {

    boolean set(String key, String value, Long millis);

    String get(String key);

    Integer increment(String key);

}