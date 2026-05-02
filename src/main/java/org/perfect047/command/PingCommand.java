package org.perfect047.command;

import org.perfect047.util.RespString;

import java.util.List;
import java.util.logging.Logger;

public class PingCommand implements ICommand {

    private static final Logger LOGGER = Logger.getLogger(PingCommand.class.getName());

    @Override
    public String execute(List<String> args) throws Exception {

        LOGGER.fine("Executing PING command");

        String writeOut = RespString.getRespSimpleString(List.of("PONG"));

        LOGGER.fine("PING response: " + writeOut);

        return writeOut;
    }
}