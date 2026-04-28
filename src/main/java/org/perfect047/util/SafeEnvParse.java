package org.perfect047.util;

import java.util.function.Function;

/**
 * SafeEnvParse safely parses environment variables with fallback defaults.
 * Uses EnvLoader to support both system environment variables and .env files.
 */
public class SafeEnvParse {

    /**
     * Helper used by the key-based method.
     *
     * @param key The value to parse
     * @param defaultValue The default value if parsing fails
     * @param parser Function to parse the string value
     * @return Parsed value or defaultValue
     */
    public static <T> T getSafeEnvParse(String key, T defaultValue, Function<String, T> parser) {
        String value = EnvLoader.get(key);

        if(value == null || value.isBlank()) return defaultValue;

        try {
            return parser.apply(value);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }
}
