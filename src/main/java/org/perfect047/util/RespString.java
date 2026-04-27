package org.perfect047.util;

import java.util.List;

public class RespString {

    public static String getRespBulkString(List<String> values) {
        StringBuilder sb = new StringBuilder();

        if (values == null || values.isEmpty()) return "$-1\r\n";

        for (String value : values) {
            sb.append("$")
                    .append(value.length())
                    .append("\r\n")
                    .append(value)
                    .append("\r\n");
        }

        return sb.toString();
    }

    public static String getRespSimpleString(List<String> values) {
        StringBuilder sb = new StringBuilder();

        if (values == null || values.isEmpty()) return "$-1\r\n";

        for (String value : values) {
            sb.append("+").append(value).append("\r\n");
        }

        return sb.toString();
    }

    public static String getRespIntegerString(Integer value) {
        StringBuilder sb = new StringBuilder();

        if (value == null) return ":0\r\n";

        sb.append(":").append(value).append("\r\n");

        return sb.toString();
    }

    public static String getRespArrayString(List<String> values) {
        StringBuilder sb = new StringBuilder();

        if (values == null) return "*-1\r\n";

        sb.append("*").append(values.size()).append("\r\n");

        for (String value : values) {
            sb.append("$").append(value.length()).append("\r\n").append(value).append("\r\n");
        }

        return sb.toString();
    }

}