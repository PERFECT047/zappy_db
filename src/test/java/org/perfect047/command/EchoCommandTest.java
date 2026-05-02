package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.util.RespString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EchoCommandTest {

    @Test
    public void testEchoCommand() throws Exception {
        EchoCommand echoCommand = new EchoCommand();

        String output = echoCommand.execute(List.of("ECHO", "hello"));

        String expected = RespString.getRespBulkString(List.of("hello"));
        assertEquals(expected, output);
    }
}