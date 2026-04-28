package org.perfect047;

import org.perfect047.command.CommandFactory;
import org.perfect047.concurrency.ConcurrencyFactory;
import org.perfect047.concurrency.IConcurrencyStrategy;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueKeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;

import java.net.ServerSocket;
import java.net.Socket;
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
    public Server() {
        this(new KeyValueKeyValueStore(), new ListValueStore(), new ConcurrencyFactory());
    }

    /**
     * Creates a server with injected stores and concurrency factory.
     *
     * @param keyValueStore key-value store implementation
     * @param listValueStore list-value store implementation
     * @param concurrencyFactory factory that selects the connection handling strategy
     */
    public Server(IKeyValueStore keyValueStore, IListValueStore listValueStore, ConcurrencyFactory concurrencyFactory) {
        this(new CommandFactory(keyValueStore, listValueStore), concurrencyFactory);
    }

    /**
     * Creates a server with an already wired command factory.
     *
     * @param commandFactory command factory used by connection handlers
     * @param concurrencyFactory factory that selects the connection handling strategy
     */
    public Server(CommandFactory commandFactory, ConcurrencyFactory concurrencyFactory) {
        this.concurrencyStrategy = concurrencyFactory.getConcurrencyStrategy(commandFactory);
    }

    /**
     * Creates a server with a fully injected concurrency strategy.
     *
     * @param concurrencyStrategy strategy responsible for handling accepted sockets
     */
    public Server(IConcurrencyStrategy concurrencyStrategy) {
        this.concurrencyStrategy = concurrencyStrategy;
    }

    /**
     * Starts the TCP server and delegates accepted sockets to the configured strategy.
     *
     * @param port TCP port to bind
     */
    public void startServer(int port) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            LOGGER.info("ZappyDB server started on port " + port);

            while (true) {
                clientSocket = serverSocket.accept();
                concurrencyStrategy.handleConnection(clientSocket);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server Exception: " + e.getMessage(), e);
        }
        finally {
            try {
                concurrencyStrategy.shutdown();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Server PostProcess Exception: " + e.getMessage(), e);
            }
        }
    }

}
