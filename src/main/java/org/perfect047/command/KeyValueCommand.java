package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;

import java.io.OutputStream;

/**
 * Base type for commands that operate on the key-value store only.
 */
public abstract class KeyValueCommand extends BaseCommand {

    protected final IKeyValueStore keyValueStore;

    /**
     * @param outputStream stream bound to the current client connection
     * @param keyValueStore store used by key-value commands
     */
    protected KeyValueCommand(OutputStream outputStream, IKeyValueStore keyValueStore) {
        super(outputStream);
        this.keyValueStore = keyValueStore;
    }
}
