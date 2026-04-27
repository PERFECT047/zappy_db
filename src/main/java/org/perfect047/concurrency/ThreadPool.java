package org.perfect047.concurrency;

import org.perfect047.handler.ClientHandler;
import org.perfect047.util.SafeEnvParse;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool implements IConcurrencyStrategy {

    private final ExecutorService pool;

    public ThreadPool() {
        int size = resolvePoolSize();

        System.out.println("Thread Pool size: " + size);

        this.pool = Executors.newFixedThreadPool(size);
    }

    @Override
    public void handleConnection(Socket socket) throws Exception {
        pool.submit(new ClientHandler(socket));
    }

    @Override
    public void shutdown() throws Exception {
        pool.shutdown();
    }

    private int resolvePoolSize() {
        int value = SafeEnvParse.getSafeEnvParse(System.getenv("THREAD_POOL_SIZE"), 4, Integer::parseInt);
        return Math.max(4, value);
    }
}
