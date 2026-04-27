package org.perfect047.storage;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;

public class StoreFactory {

    private static final IKeyValueStore keyValueStore;
    private static final IListValueStore listValueStore;

    static{
        keyValueStore = new KeyValueKeyValueStore();
        listValueStore = new ListValueStore();
    }

    public static IKeyValueStore getKeyValueStore() {
        return keyValueStore;
    }
    public static IListValueStore getListValueStore() {
        return listValueStore;
    }

}
