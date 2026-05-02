package org.perfect047.handler;

import org.perfect047.command.CommandFactory;
import org.perfect047.util.RespString;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles non-blocking client data: accumulate, parse, execute, respond.
 */
public class NioClientHandler {

    private static final Logger LOGGER = Logger.getLogger(NioClientHandler.class.getName());
    private final ClientProcessor processor;
    // Per-connection buffer for partial reads
    private final ByteBuffer buffer = ByteBuffer.allocate(4096);

    /**
     * @param factory command factory used to resolve commands
     */
    public NioClientHandler(CommandFactory factory) {
        this.processor = new ClientProcessor(factory);
    }

    /**
     * Processes incoming data and returns serialized responses.
     *
     * @param incoming newly read data from socket
     * @return list of RESP encoded responses
     */
    public List<byte[]> process(ByteBuffer incoming) {
        List<byte[]> responses = new ArrayList<>();

        buffer.put(incoming);
        buffer.flip();

        while (true) {
            String line = readLine();
            if (line == null) break;

            List<String> tokens = List.of(line.split(" "));
            try {
                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                byte[] response = processor.process(data, data.length);

                if (response != null) {
                    responses.add(response);
                }
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid args: " + e.getMessage(), e);
                responses.add(RespString.getRespErrorString(e.getMessage()).getBytes(StandardCharsets.UTF_8));

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Execution error: " + e.getMessage(), e);
                responses.add(RespString.getRespErrorString("Internal server error").getBytes(StandardCharsets.UTF_8));
            }
        }

        buffer.compact();
        return responses;
    }

    /**
     * Reads a single CRLF-terminated line from buffer.
     *
     * @return decoded line or null if incomplete
     */
    private String readLine() {
        for (int i = buffer.position(); i < buffer.limit() - 1; i++) {
            if (buffer.get(i) == '\r' && buffer.get(i + 1) == '\n') {
                int len = i - buffer.position();
                byte[] data = new byte[len];

                buffer.get(data);
                buffer.get(); // CR
                buffer.get(); // LF

                return new String(data, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}