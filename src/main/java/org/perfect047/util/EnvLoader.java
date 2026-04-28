package org.perfect047.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EnvLoader handles loading environment variables from .env files.
 * Follows the pattern: loads .env file, then system env vars override file vars.
 */
public class EnvLoader {

    private static final Logger LOGGER = Logger.getLogger(EnvLoader.class.getName());
    private static final Map<String, String> envCache = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Loads environment variables from .env file in the current working directory.
     * System environment variables take precedence over .env file values.
     */
    public static void load() {
        if (loaded) {
            return;
        }

        Path envPath = Paths.get(".env");

        if (Files.exists(envPath)) {
            try {
                loadEnvFile(envPath);
                LOGGER.info("Loaded environment variables from .env file");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load .env file: " + e.getMessage(), e);
            }
        } else {
            LOGGER.info(".env file not found at: " + envPath.toAbsolutePath());
        }

        loaded = true;
    }

    /**
     * Gets an environment variable, checking system env first, then .env cache.
     * 
     * @param key The environment variable key
     * @return The value, or null if not found
     */
    public static String get(String key) {
        // System env vars take precedence
        String systemValue = System.getenv(key);
        if (systemValue != null) {
            return systemValue;
        }

        // Fall back to .env file values
        return envCache.get(key);
    }

    private static void loadEnvFile(Path envPath) throws IOException {
        String content = Files.readString(envPath, StandardCharsets.UTF_8);

        for (String line : content.split("\n")) {
            line = line.trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Parse KEY=VALUE
            int equalsIndex = line.indexOf('=');
            if (equalsIndex <= 0) {
                LOGGER.warning("Invalid .env line: " + line);
                continue;
            }

            String key = line.substring(0, equalsIndex).trim();
            String value = line.substring(equalsIndex + 1).trim();

            // Remove quotes if present
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            envCache.put(key, value);
            LOGGER.fine("Loaded env var: " + key);
        }
    }
}
