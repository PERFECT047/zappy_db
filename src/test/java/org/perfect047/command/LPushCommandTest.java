package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.StoreFactory;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPushCommandTest {

    @Test
    public void testLPushCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LPushCommand lPushCommand = new LPushCommand(outputStream);

        lPushCommand.execute(List.of("LPUSH", listName, "value1", "value2"));

        String expected = RespString.getRespIntegerString(2);
        assertEquals(expected, outputStream.toString());

        // Verify the list
        List<String> list = StoreFactory.getListValueStore().get(listName, 0, -1);
        assertEquals(List.of("value2", "value1"), list); // left add, so reverse order
    }
}