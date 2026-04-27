package org.perfect047.command;

import org.perfect047.util.RespString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PingCommand extends BaseCommand implements ICommand{

    PingCommand(OutputStream outputStream){
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {
        System.out.println("Ping Command");
        try {
            String writeOut = RespString.getRespSimpleString(List.of("PONG"));
            System.out.println(writeOut);
            this.getOutputStream().write(writeOut.getBytes());
        }
        catch (IOException ex) {
            System.out.println("Command Execution Exception: " + ex.getMessage());
        }
    }
}
