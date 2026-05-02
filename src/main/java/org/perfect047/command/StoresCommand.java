package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;

public class StoresCommand {

    protected final IStreamValueStore streamValueStore;
    protected final IListValueStore listValueStore;
    protected final IKeyValueStore keyValueStore;

    /**
     * @param streamValueStore store used by stream commands
     * @param keyValueStore    store used by map commands
     * @param listValueStore   store used by list commands
     */
    protected StoresCommand(IStreamValueStore streamValueStore, IKeyValueStore keyValueStore, IListValueStore listValueStore) {
        this.streamValueStore = streamValueStore;
        this.keyValueStore = keyValueStore;
        this.listValueStore = listValueStore;
    }

}