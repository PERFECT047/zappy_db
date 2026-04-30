package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;

import java.io.OutputStream;

public class StoresCommand extends BaseCommand{

    protected final IStreamValueStore streamValueStore;
    protected final IListValueStore listValueStore;
    protected final IKeyValueStore keyValueStore;

    /**
     * @param outputStream stream bound to the current client connection
     * @param streamValueStore store used by stream commands
     * @param keyValueStore store used by map commands
     * @param listValueStore store used by list commands
     */
    protected StoresCommand(OutputStream outputStream, IStreamValueStore streamValueStore, IKeyValueStore keyValueStore, IListValueStore listValueStore) {
        super(outputStream);
        this.streamValueStore = streamValueStore;
        this.keyValueStore = keyValueStore;
        this.listValueStore = listValueStore;
    }

}
