package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.StoreFactory;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LLenCommandTest {

    @Test
    public void testLLenCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();

        // Setup list
        StoreFactory.getListValueStore().leftAdd(listName, List.of("value1", "value2"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LLenCommand lLenCommand = new LLenCommand(outputStream);

        lLenCommand.execute(List.of("LLEN", listName));

        String expected = RespString.getRespIntegerString(2);
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void testLLenCommandEmptyList() throws Exception {
        String listName = "empty_list_" + UUID.randomUUID();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LLenCommand lLenCommand = new LLenCommand(outputStream);

        lLenCommand.execute(List.of("LLEN", listName));

        String expected = RespString.getRespIntegerString(0);
        assertEquals(expected, outputStream.toString());
    }
}