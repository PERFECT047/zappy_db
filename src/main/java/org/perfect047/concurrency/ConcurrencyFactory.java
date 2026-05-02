package org.perfect047.concurrency;

import org.perfect047.Enum.ConcurrencyStrategy;
import org.perfect047.command.CommandFactory;
import org.perfect047.util.SafeEnvParse;

public class ConcurrencyFactory {

    public IConcurrencyStrategy getConcurrencyStrategy(CommandFactory commandFactory, int port) {

        ConcurrencyStrategy strategy = SafeEnvParse.getSafeEnvParse(
                "CONCURRENCY_STRATEGY",
                ConcurrencyStrategy.THREAD_POOL,
                value -> ConcurrencyStrategy.valueOf(value.toUpperCase())
        );

        int cores = Runtime.getRuntime().availableProcessors();

        int corePoolSize = Math.max(SafeEnvParse.getSafeEnvParse(
                        "THREAD_POOL_CORE_SIZE",
                        cores,
                        Integer::parseInt
                ),
                4);

        int maxPoolSize = SafeEnvParse.getSafeEnvParse(
                "THREAD_POOL_MAX_SIZE",
                cores * 4,
                Integer::parseInt
        );

        int queueSize = SafeEnvParse.getSafeEnvParse(
                "THREAD_POOL_QUEUE_SIZE",
                1000,
                Integer::parseInt
        );

        switch (strategy) {
            case THREAD_POOL:
                return new ThreadPool(commandFactory, port, corePoolSize, maxPoolSize, queueSize);

            case NIO_EVENT_LOOP:
                return new NioEventLoop(commandFactory, port, corePoolSize, maxPoolSize, queueSize);

            default:
                return new NioEventLoop(commandFactory, port, corePoolSize, maxPoolSize, queueSize); // default behaviour
        }
    }
}