package org.perfect047.command;

import java.util.List;

public interface ICommand {
    String execute(List<String> args) throws Exception;
}
