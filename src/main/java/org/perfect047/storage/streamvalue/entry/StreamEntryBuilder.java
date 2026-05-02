package org.perfect047.storage.streamvalue.entry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StreamEntryBuilder {

    private StreamEntryBuilder() {
    }

    public static Map<String, String> build(String id, List<String> args) {

        Map<String, String> entry =
                new LinkedHashMap<>(args.size() / 2 + 1);

        entry.put("id", id);

        for (int i = 1; i < args.size(); i += 2) {
            entry.put(args.get(i), args.get(i + 1));
        }

        return entry;
    }
}