package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;

import java.io.OutputStream;

/**
 * Base type for commands that operate on the key-value store only.
 */
public abstract class KeyValueCommand {

    protected final IKeyValueStore keyValueStore;

    /**
     * @param keyValueStore store used by key-value commands
     */
    protected KeyValueCommand(IKeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }
}
