package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetCommandTest {

    @Test
    public void testGetCommand() throws Exception {
        String key = "test_key_" + UUID.randomUUID();
        String value = "test_value";
        IKeyValueStore keyValueStore = new KeyValueStore();

        // Set a value first
        keyValueStore.set(key, value, null);

        GetCommand getCommand = new GetCommand(keyValueStore);

        String output = getCommand.execute(List.of("GET", key));

        String expected = RespString.getRespBulkString(List.of(value));
        assertEquals(expected, output);
    }

    @Test
    public void testGetCommandNonExistentKey() throws Exception {
        String key = "non_existent_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();

        GetCommand getCommand = new GetCommand(keyValueStore);

        String output = getCommand.execute(List.of("GET", key));

        String expected = RespString.getRespBulkString(List.of()); // null returns empty list
        assertEquals(expected, output);
    }
}