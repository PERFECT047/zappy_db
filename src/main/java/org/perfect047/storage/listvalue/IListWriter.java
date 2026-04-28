package org.perfect047.storage.listvalue;

import java.util.List;

/**
 * Interface for list write operations.
 * Segregated responsibility: only handles list modifications (add operations).
 */
public interface IListWriter {
    Integer leftAdd(String listName, List<String> values);
    Integer rightAdd(String listName, List<String> values);
}
