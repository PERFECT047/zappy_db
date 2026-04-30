package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.storage.keyvalue.KeyValueStore;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TypeCommand typeCommand = new TypeCommand(outputStream, streamValueStore, keyValueStore, listValueStore);

        typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("string"));
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void testTypeCommandListValue() throws Exception {
        String key = "test_list_key_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        listValueStore.leftAdd(key, List.of("item1", "item2"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TypeCommand typeCommand = new TypeCommand(outputStream, streamValueStore, keyValueStore, listValueStore);

        typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("list"));
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void testTypeCommandStreamValue() throws Exception {
        String key = "test_stream_key_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        streamValueStore.add(key, List.of("0-1", "field1", "value1"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TypeCommand typeCommand = new TypeCommand(outputStream, streamValueStore, keyValueStore, listValueStore);

        typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("stream"));
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void testTypeCommandNonExistentKey() throws Exception {
        String key = "non_existent_" + UUID.randomUUID();
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TypeCommand typeCommand = new TypeCommand(outputStream, streamValueStore, keyValueStore, listValueStore);

        typeCommand.execute(List.of("TYPE", key));

        String expected = RespString.getRespSimpleString(List.of("none"));
        assertEquals(expected, outputStream.toString());
    }

    @Test
    public void testTypeCommandMissingKeyArgument() {
        IKeyValueStore keyValueStore = new KeyValueStore();
        IListValueStore listValueStore = new ListValueStore();
        IStreamValueStore streamValueStore = new StreamValueStore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TypeCommand typeCommand = new TypeCommand(outputStream, streamValueStore, keyValueStore, listValueStore);

        assertThrows(IllegalArgumentException.class, () -> {
            typeCommand.execute(List.of("TYPE"));
        });
    }
}
