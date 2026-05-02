package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LRangeCommandTest {

    @Test
    public void testLRangeCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        // Setup list: [value3, value2, value1] assuming left add
        listValueStore.leftAdd(listName, List.of("value1", "value2", "value3"));

        LRangeCommand lRangeCommand = new LRangeCommand(listValueStore);

        String output = lRangeCommand.execute(List.of("LRANGE", listName, "0", "1"));

        String expected = RespString.getRespArrayString(List.of("value3", "value2"));
        assertEquals(expected, output);
    }
}