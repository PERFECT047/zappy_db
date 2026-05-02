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

    public static String getRespArrayString(List<?> values) {
        StringBuilder sb = new StringBuilder();

        if (values == null) return "*-1\r\n";

        sb.append("*").append(values.size()).append("\r\n");

        for (Object value : values) {

            if (value instanceof List<?>) {
                sb.append(getRespArrayString((List<?>) value));
            } else if (value instanceof String str) {
                sb.append("$")
                        .append(str.length())
                        .append("\r\n")
                        .append(str)
                        .append("\r\n");
            } else if (value instanceof Integer i) {
                sb.append(":").append(i).append("\r\n");
            } else if (value == null) {
                sb.append("$-1\r\n");
            } else {
                String str = value.toString();
                sb.append("$")
                        .append(str.length())
                        .append("\r\n")
                        .append(str)
                        .append("\r\n");
            }
        }

        return sb.toString();
    }

    public static String getRespErrorString(String err) {
        return "-" + err + "\r\n";
    }

}