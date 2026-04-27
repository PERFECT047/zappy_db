package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.keyvalue.IKeyValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class GetCommand extends BaseCommand implements ICommand {

    public GetCommand(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {
        IKeyValueStore db = StoreFactory.getKeyValueStore();

        try{
            String value = db.get(args.get(1));

            this.getOutputStream().write(RespString.getRespBulkString(
                    value == null ? List.of() : List.of(value)
            ).getBytes());
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
