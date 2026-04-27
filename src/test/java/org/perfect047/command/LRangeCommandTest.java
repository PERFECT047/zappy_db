package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.StoreFactory;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LRangeCommandTest {

    @Test
    public void testLRangeCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();

        // Setup list: [value3, value2, value1] assuming left add
        StoreFactory.getListValueStore().leftAdd(listName, List.of("value1", "value2", "value3"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LRangeCommand lRangeCommand = new LRangeCommand(outputStream);

        lRangeCommand.execute(List.of("LRANGE", listName, "0", "1"));

        String expected = RespString.getRespArrayString(List.of("value3", "value2"));
        assertEquals(expected, outputStream.toString());
    }
}