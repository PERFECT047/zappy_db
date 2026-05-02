package org.perfect047.command;

import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.util.RespString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class XAddCommand extends StreamValueCommand implements ICommand {

    public XAddCommand(IStreamValueStore streamValueStore) {
        super(streamValueStore);
    }

    @Override
    public String execute(List<String> args) throws IOException {

        if (args.size() < 5) {
            throw new IllegalArgumentException(
                    "XADD requires key, id and at least one field-value pair"
            );
        }

        String streamName = args.get(1);
        List<String> streamArgs = args.subList(2, args.size());

        if ((streamArgs.size() - 1) % 2 != 0) {
            throw new IllegalArgumentException(
                    "XADD field-value pairs must be even"
            );
        }

        String id = streamValueStore.add(streamName, streamArgs);

        return RespString.getRespBulkString(List.of(id));

    }
}