package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.StoreFactory;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BLPopCommandTest {

    @Test
    public void testBLPopCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();

        // Setup list
        StoreFactory.getListValueStore().leftAdd(listName, List.of("value1"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BLPopCommand blPopCommand = new BLPopCommand(outputStream);

        blPopCommand.execute(List.of("BLPOP", listName, "1"));

        String expected = RespString.getRespArrayString(List.of("value1"));
        assertEquals(expected, outputStream.toString());
    }
}