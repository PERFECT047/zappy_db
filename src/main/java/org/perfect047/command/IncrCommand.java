package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.util.RespString;

import java.util.List;

public class IncrCommand extends KeyValueCommand implements ICommand {
    public IncrCommand(IKeyValueStore keyValueStore) {
        super(keyValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {

        if (args.size() < 2) {
            throw new IllegalArgumentException("INCR command requires key");
        }

        String key = args.get(1);

        Integer incrementedValue = keyValueStore.increment(key);

        return RespString.getRespIntegerString(incrementedValue);
    }
}