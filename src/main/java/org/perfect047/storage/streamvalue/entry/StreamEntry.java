package org.perfect047.storage.streamvalue.entry;

import org.perfect047.storage.streamvalue.cursor.IdCursor;

import java.util.Map;

public final class StreamEntry {

    private final IdCursor id;
    private final Map<String, String> fields;

    public StreamEntry(IdCursor id, Map<String, String> fields) {
        this.id = id;
        this.fields = fields;
    }

    public IdCursor getId() {
        return id;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}