package org.perfect047.storage.listvalue;

import java.util.List;

/**
 * Interface for blocking list read operations.
 * Segregated responsibility: only handles blocking/concurrent read operations.
 */
public interface IBlockingListReader {
    List<String> blockingLeftPop(String listName, Float seconds) throws InterruptedException;
}
