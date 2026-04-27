package org.perfect047.storage;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueKeyValueStore;

public class StoreFactory {

    private static final IKeyValueStore keyValueStore;

    static{
        keyValueStore = new KeyValueKeyValueStore();
    }

    public static IKeyValueStore getKeyValueStore() {
        return keyValueStore;
    }
}
