package org.perfect047.command;

import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XReadCommand extends StreamValueCommand implements ICommand {

    public XReadCommand(IStreamValueStore streamValueStore) {
        super(streamValueStore);
    }

    @Override
    public String execute(List<String> args) throws Exception {

        if (args.size() < 4) {
            throw new IllegalArgumentException("Invalid XREAD arguments");
        }

        int index = 1;
        long blockTime = -1;

        // BLOCK parsing
        if ("BLOCK".equalsIgnoreCase(args.get(index))) {
            blockTime = Long.parseLong(args.get(index + 1));
            index += 2;
        }

        if (!"STREAMS".equalsIgnoreCase(args.get(index))) {
            throw new IllegalArgumentException("ERR missing STREAMS");
        }

        index++;

        List<Object> finalResult = handleStreams(args, index, blockTime);

        return RespString.getRespArrayString(finalResult);
    }

    /**
     * Handles STREAMS parsing + iteration
     */
    private List<Object> handleStreams(List<String> args, int index, long blockTime) {

        int remaining = args.size() - index;
        int n = remaining / 2;

        List<String> keys = args.subList(index, index + n);
        List<String> ids  = args.subList(index + n, args.size());

        List<Object> finalResult = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String key = keys.get(i);
            String id = resolveId(key, ids.get(i));

            List<Object> result = handleReadOrBlock(key, id, blockTime);

            if (result != null && !result.isEmpty()) {
                finalResult.add(List.of(key, result));
            }
        }

        return finalResult;
    }

    /**
     * Handles BLOCK vs non-BLOCK logic
     */
    private List<Object> handleReadOrBlock(String key, String id, long blockTime) {
        if (blockTime >= 0) {
            return streamValueStore.readBlocking(key, id, blockTime);
        } else {
            return streamValueStore.read(key, id);
        }
    }

    /**
     * Resolves special ID cases like "$"
     */
    private String resolveId(String key, String id) {
        if ("$".equals(id)) {
            String lastId = streamValueStore.getLastId(key);

            if (lastId == null) {
                return "0-0";
            }

            String[] parts = lastId.split("-");
            long ms = Long.parseLong(parts[0]);
            long seq = Long.parseLong(parts[1]);

            if (seq > 0) {
                return ms + "-" + (seq - 1);
            } else {
                return (ms - 1) + "-0";
            }
        }
        return id;
    }
}