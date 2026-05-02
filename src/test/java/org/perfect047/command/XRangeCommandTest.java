package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XRangeCommandTest {

    @Test
    public void testXRangeCommandBasic() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XRangeCommand xRangeCommand = new XRangeCommand(streamValueStore);

        // Add some entries
        streamValueStore.add(streamName, List.of("1-1", "field1", "value1"));
        streamValueStore.add(streamName, List.of("1-2", "field2", "value2"));
        streamValueStore.add(streamName, List.of("2-1", "field3", "value3"));

        List<String> args = List.of("XRANGE", streamName, "1-1", "2-1");
        String output = xRangeCommand.execute(args);

        List<Object> expectedResult = List.of(
                List.of("1-1", List.of("field1", "value1")),
                List.of("1-2", List.of("field2", "value2")),
                List.of("2-1", List.of("field3", "value3"))
        );
        String expected = RespString.getRespArrayString(expectedResult);
        assertEquals(expected, output);
    }

    @Test
    public void testXRangeCommandWithMinMaxIds() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XRangeCommand xRangeCommand = new XRangeCommand(streamValueStore);

        // Add some entries
        streamValueStore.add(streamName, List.of("1-1", "field1", "value1"));
        streamValueStore.add(streamName, List.of("1-2", "field2", "value2"));
        streamValueStore.add(streamName, List.of("2-1", "field3", "value3"));

        List<String> args = List.of("XRANGE", streamName, "-", "+");
        String output = xRangeCommand.execute(args);

        List<Object> expectedResult = List.of(
                List.of("1-1", List.of("field1", "value1")),
                List.of("1-2", List.of("field2", "value2")),
                List.of("2-1", List.of("field3", "value3"))
        );
        String expected = RespString.getRespArrayString(expectedResult);
        assertEquals(expected, output);
    }

    @Test
    public void testXRangeCommandNonExistentStream() throws Exception {
        String streamName = "non_existent_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XRangeCommand xRangeCommand = new XRangeCommand(streamValueStore);

        List<String> args = List.of("XRANGE", streamName, "-", "+");
        String output = xRangeCommand.execute(args);

        String expected = RespString.getRespArrayString(List.of());
        assertEquals(expected, output);
    }

    @Test
    public void testXRangeCommandEmptyStream() throws Exception {
        String streamName = "empty_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XRangeCommand xRangeCommand = new XRangeCommand(streamValueStore);

        List<String> args = List.of("XRANGE", streamName, "-", "+");
        String output = xRangeCommand.execute(args);

        String expected = RespString.getRespArrayString(List.of());
        assertEquals(expected, output);
    }

    @Test
    public void testXRangeCommandInvalidRange() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XRangeCommand xRangeCommand = new XRangeCommand(streamValueStore);

        streamValueStore.add(streamName, List.of("10-1", "field1", "value1"));
        streamValueStore.add(streamName, List.of("20-1", "field2", "value2"));

        List<String> args = List.of("XRANGE", streamName, "30-1", "1-1"); // End before start
        String output = xRangeCommand.execute(args);

        String expected = RespString.getRespArrayString(List.of());
        assertEquals(expected, output);
    }

    @Test
    public void testXRangeCommandMissingArguments() {
        IStreamValueStore streamValueStore = new StreamValueStore();
        XRangeCommand xRangeCommand = new XRangeCommand(streamValueStore);

        assertThrows(IllegalArgumentException.class, () -> {
            xRangeCommand.execute(List.of("XRANGE", "stream1", "1-1")); // Missing end
        });

        assertThrows(IllegalArgumentException.class, () -> {
            xRangeCommand.execute(List.of("XRANGE", "stream1")); // Missing start and end
        });
    }
}