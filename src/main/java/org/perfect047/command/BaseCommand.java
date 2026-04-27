package org.perfect047.command;

import java.io.OutputStream;

public class BaseCommand {

    private final OutputStream outputStream;

    public BaseCommand(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
