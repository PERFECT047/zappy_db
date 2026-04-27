package org.perfect047.command;

import org.perfect047.util.RespString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class EchoCommand extends BaseCommand implements ICommand {

    EchoCommand(OutputStream outputStream){
        super(outputStream);
    }


    @Override
    public void execute(List<String> args) {
        System.out.println("Echo Command");
        try {
            String writeOut = RespString.getRespBulkString(List.of(args.get(1)));
            System.out.println(writeOut);
            this.getOutputStream().write(writeOut.getBytes());
        }
        catch (IOException ex) {
            System.out.println("Command Execution Exception: " + ex.getMessage());
        }
    }
}
