package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates command instances for a client connection while keeping store wiring centralized.
 */
public class CommandFactory {

    private final IKeyValueStore keyValueStore;
    private final IListValueStore listValueStore;
    private final IStreamValueStore streamValueStore;
    private final Map<String, ICommand> commandRegistry = new HashMap<>();

    /**
     * @param keyValueStore    backing store for key-value commands
     * @param listValueStore   backing store for list commands
     * @param streamValueStore backing store for stream commands
     */
    public CommandFactory(
            IKeyValueStore keyValueStore,
            IListValueStore listValueStore,
            IStreamValueStore streamValueStore
    ) {
        this.keyValueStore = keyValueStore;
        this.listValueStore = listValueStore;
        this.streamValueStore = streamValueStore;
        registerCommands();
    }

    private void registerCommands() {
        commandRegistry.put("PING", new PingCommand());
        commandRegistry.put("ECHO", new EchoCommand());
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
        commandRegistry.put("INCR", new IncrCommand(keyValueStore));
    }

    public ICommand getCommand(String commandName) {
        ICommand command = commandRegistry.get(commandName.toUpperCase());

        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }

        return command;
    }
}