package org.perfect047;

import org.perfect047.util.EnvLoader;
import org.perfect047.util.SafeEnvParse;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        LOGGER.info("Starting ZappyDB server...");

        // Load environment variables from .env file (if present) and system env
        EnvLoader.load();

        int port = SafeEnvParse.getSafeEnvParse("PORT", 6379, Integer::parseInt);

        new org.perfect047.Server().startServer(port);
    }
}

