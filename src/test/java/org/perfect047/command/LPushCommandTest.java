package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPushCommandTest {

    @Test
    public void testLPushCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LPushCommand lPushCommand = new LPushCommand(outputStream, listValueStore);

        lPushCommand.execute(List.of("LPUSH", listName, "value1", "value2"));

        String expected = RespString.getRespIntegerString(2);
        assertEquals(expected, outputStream.toString());

        // Verify the list
        List<String> list = listValueStore.get(listName, 0, -1);
        assertEquals(List.of("value2", "value1"), list); // left add, so reverse order
    }
}
