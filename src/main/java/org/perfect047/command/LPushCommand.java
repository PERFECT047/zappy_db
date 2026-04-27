package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class LPushCommand extends BaseCommand implements ICommand{

    public LPushCommand(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {
        IListValueStore db = StoreFactory.getListValueStore();

        try {
            Integer size = db.leftAdd(args.get(1),  args.subList(2, args.size()));

            getOutputStream().write(RespString.getRespIntegerString(size).getBytes());
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
