package org.perfect047.concurrency;

import org.perfect047.command.CommandFactory;
import org.perfect047.handler.NioClientHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Non-blocking NIO server using a single event loop and worker thread pool.
 * Accepts connections, reads requests, and writes responses asynchronously.
 */
public class NioEventLoop implements IConcurrencyStrategy, Runnable {

    private static final Logger LOGGER = Logger.getLogger(NioEventLoop.class.getName());
    private final CommandFactory commandFactory;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final Thread eventLoopThread;
    private final ThreadPoolExecutor workerPool;
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * @param commandFactory factory used to resolve commands
     * @param port           server port
     * @param corePoolSize   minimum worker threads
     * @param maxPoolSize    maximum worker threads
     * @param queueSize      worker queue capacity
     */
    public NioEventLoop(CommandFactory commandFactory,
                        int port,
                        int corePoolSize,
                        int maxPoolSize,
                        int queueSize) {

        this.commandFactory = commandFactory;

        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new java.net.InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            workerPool = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(queueSize),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

            eventLoopThread = new Thread(this, "nio-event-loop");

            LOGGER.info("NIO server running on port " + port);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to initialize NIO server", ex);
        }
    }

    /**
     * Event loop: handles accept, read, and write events.
     */
    @Override
    public void run() {
        while (running.get()) {
            try {
                selector.select();

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            handleAccept();
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Connection error", e);
                        closeKey(key);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Selector error", e);
            }
        }

        cleanup();
    }

    /**
     * Accepts a new client connection.
     */
    private void handleAccept() throws IOException {
        SocketChannel client = serverChannel.accept();
        if (client == null) return;

        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ,
                new ConnectionContext(client, commandFactory));
    }

    /**
     * Reads data and dispatches processing to worker pool.
     */
    private void handleRead(SelectionKey key) throws IOException {
        ConnectionContext ctx = (ConnectionContext) key.attachment();
        SocketChannel ch = ctx.channel;

        int read = ch.read(ctx.readBuffer);

        if (read == -1) {
            closeKey(key);
            return;
        }

        if (read == 0) return;

        ctx.readBuffer.flip();

        ByteBuffer copy = ctx.readBuffer.slice();

        ctx.readBuffer.clear();

        workerPool.execute(() -> {
            try {
                var responses = ctx.handler.process(copy);

                if (!responses.isEmpty()) {
                    for (byte[] resp : responses) {
                        ctx.writeQueue.add(ByteBuffer.wrap(resp));
                    }

                    // 🔥 Only wake selector if WRITE not already set
                    if ((key.interestOps() & SelectionKey.OP_WRITE) == 0) {
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        selector.wakeup();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Worker error", e);
            }
        });
    }

    /**
     * Writes queued responses to client.
     */
    private void handleWrite(SelectionKey key) throws IOException {
        ConnectionContext ctx = (ConnectionContext) key.attachment();
        SocketChannel ch = ctx.channel;

        Queue<ByteBuffer> queue = ctx.writeQueue;

        while (!queue.isEmpty()) {
            ByteBuffer buf = queue.peek();
            ch.write(buf);

            if (buf.hasRemaining()) return;

            queue.poll();
        }

        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    /**
     * Closes a connection.
     */
    private void closeKey(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException ignored) {
        }
        key.cancel();
    }

    /**
     * Starts the event loop thread.
     */
    @Override
    public void start() {
        eventLoopThread.start();
    }

    /**
     * Stops the server and releases resources.
     */
    @Override
    public void shutdown() {
        running.set(false);
        selector.wakeup();
        workerPool.shutdown();

        try {
            eventLoopThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Cleans up selector and channels.
     */
    private void cleanup() {
        try {
            for (SelectionKey key : selector.keys()) {
                closeKey(key);
            }
            selector.close();
            serverChannel.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cleanup error", e);
        }
    }

    static class ConnectionContext {
        final SocketChannel channel;
        final ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
        final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
        final NioClientHandler handler;

        ConnectionContext(SocketChannel ch, CommandFactory factory) {
            this.channel = ch;
            this.handler = new NioClientHandler(factory);
        }
    }
}