package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.storage.streamvalue.StreamValueStore;
import org.perfect047.util.RespString;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XReadCommandTest {

    @Test
    public void testXReadCommandBasic() throws Exception {
        String streamName1 = "test_stream_1_" + UUID.randomUUID();
        String streamName2 = "test_stream_2_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XReadCommand xReadCommand = new XReadCommand(streamValueStore);

        // Add entries to stream1
        streamValueStore.add(streamName1, List.of("1-1", "field1", "value1"));
        streamValueStore.add(streamName1, List.of("1-2", "field2", "value2"));

        // Add entries to stream2
        streamValueStore.add(streamName2, List.of("2-1", "fieldA", "valueA"));

        List<String> args = List.of("XREAD", "STREAMS", streamName1, streamName2, "0-0", "0-0");
        String output = xReadCommand.execute(args);

        List<Object> expectedResult = List.of(
                List.of(
                        streamName1,
                        List.of(
                                List.of("1-1", List.of("field1", "value1")),
                                List.of("1-2", List.of("field2", "value2"))
                        )
                ),
                List.of(
                        streamName2,
                        List.of(
                                List.of("2-1", List.of("fieldA", "valueA"))
                        )
                )
        );
        String expected = RespString.getRespArrayString(expectedResult);
        assertEquals(expected, output);
    }

    @Test
    public void testXReadCommandWithDollarId() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XReadCommand xReadCommand = new XReadCommand(streamValueStore);

        // Add some entries
        streamValueStore.add(streamName, List.of("1-1", "field1", "value1"));
        String lastId = streamValueStore.add(streamName, List.of("*", "field2", "value2"));

        List<String> args = List.of("XREAD", "STREAMS", streamName, "$");
        String output = xReadCommand.execute(args);

        List<Object> expectedResult = List.of(
                List.of(
                        streamName,
                        List.of(
                                List.of(lastId, List.of("field2", "value2"))
                        )
                )
        );
        String expected = RespString.getRespArrayString(expectedResult);
        assertEquals(expected, output);
    }

    @Test
    public void testXReadCommandWithBlock() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XReadCommand xReadCommand = new XReadCommand(streamValueStore);

        // Add an entry after a short delay to simulate blocking
        final String[] generatedId = new String[1];

        new Thread(() -> {
            try {
                Thread.sleep(100);
                generatedId[0] = streamValueStore.add(streamName, List.of("*", "field1", "value1"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        List<String> args = List.of("XREAD", "BLOCK", "500", "STREAMS", streamName, "$");
        String output = xReadCommand.execute(args);

        List<Object> expectedResult = List.of(
                List.of(
                        streamName,
                        List.of(
                                List.of(generatedId[0], List.of("field1", "value1"))
                        )
                )
        );

        String expected = RespString.getRespArrayString(expectedResult);
        assertEquals(expected, output);
    }

    @Test
    public void testXReadCommandWithBlockTimeout() throws Exception {
        String streamName = "test_stream_" + UUID.randomUUID();
        IStreamValueStore streamValueStore = new StreamValueStore();
        XReadCommand xReadCommand = new XReadCommand(streamValueStore);

        List<String> args = List.of("XREAD", "BLOCK", "100", "STREAMS", streamName, "$");
        String output = xReadCommand.execute(args);

        String expected = RespString.getRespArrayString(List.of()); // Should return empty array on timeout
        assertEquals(expected, output);
    }

    @Test
    public void testXReadCommandMissingStreamsKeyword() {
        IStreamValueStore streamValueStore = new StreamValueStore();
        XReadCommand xReadCommand = new XReadCommand(streamValueStore);

        assertThrows(IllegalArgumentException.class, () -> {
            xReadCommand.execute(List.of("XREAD", "test_stream", "0-0"));
        });
    }

}