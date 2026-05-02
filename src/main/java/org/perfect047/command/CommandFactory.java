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
        commandRegistry.put("SET", new SetCommand(keyValueStore));
        commandRegistry.put("GET", new GetCommand(keyValueStore));
        commandRegistry.put("LPUSH", new LPushCommand(listValueStore));
        commandRegistry.put("RPUSH", new RPushCommand(listValueStore));
        commandRegistry.put("LRANGE", new LRangeCommand(listValueStore));
        commandRegistry.put("LLEN", new LLenCommand(listValueStore));
        commandRegistry.put("LPOP", new LPopCommand(listValueStore));
        commandRegistry.put("BLPOP", new BLPopCommand(listValueStore));
        commandRegistry.put("XADD", new XAddCommand(streamValueStore));
        commandRegistry.put("XRANGE", new XRangeCommand(streamValueStore));
        commandRegistry.put("XREAD", new XReadCommand(streamValueStore));
        commandRegistry.put("TYPE", new TypeCommand(streamValueStore, keyValueStore, listValueStore));
    }

    public ICommand getCommand(String commandName) {
        Function<OutputStream, ICommand> creator = commandRegistry.get(commandName.toUpperCase());
    }
}
