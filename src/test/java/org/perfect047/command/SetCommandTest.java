package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.StoreFactory;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetCommandTest {

    @Test
    public void testSetCommand() throws Exception {
        String key = "test_key_" + UUID.randomUUID();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SetCommand setCommand = new SetCommand(outputStream);

        setCommand.execute(List.of("SET", key, "value"));

        String expected = RespString.getRespSimpleString(List.of("OK"));
        assertEquals(expected, outputStream.toString());

        // Verify the value was set
        String value = StoreFactory.getKeyValueStore().get(key);
        assertEquals("value", value);
    }
}