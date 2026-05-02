package org.perfect047.handler;

import org.perfect047.command.CommandFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a single client connection: read, parse, execute, respond.
 */
public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final int BUFFER_SIZE = 4096;

    // Tune this (32–64 is usually optimal)
    private static final int FLUSH_BATCH_SIZE = 32;

    private final ClientProcessor processor;
    private final Socket clientSocket;

    /**
     * @param clientSocket   connected client socket
     * @param commandFactory factory used to resolve commands
     */
    public ClientHandler(Socket clientSocket, CommandFactory commandFactory) {
        this.clientSocket = clientSocket;
        this.processor = new ClientProcessor(commandFactory);
    }

    @Override
    public void run() {
        int pending = 0;

        try (
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                byte[] response = processor.process(buffer, bytesRead);

                if (response != null) {
                    outputStream.write(response);
                    pending++;

                    // Batch flush (reduces syscalls significantly)
                    if (pending >= FLUSH_BATCH_SIZE) {
                        outputStream.flush();
                        pending = 0;
                    }
                }
            }

            // Final flush (ensure remaining data is sent)
            if (pending > 0) {
                outputStream.flush();
            }

        } catch (Exception e) {
            // Keep warning for real errors
            LOGGER.log(Level.WARNING, "Client connection error: " + e.getMessage(), e);

        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing client socket: " + e.getMessage(), e);
            }

            // Downgraded from INFO → FINE (avoid overhead in production/bench)
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Client connection closed");
            }
        }
    }
}