package org.perfect047.command;

import org.junit.jupiter.api.Test;
import org.perfect047.util.RespString;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PingCommandTest {

    @Test
    public void testPingCommand() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PingCommand pingCommand = new PingCommand(outputStream);

        pingCommand.execute(List.of("PING"));

        String expected = RespString.getRespSimpleString(List.of("PONG"));
        assertEquals(expected, outputStream.toString());
    }
}