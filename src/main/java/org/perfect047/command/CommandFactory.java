package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates command instances for a client connection while keeping store wiring centralized.
 */
public class CommandFactory {

    private final IKeyValueStore keyValueStore;
    private final IListValueStore listValueStore;
    private final IStreamValueStore streamValueStore;
    private final Map<String, Function<OutputStream, ICommand>> commandRegistry = new HashMap<>();

    /**
     * @param keyValueStore backing store for key-value commands
     * @param listValueStore backing store for list commands
     */
    public CommandFactory(IKeyValueStore keyValueStore, IListValueStore listValueStore, IStreamValueStore streamValueStore) {
        this.keyValueStore = keyValueStore;
        this.listValueStore = listValueStore;
        this.streamValueStore = streamValueStore;
        registerCommands();
    }

    private void registerCommands() {
        commandRegistry.put("PING", PingCommand::new);
        commandRegistry.put("ECHO", EchoCommand::new);
        commandRegistry.put("SET", os -> new SetCommand(os, keyValueStore));
        commandRegistry.put("GET", os -> new GetCommand(os, keyValueStore));
        commandRegistry.put("LPUSH", os -> new LPushCommand(os, listValueStore));
        commandRegistry.put("RPUSH", os -> new RPushCommand(os, listValueStore));
        commandRegistry.put("LRANGE", os -> new LRangeCommand(os, listValueStore));
        commandRegistry.put("LLEN", os -> new LLenCommand(os, listValueStore));
        commandRegistry.put("LPOP", os -> new LPopCommand(os, listValueStore));
        commandRegistry.put("BLPOP", os -> new BLPopCommand(os, listValueStore));
        commandRegistry.put("XADD", os -> new XAddCommand(os, streamValueStore));
        commandRegistry.put("XRANGE", os -> new XRangeCommand(os, streamValueStore));
        commandRegistry.put("XREAD", os -> new XReadCommand(os, streamValueStore));
        commandRegistry.put("TYPE", os -> new TypeCommand(os, streamValueStore, keyValueStore, listValueStore));
    }

    public ICommand getCommand(String commandName, OutputStream outputStream) {
        Function<OutputStream, ICommand> creator = commandRegistry.get(commandName.toUpperCase());
        return creator != null ? creator.apply(outputStream) : null;
    }
}
