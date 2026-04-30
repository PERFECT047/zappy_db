package org.perfect047.command;

import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XReadCommand extends StreamValueCommand implements ICommand {

    public XReadCommand(OutputStream outputStream, IStreamValueStore streamValueStore) {
        super(outputStream, streamValueStore);
    }

    @Override
    public void execute(List<String> args) throws Exception {

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

        getOutputStream().write(RespString.getRespArrayString(finalResult).getBytes());
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
            String id  = resolveId(keys.get(i), ids.get(i));

            List<Object> result = handleReadOrBlock(key, id, blockTime);

            if (result != null && !result.isEmpty()) {
                finalResult.add(result.get(0));
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
            return streamValueStore.getLastId(key);
        }
        return id;
    }
}