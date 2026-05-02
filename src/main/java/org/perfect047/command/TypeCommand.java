package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.util.RespString;

import java.util.List;

public class TypeCommand extends StoresCommand implements ICommand {

    public TypeCommand(IStreamValueStore streamValueStore, IKeyValueStore keyValueStore, IListValueStore listValueStore) {
        super(streamValueStore, keyValueStore, listValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {

        if (args.size() < 2) {
            throw new IllegalArgumentException("TYPE requires key");
        }

        String key = args.get(1);

        String type = resolveType(key);

        return RespString.getRespSimpleString(List.of(type));
    }

    /**
     * Resolves the type of the key across all stores
     */
    private String resolveType(String key) {

        String keyType = keyValueStore.type(key);
        String listType = listValueStore.type(key);
        String streamType = streamValueStore.type(key);

        return keyType != null ? keyType : listType != null ? listType : streamType != null ? streamType : "none";
    }
}