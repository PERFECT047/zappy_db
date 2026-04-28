package org.perfect047.concurrency;

import org.perfect047.command.CommandFactory;
import org.perfect047.util.SafeEnvParse;
import org.perfect047.Enum.ConcurrencyStrategy;

//TODO: Event Loop integration
public class ConcurrencyFactory {

    public IConcurrencyStrategy getConcurrencyStrategy(CommandFactory commandFactory) {
        ConcurrencyStrategy strategy = SafeEnvParse.getSafeEnvParse(
                "CONCURRENCY_STRATEGY",
                ConcurrencyStrategy.THREAD_POOL,
                value -> ConcurrencyStrategy.valueOf(value.toUpperCase())
        );

        switch (strategy) {
            case THREAD_POOL:
                return new ThreadPool(commandFactory);

        }

        return new ThreadPool(commandFactory); //default behaviour
    }
}
