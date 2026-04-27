package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class SetCommand extends BaseCommand implements ICommand{

    public SetCommand(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {

        IKeyValueStore db = StoreFactory.getKeyValueStore();

        try{
            Long millis = null;

            if (args.size() >= 5) {
                millis = getExpiry(args.get(3), args.get(4));
            }

            db.set(args.get(1), args.get(2), millis);
            this.getOutputStream().write(RespString.getRespSimpleString(List.of("OK")).getBytes());
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

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
        }

        return millis;
    }

}
