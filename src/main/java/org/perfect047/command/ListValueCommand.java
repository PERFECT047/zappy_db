package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;

import java.io.OutputStream;

/**
 * Base type for commands that operate on list values only.
 */
public abstract class ListValueCommand {

    protected final IListValueStore listValueStore;

    /**
     * @param listValueStore store used by list commands
     */
    protected ListValueCommand(IListValueStore listValueStore) {
        this.listValueStore = listValueStore;
    }
}
