package org.perfect047.storage.streamvalue;

import java.util.List;

/**
 * Interface for non-blocking stream read operations.
 * Segregated responsibility: only handles non-blocking/concurrent read operations.
 */
public interface IStreamReader {
    List<Object> read(String listName, String startId);

    List<Object> range(String listName, String start, String end);

    String getLastId(String listName);
}