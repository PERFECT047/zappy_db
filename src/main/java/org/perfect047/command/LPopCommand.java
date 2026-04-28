package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class LPopCommand extends ListValueCommand implements ICommand{

    public LPopCommand(OutputStream outputStream, IListValueStore listValueStore) {
        super(outputStream, listValueStore);
    }

    @Override
    public void execute(List<String> args) throws Exception {
        if (args.size() < 2) {
            throw new IllegalArgumentException("LPOP command requires a list name");
        }
        String listName = args.get(1);
        Integer repetitions = 1;

        if (args.size() > 2) {
            repetitions = Integer.parseInt(args.get(2));
        }

        List<String> result = listValueStore.leftPop(listName, repetitions);

        getOutputStream().write(
                ((result == null) || (result.size() <= 1)) ?
                    RespString.getRespBulkString(result).getBytes() :
                    RespString.getRespArrayString(result).getBytes()
        );
    }
}
