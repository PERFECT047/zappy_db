package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPopCommandTest {

    @Test
    public void testLPopCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        // Setup list
        listValueStore.leftAdd(listName, List.of("value1", "value2"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LPopCommand lPopCommand = new LPopCommand(outputStream, listValueStore);

        lPopCommand.execute(List.of("LPOP", listName));

        String expected = RespString.getRespBulkString(List.of("value2")); // left pop removes from left
        assertEquals(expected, outputStream.toString());

        // Verify remaining
        List<String> remaining = listValueStore.get(listName, 0, -1);
        assertEquals(List.of("value1"), remaining);
    }

    @Test
    public void testLPopCommandEmptyList() throws Exception {
        String listName = "empty_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LPopCommand lPopCommand = new LPopCommand(outputStream, listValueStore);

        lPopCommand.execute(List.of("LPOP", listName));

        String expected = RespString.getRespBulkString(null); // null returns empty
        assertEquals(expected, outputStream.toString());
    }
}
