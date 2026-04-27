package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EchoCommandTest {

    @Test
    public void testEchoCommand() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EchoCommand echoCommand = new EchoCommand(outputStream);

        echoCommand.execute(List.of("ECHO", "hello"));

        String expected = RespString.getRespBulkString(List.of("hello"));
        assertEquals(expected, outputStream.toString());
    }
}