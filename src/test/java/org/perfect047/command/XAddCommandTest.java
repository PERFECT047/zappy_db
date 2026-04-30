package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XAddCommandTest {

    @Test
    public void testXAddCommand() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XAddCommand xAddCommand = new XAddCommand(outputStream, streamValueStore);

        List<String> args = List.of("XADD", streamName, "*", "field1", "value1", "field2", "value2");
        xAddCommand.execute(args);

        String response = outputStream.toString();
        assertTrue(response.startsWith("$")); // Bulk string response
        assertTrue(response.contains("-")); // ID should contain a hyphen
        outputStream.reset();

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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XAddCommand xAddCommand = new XAddCommand(outputStream, streamValueStore);

        String specificId = "1-0";
        List<String> args = List.of("XADD", streamName, specificId, "field1", "value1");
        xAddCommand.execute(args);

        String expected = RespString.getRespBulkString(List.of(specificId));
        assertEquals(expected, outputStream.toString());
        outputStream.reset();

        // Verify the entry was added
        List<Object> rangeResult = streamValueStore.range(streamName, "-", "+");
        assertEquals(1, rangeResult.size());
        List<Object> entry = (List<Object>) rangeResult.get(0);
        assertEquals(specificId, entry.get(0));
    }

    @Test
    public void testXAddCommandInvalidArgumentsCount() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XAddCommand xAddCommand = new XAddCommand(outputStream, streamValueStore);

        List<String> args = List.of("XADD", streamName, "*"); // Missing field-value pairs
        xAddCommand.execute(args);

        String expectedError = RespString.getRespErrorString("XADD requires key, id and at least one field-value pair");
        assertEquals(expectedError, outputStream.toString());
    }

    @Test
    public void testXAddCommandOddNumberOfFieldValues() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XAddCommand xAddCommand = new XAddCommand(outputStream, streamValueStore);

        List<String> args = List.of("XADD", streamName, "*", "field1", "value1", "field2"); // Odd number
        xAddCommand.execute(args);

        String expectedError = RespString.getRespErrorString("XADD field-value pairs must be even");
        assertEquals(expectedError, outputStream.toString());
    }

    @Test
    public void testXAddCommandWithZeroId() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XAddCommand xAddCommand = new XAddCommand(outputStream, streamValueStore);

        List<String> args = List.of("XADD", streamName, "0-0", "field1", "value1");
        xAddCommand.execute(args);

        String expected = RespString.getRespErrorString("ERR The ID specified in XADD must be greater than 0-0");
        assertEquals(expected, outputStream.toString());
    }
}
