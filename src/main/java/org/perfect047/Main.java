package org.perfect047;

import org.perfect047.util.EnvLoader;
import org.perfect047.util.SafeEnvParse;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static void main(String[] args) {

        // Disable logging in benchmark mode
        if ("true".equalsIgnoreCase(System.getenv("BENCH_MODE"))) {
            Logger root = Logger.getLogger("");
            root.setLevel(Level.OFF);
            for (Handler h : root.getHandlers()) {
                h.setLevel(Level.OFF);
            }
        }

        LOGGER.info("Starting ZappyDB server...");

        EnvLoader.load();

        int port = SafeEnvParse.getSafeEnvParse("PORT", 6379, Integer::parseInt);

        new Server(port).startServer();
    }
}