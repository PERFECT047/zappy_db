package org.perfect047.command;

import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class SetCommand extends KeyValueCommand implements ICommand{

    public SetCommand(OutputStream outputStream, IKeyValueStore keyValueStore) {
        super(outputStream, keyValueStore);
    }

    @Override
    public void execute(List<String> args) throws Exception {
        if (args.size() < 3) {
            throw new IllegalArgumentException("SET command requires key and value");
        }
        Long millis = null;

        if (args.size() >= 5) {
            millis = getExpiry(args.get(3), args.get(4));
        }

        keyValueStore.set(args.get(1), args.get(2), millis);
        this.getOutputStream().write(RespString.getRespSimpleString(List.of("OK")).getBytes());
    }

    private Long getExpiry(String arg, String value){
        if(arg == null) return null;

        long millis = Long.parseLong(value);

        switch(arg.toUpperCase()){
            case "PX":
                break;
            case "EX":
                millis = millis * 1000;
                break;
            default:
                throw new IllegalArgumentException("SET only supports EX or PX expiry options");
        }

        return millis;
    }

}
