package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class BLPopCommand extends BaseCommand implements ICommand{

    public BLPopCommand(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {
        IListValueStore db = StoreFactory.getListValueStore();

        try{
            String listName = args.get(1);
            float seconds = 0f;

            if(args.size()>2) seconds = Float.parseFloat(args.get(2));

            getOutputStream().write(RespString.getRespArrayString(db.blockingLeftPop(listName, seconds)).getBytes());
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
