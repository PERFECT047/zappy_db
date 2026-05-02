package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.util.List;

public class LLenCommand extends ListValueCommand implements ICommand {

    public LLenCommand(IListValueStore listValueStore) {
        super(listValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {
        if (args.size() < 2) {
            throw new IllegalArgumentException("LLEN command requires a list name");
        }
        String listName = args.get(1);

        return RespString.getRespIntegerString(listValueStore.getSize(listName));
    }
}
