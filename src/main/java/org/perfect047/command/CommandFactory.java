package org.perfect047.command;

import java.io.OutputStream;

public class CommandFactory {

    public static ICommand getCommand(String commandName, OutputStream outputStream) {
        ICommand command = null;
        switch (commandName){
            case "PING":
                command = new PingCommand(outputStream);
                break;

            case "ECHO":
                command = new EchoCommand(outputStream);
                break;

            case "SET":
                command = new SetCommand(outputStream);
                break;

            case "GET":
                command = new GetCommand(outputStream);
                break;
        }

        return command;
    }

}
