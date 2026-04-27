package org.perfect047.command;

import org.perfect047.storage.StoreFactory;
import org.perfect047.storage.listvalue.IListValueStore;
import org.perfect047.util.RespString;

import java.io.OutputStream;
import java.util.List;

public class LRangeCommand extends BaseCommand implements ICommand {

    public LRangeCommand(OutputStream outputStream) {
        super(outputStream);
    }


    @Override
    public void execute(List<String> args) {
        IListValueStore db = StoreFactory.getListValueStore();

        try {
            System.out.println(args);
            String listName = args.get(1);

            Integer startIndex = Integer.parseInt(args.get(2));
            Integer endIndex = Integer.parseInt(args.get(3));

            getOutputStream().write(RespString.getRespArrayString(db.get(listName, startIndex, endIndex)).getBytes());

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
