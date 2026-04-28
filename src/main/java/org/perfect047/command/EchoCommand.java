package org.perfect047.command;

import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

public class EchoCommand extends BaseCommand implements ICommand {

    private static final Logger LOGGER = Logger.getLogger(EchoCommand.class.getName());

    public EchoCommand(OutputStream outputStream){
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) throws Exception {
        LOGGER.fine("Executing ECHO command");
        if (args.size() < 2) {
            throw new IllegalArgumentException("ECHO command requires an argument");
        }
        String writeOut = RespString.getRespBulkString(List.of(args.get(1)));
        LOGGER.fine("ECHO response: " + writeOut);
        this.getOutputStream().write(writeOut.getBytes());
    }
}
