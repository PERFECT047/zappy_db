package org.perfect047.storage.streamvalue;

import java.util.List;

/**
 * Interface for blocking stream read operations.
 * Segregated responsibility: only handles blocking/concurrent read operations.
 */
public interface IBlockingStreamReader {
    List<Object> readBlocking(String listName, String startId, long timeoutMs);
}
