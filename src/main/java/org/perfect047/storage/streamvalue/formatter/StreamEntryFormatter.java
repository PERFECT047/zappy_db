package org.perfect047.storage.streamvalue.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class StreamEntryFormatter {

    private StreamEntryFormatter() {
    }

    public static List<Object> format(Map<String, String> entry) {

        String id = entry.get("id");

        List<String> kv = new ArrayList<>(entry.size() * 2);

        for (Map.Entry<String, String> kvEntry : entry.entrySet()) {
            if (!kvEntry.getKey().equals("id")) {
                kv.add(kvEntry.getKey());
                kv.add(kvEntry.getValue());
            }
        }

        return List.of(id, kv);
    }
}