package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.util.RespString;

import java.util.List;

public class GetCommand extends KeyValueCommand implements ICommand {

    public GetCommand(IKeyValueStore keyValueStore) {
        super(keyValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {
        if (args.size() < 2) {
            throw new IllegalArgumentException("GET command requires a key");
        }
        String value = keyValueStore.get(args.get(1));

        return RespString.getRespBulkString(value == null ? List.of() : List.of(value));
    }
}
