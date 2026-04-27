package org.perfect047.storage.listvalue;

import java.util.List;

public interface IListValueStore {

    Integer leftAdd(String listName, List<String> values);

    Integer rightAdd(String listName, List<String> values);

    List<String> leftPop(String listName, Integer repetations);

    List<String> blockingLeftPop(String listName, Float seconds) throws InterruptedException;

    List<String> get(String listName, Integer startIndex, Integer endIndex);

    Integer getSize(String listName);
}
