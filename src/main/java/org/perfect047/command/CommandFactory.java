package org.perfect047.command;

import java.io.OutputStream;

public class CommandFactory {

    public static ICommand getCommand(String commandName, OutputStream outputStream) {

        return switch (commandName) {

            case "PING" -> new PingCommand(outputStream);

            case "ECHO" -> new EchoCommand(outputStream);

            case "SET" -> new SetCommand(outputStream);

            case "GET" -> new GetCommand(outputStream);

            case "LPUSH" -> new LPushCommand(outputStream);

            case "RPUSH" -> new RPushCommand(outputStream);

            case "LRANGE" -> new LRangeCommand(outputStream);

            case "LLEN" -> new LLenCommand(outputStream);

            case "LPOP" -> new LPopCommand(outputStream);

            case"BLPOP" -> new BLPopCommand(outputStream);

            default -> null;
        };
    }

}
