package org.perfect047.command;

import java.io.OutputStream;

/**
 * Shared base for all commands that write RESP output to the client socket.
 */
public abstract class BaseCommand {

    private final OutputStream outputStream;

    /**
     * @param outputStream stream bound to the current client connection
     */
    protected BaseCommand(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * @return the output stream used to write the command response
     */
    protected OutputStream getOutputStream() {
        return outputStream;
    }
}
