package org.perfect047.storage;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;

public class StoreFactory {

    private static final IKeyValueStore keyValueStore;
    private static final IListValueStore listValueStore;
    private static final IStreamValueStore streamValueStore;

    static{
        keyValueStore = new KeyValueStore();
        listValueStore = new ListValueStore();
        streamValueStore = new StreamValueStore();
    }

    public static IKeyValueStore getKeyValueStore() {
        return keyValueStore;
    }
    public static IListValueStore getListValueStore() {
        return listValueStore;
    }
    public  static IStreamValueStore getStreamValueStore() {
        return streamValueStore;
    }

}
