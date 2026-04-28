package org.perfect047.handler;

import org.perfect047.command.CommandFactory;
import org.perfect047.command.ICommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the lifecycle of a single client socket: read, parse, dispatch, respond.
 */
public class ClientHandler implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private final Socket clientSocket;
    private final CommandFactory commandFactory;

    /**
     * @param clientSocket connected client socket
     * @param commandFactory factory used to resolve and instantiate commands
     */
    public ClientHandler(Socket clientSocket, CommandFactory commandFactory){
        this.clientSocket = clientSocket;
        this.commandFactory = commandFactory;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while((bytesRead = inputStream.read(buffer)) != -1 ){
                List<String> cmd = parseCommand(buffer, bytesRead);
                executeCommand(cmd, outputStream);
            }
        }
        catch (IOException e){
            LOGGER.log(Level.WARNING, "Client connection error: " + e.getMessage(), e);
        }
        finally {
            try {
                clientSocket.close();
                LOGGER.info("Client connection closed");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing client socket: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Parses a single RESP payload chunk into command tokens.
     * This is a lightweight parser for the current command set, not a full streaming RESP parser.
     *
     * @param buffer raw bytes read from the socket
     * @param bytesRead number of valid bytes in {@code buffer}
     * @return filtered command tokens ready for dispatch
     */
    private List<String> parseCommand(byte[] buffer, int bytesRead) {
        return Arrays.stream(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8).split("\\r\\n"))
                .filter(s -> !s.isEmpty() && !s.startsWith("*") && !s.startsWith("$"))
                .toList();
    }

    /**
     * Resolves the command, executes it, and writes protocol errors back to the client.
     *
     * @param cmd parsed command tokens
     * @param outputStream client output stream
     * @throws IOException when writing the response fails
     */
    private void executeCommand(List<String> cmd, OutputStream outputStream) throws IOException {
        if (cmd.isEmpty()) {
            return;
        }

        for (String cmdStr : cmd) {
            LOGGER.fine("Received command part: " + cmdStr);
        }

        try {
            ICommand command = commandFactory.getCommand(cmd.getFirst().toUpperCase(), outputStream);

            if (command == null) {
                writeError(outputStream, "unknown command '" + cmd.getFirst() + "'");
                return;
            }

            command.execute(cmd);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid command arguments: " + e.getMessage(), e);
            writeError(outputStream, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Command execution error: " + e.getMessage(), e);
            writeError(outputStream, "Internal server error");
        }
    }

    /**
     * Writes a RESP error response in the standard {@code -ERR} format.
     *
     * @param outputStream client output stream
     * @param message error message body
     * @throws IOException when the socket write fails
     */
    private void writeError(OutputStream outputStream, String message) throws IOException {
        outputStream.write(("-ERR " + message + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

}
