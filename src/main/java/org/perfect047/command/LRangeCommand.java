package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

public class LRangeCommand extends ListValueCommand implements ICommand {

    private static final Logger LOGGER = Logger.getLogger(LRangeCommand.class.getName());

    public LRangeCommand(IListValueStore listValueStore) {
        super(listValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {
        if (args.size() < 4) {
            throw new IllegalArgumentException("LRANGE command requires list name, start index, and end index");
        }

        LOGGER.fine("LRANGE command args: " + args);

        String listName = args.get(1);
        Integer startIndex = Integer.parseInt(args.get(2));
        Integer endIndex = Integer.parseInt(args.get(3));

        return RespString.getRespArrayString(listValueStore.get(listName, startIndex, endIndex));
    }
}
