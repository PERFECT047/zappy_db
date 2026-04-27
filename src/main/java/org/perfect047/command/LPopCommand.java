package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class LPopCommand extends BaseCommand implements ICommand{

    public LPopCommand(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void execute(List<String> args) {

        IListValueStore db = StoreFactory.getListValueStore();

        try{
            String listName = args.get(1);
            Integer repetitions = 1;

            if (args.size() > 2) {
                repetitions = Integer.parseInt(args.get(2));
            }

            List<String> result = db.leftPop(listName, repetitions);

            getOutputStream().write(
                    ((result == null) || (result.size() <= 1)) ?
                        RespString.getRespBulkString(result).getBytes() :
                        RespString.getRespArrayString(result).getBytes()
            );
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
