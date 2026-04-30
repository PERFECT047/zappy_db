package org.perfect047.command;

import org.perfect047.storage.streamvalue.IStreamValueStore;

import java.io.OutputStream;

public class StreamValueCommand extends BaseCommand{

    protected final IStreamValueStore streamValueStore;

    /**
     * @param outputStream stream bound to the current client connection
     * @param streamValueStore store used by stream commands
     */
    protected StreamValueCommand(OutputStream outputStream, IStreamValueStore streamValueStore) {
        super(outputStream);
        this.streamValueStore = streamValueStore;
    }

}
