package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LLenCommandTest {

    @Test
    public void testLLenCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        // Setup list
        listValueStore.leftAdd(listName, List.of("value1", "value2"));

        LLenCommand lLenCommand = new LLenCommand(listValueStore);

        String output = lLenCommand.execute(List.of("LLEN", listName));

        String expected = RespString.getRespIntegerString(2);
        assertEquals(expected, output);
    }

    @Test
    public void testLLenCommandEmptyList() throws Exception {
        String listName = "empty_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        LLenCommand lLenCommand = new LLenCommand(listValueStore);

        String output = lLenCommand.execute(List.of("LLEN", listName));

        String expected = RespString.getRespIntegerString(0);
        assertEquals(expected, output);
    }
}