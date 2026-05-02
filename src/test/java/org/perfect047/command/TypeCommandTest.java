package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TypeCommandTest {

    @Test
    public void testTypeCommandKeyValue() throws Exception {
        String key = "test_key_" + UUID.randomUUID();
        String value = "test_value";
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        keyValueStore.set(key, value, null);

        TypeCommand typeCommand = new TypeCommand(streamValueStore, keyValueStore, listValueStore);

        String output = typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("string"));
        assertEquals(expected, output);
    }

    @Test
    public void testTypeCommandListValue() throws Exception {
        String key = "test_list_key_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        listValueStore.leftAdd(key, List.of("item1", "item2"));

        TypeCommand typeCommand = new TypeCommand(streamValueStore, keyValueStore, listValueStore);

        String output = typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("list"));
        assertEquals(expected, output);
    }

    @Test
    public void testTypeCommandStreamValue() throws Exception {
        String key = "test_stream_key_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        streamValueStore.add(key, List.of("0-1", "field1", "value1"));

        TypeCommand typeCommand = new TypeCommand(streamValueStore, keyValueStore, listValueStore);

        String output = typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("stream"));
        assertEquals(expected, output);
    }

    @Test
    public void testTypeCommandNonExistentKey() throws Exception {
        String key = "non_existent_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        TypeCommand typeCommand = new TypeCommand(streamValueStore, keyValueStore, listValueStore);

        String output = typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("none"));
        assertEquals(expected, output);
    }

    @Test
    public void testTypeCommandMissingKeyArgument() {
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        TypeCommand typeCommand = new TypeCommand(streamValueStore, keyValueStore, listValueStore);

        assertThrows(IllegalArgumentException.class, () -> {
            typeCommand.execute(List.of("TYPE"));
        });
    }
}