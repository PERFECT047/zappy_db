package org.perfect047.command;

import org.perfect047.storage.streamvalue.IStreamValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class XRangeCommand extends StreamValueCommand implements ICommand{

    public XRangeCommand(OutputStream outputStream, IStreamValueStore streamValueStore){
        super(outputStream, streamValueStore);
    }

    @Override
    public void execute(List<String> args) throws Exception {

        if (args.size() < 4) {
            throw new IllegalArgumentException(
                    "XRANGE requires key, start and end"
            );
        }

        String streamName = args.get(1);
        String start = args.get(2);
        String end = args.get(3);

        List<Object> result = streamValueStore.range(streamName, start, end);

        getOutputStream().write(
                RespString.getRespArrayString(result).getBytes()
        );
    }

}
