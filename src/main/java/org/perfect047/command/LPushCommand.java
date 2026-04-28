package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class LPushCommand extends ListValueCommand implements ICommand{

    public LPushCommand(OutputStream outputStream, IListValueStore listValueStore) {
        super(outputStream, listValueStore);
    }

    @Override
    public void execute(List<String> args) throws Exception {
        if (args.size() < 3) {
            throw new IllegalArgumentException("LPUSH command requires list name and values");
        }
        Integer size = listValueStore.leftAdd(args.get(1), args.subList(2, args.size()));
        getOutputStream().write(RespString.getRespIntegerString(size).getBytes());
    }
}
