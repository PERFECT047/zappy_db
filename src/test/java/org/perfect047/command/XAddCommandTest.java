package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XAddCommandTest {

    @Test
    public void testXAddCommand() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XAddCommand xAddCommand = new XAddCommand(streamValueStore);

        List<String> args = List.of("XADD", streamName, "*", "field1", "value1", "field2", "value2");
        String output = xAddCommand.execute(args);

        assertTrue(output.startsWith("$")); // Bulk string response
        assertTrue(output.contains("-")); // ID should contain a hyphen

        // Verify the entry was added
        List<Object> rangeResult = streamValueStore.range(streamName, "-", "+");
        assertEquals(1, rangeResult.size());
        List<Object> entry = (List<Object>) rangeResult.get(0);
        assertEquals(2, entry.size()); // ID and fields
        List<String> fields = (List<String>) entry.get(1);
        assertEquals(4, fields.size()); // field1, value1, field2, value2
        assertEquals("field1", fields.get(0));
        assertEquals("value1", fields.get(1));
        assertEquals("field2", fields.get(2));
        assertEquals("value2", fields.get(3));
    }

    @Test
    public void testXAddCommandWithSpecificId() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XAddCommand xAddCommand = new XAddCommand(streamValueStore);

        String specificId = "1-0";
        List<String> args = List.of("XADD", streamName, specificId, "field1", "value1");
        String output = xAddCommand.execute(args);

        String expected = RespString.getRespBulkString(List.of(specificId));
        assertEquals(expected, output);

        // Verify the entry was added
        List<Object> rangeResult = streamValueStore.range(streamName, "-", "+");
        assertEquals(1, rangeResult.size());
        List<Object> entry = (List<Object>) rangeResult.get(0);
        assertEquals(specificId, entry.get(0));
    }

    @Test
    public void testXAddCommandInvalidArgumentsCount() {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XAddCommand xAddCommand = new XAddCommand(streamValueStore);

        List<String> args = List.of("XADD", streamName, "*");

        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> xAddCommand.execute(args)
        );

        assertEquals(
                "XADD requires key, id and at least one field-value pair",
                ex.getMessage()
        );
    }

    @Test
    public void testXAddCommandOddNumberOfFieldValues() {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XAddCommand xAddCommand = new XAddCommand(streamValueStore);

        List<String> args = List.of("XADD", streamName, "*", "field1", "value1", "field2");

        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> xAddCommand.execute(args)
        );

        assertEquals(
                "XADD field-value pairs must be even",
                ex.getMessage()
        );
    }

    @Test
    public void testXAddCommandWithZeroId() {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XAddCommand xAddCommand = new XAddCommand(streamValueStore);

        List<String> args = List.of("XADD", streamName, "0-0", "field1", "value1");

        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> xAddCommand.execute(args)
        );

        assertEquals(
                "ERR The ID specified in XADD must be greater than 0-0",
                ex.getMessage()
        );
    }
}