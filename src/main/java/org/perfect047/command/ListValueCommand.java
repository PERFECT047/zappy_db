package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;

import java.io.OutputStream;

/**
 * Base type for commands that operate on list values only.
 */
public abstract class ListValueCommand extends BaseCommand {

    protected final IListValueStore listValueStore;

    /**
     * @param outputStream stream bound to the current client connection
     * @param listValueStore store used by list commands
     */
    protected ListValueCommand(OutputStream outputStream, IListValueStore listValueStore) {
        super(outputStream);
        this.listValueStore = listValueStore;
    }
}
