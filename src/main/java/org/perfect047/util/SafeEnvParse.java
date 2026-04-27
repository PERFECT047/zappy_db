package org.perfect047.util;

import java.util.function.Function;

public class SafeEnvParse {

    public static <T> T getSafeEnvParse(String value, T defaultValue, Function<String, T> parser){

        if(value == null || value.isBlank()) return defaultValue;

        try {
            return parser.apply(value);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }
}
