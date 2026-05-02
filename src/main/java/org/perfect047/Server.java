package org.perfect047;

import org.perfect047.command.CommandFactory;
import org.perfect047.concurrency.ConcurrencyFactory;
import org.perfect047.concurrency.IConcurrencyStrategy;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point for server-side dependency wiring and socket accept loop.
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private final IConcurrencyStrategy concurrencyStrategy;

    /**
     * Creates the default server with in-memory stores and configured concurrency strategy.
     */
    public Server(int port) {
        this(
                new KeyValueStore(),
                new ListValueStore(),
                new StreamValueStore(),
                new ConcurrencyFactory(),
                port
        );
    }

    /**
     * Creates a server with injected stores and concurrency factory.
     *
     * @param keyValueStore      key-value store implementation
     * @param listValueStore     list-value store implementation
     * @param streamValueStore   stream-value store implementation
     * @param concurrencyFactory factory that selects the connection handling strategy
     * @param port               connection port mapping
     */
    public Server(IKeyValueStore keyValueStore,
                  IListValueStore listValueStore,
                  IStreamValueStore streamValueStore,
                  ConcurrencyFactory concurrencyFactory,
                  int port) {

        CommandFactory commandFactory =
                new CommandFactory(keyValueStore, listValueStore, streamValueStore);

        this.concurrencyStrategy =
                concurrencyFactory.getConcurrencyStrategy(commandFactory, port);
    }

    /**
     * Starts the server.
     */
    public void startServer() {
        try {
            concurrencyStrategy.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server failed to start", e);
            shutdown();
        }
    }

    /**
     * Stops the server.
     */
    public void shutdown() {
        try {
            concurrencyStrategy.shutdown();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Shutdown error", e);
        }
    }
}