package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.StoreFactory;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetCommandTest {

    @Test
    public void testGetCommand() throws Exception {
        String key = "test_key_" + UUID.randomUUID();
        String value = "test_value";

        // Set a value first
        StoreFactory.getKeyValueStore().set(key, value, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GetCommand getCommand = new GetCommand(outputStream);

        getCommand.execute(List.of("GET", key));

        String expected = RespString.getRespBulkString(List.of(value));
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void testGetCommandNonExistentKey() throws Exception {
        String key = "non_existent_" + UUID.randomUUID();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GetCommand getCommand = new GetCommand(outputStream);

        getCommand.execute(List.of("GET", key));

        String expected = RespString.getRespBulkString(List.of()); // null returns empty list
        assertEquals(expected, outputStream.toString());
    }
}