package org.perfect047.concurrency;

import org.perfect047.command.CommandFactory;
import org.perfect047.handler.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Blocking thread pool based server.
 * Accepts connections and delegates each client to a worker thread.
 */
public class ThreadPool implements IConcurrencyStrategy {

    private static final Logger LOGGER = Logger.getLogger(ThreadPool.class.getName());

    private final ExecutorService pool;
    private final int port;
    private final CommandFactory commandFactory;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private ServerSocket serverSocket;

    /**
     * @param commandFactory factory used to resolve commands
     * @param port server port
     * @param corePoolSize minimum number of threads
     * @param maxPoolSize maximum number of threads
     * @param queueSize task queue capacity
     */
    public ThreadPool(
            CommandFactory commandFactory,
            int port,
            int corePoolSize,
            int maxPoolSize,
            int queueSize
    ) {
        this.commandFactory = commandFactory;
        this.port = port;

        LOGGER.info("ThreadPool config -> core: " + corePoolSize + ", max: " + maxPoolSize);

        this.pool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * Starts the server and begins accepting connections.
     */
    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            LOGGER.info("ThreadPool server running on port " + port);

            while (running.get()) {
                try {
                    Socket client = serverSocket.accept();
                    pool.execute(new ClientHandler(client, commandFactory));
                } catch (Exception e) {
                    if (running.get()) {
                        LOGGER.log(Level.SEVERE, "Error accepting connection", e);
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Server error", ex);
        } finally {
            shutdownExecutor();
        }
    }

    /**
     * Stops accepting new connections and shuts down resources.
     */
    @Override
    public void shutdown() {
        running.set(false);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing server socket", e);
        }

        shutdownExecutor();
    }

    /**
     * Gracefully shuts down executor service.
     */
    private void shutdownExecutor() {
        pool.shutdown();

        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}