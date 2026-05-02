package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BLPopCommandTest {

    @Test
    public void testBLPopCommand() throws Exception {
        String listName = "test_list_" + UUID.randomUUID();
        IListValueStore listValueStore = new ListValueStore();

        // Setup list
        listValueStore.leftAdd(listName, List.of("value1"));

        BLPopCommand blPopCommand = new BLPopCommand(listValueStore);

        String output = blPopCommand.execute(List.of("BLPOP", listName, "1"));

        String expected = RespString.getRespArrayString(List.of("value1"));
        assertEquals(expected, output);
    }
}