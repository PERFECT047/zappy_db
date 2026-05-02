package org.perfect047.handler;

import org.perfect047.command.CommandFactory;
import org.perfect047.command.ICommand;
import org.perfect047.util.RespString;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared command processing logic for all concurrency models.
 */
public class ClientProcessor {

    private static final Logger LOGGER = Logger.getLogger(ClientProcessor.class.getName());

    private final CommandFactory factory;

    /**
     * @param factory command factory used to resolve commands
     */
    public ClientProcessor(CommandFactory factory) {
        this.factory = factory;
    }

    /**
     * Processes raw input and returns RESP encoded response.
     *
     * @param data   input bytes
     * @param length valid length
     * @return RESP encoded response or null
     */
    public byte[] process(byte[] data, int length) {
        List<String> tokens = Arrays.stream(
                        new String(data, 0, length, StandardCharsets.UTF_8).split("\\r\\n"))
                .filter(s -> !s.isEmpty() && !s.startsWith("*") && !s.startsWith("$"))
                .toList();

        if (tokens.isEmpty()) return null;

        try {
            ICommand cmd = factory.getCommand(tokens.getFirst().toUpperCase());

            if (cmd == null) {
                return RespString.getRespErrorString("unknown command").getBytes(StandardCharsets.UTF_8);
            }

            String result = cmd.execute(tokens);
            return result != null ? result.getBytes(StandardCharsets.UTF_8) : null;

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid args: " + e.getMessage(), e);
            return RespString.getRespErrorString(e.getMessage()).getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Execution error: " + e.getMessage(), e);
            return RespString.getRespErrorString("Internal server error").getBytes(StandardCharsets.UTF_8);
        }
    }
}