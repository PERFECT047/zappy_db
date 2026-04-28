package org.perfect047.storage.listvalue;

import java.util.List;

/**
 * Interface for list read operations.
 * Segregated responsibility: only handles list queries and metadata.
 */
public interface IListReader {
    List<String> leftPop(String listName, Integer repetations);
    List<String> get(String listName, Integer startIndex, Integer endIndex);
    Integer getSize(String listName);
}
