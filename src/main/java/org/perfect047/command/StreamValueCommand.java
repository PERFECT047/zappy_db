package org.perfect047.command;

import org.perfect047.storage.streamvalue.IStreamValueStore;

public class StreamValueCommand {

    protected final IStreamValueStore streamValueStore;

    /**
     * @param streamValueStore store used by stream commands
     */
    protected StreamValueCommand(IStreamValueStore streamValueStore) {
        this.streamValueStore = streamValueStore;
    }

}