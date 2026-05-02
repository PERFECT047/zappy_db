package org.perfect047.command;

import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.util.List;

public class BLPopCommand extends ListValueCommand implements ICommand {

    public BLPopCommand(IListValueStore listValueStore) {
        super(listValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {
        if (args.size() < 2) {
            throw new IllegalArgumentException("BLPOP command requires a list name");
        }
        String listName = args.get(1);
        float seconds = 0f;

        if (args.size() > 2) seconds = Float.parseFloat(args.get(2));

        return RespString.getRespArrayString(listValueStore.blockingLeftPop(listName, seconds));
    }
}