package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class LLenCommand extends BaseCommand implements ICommand {

    public LLenCommand(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {
        IListValueStore db = StoreFactory.getListValueStore();

        try{
            String listName = args.get(1);

            getOutputStream().write(RespString.getRespIntegerString(db.getSize(listName)).getBytes());
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
