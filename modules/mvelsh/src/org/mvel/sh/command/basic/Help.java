package org.mvel.sh.command.basic;

import org.mvel.sh.Command;
import org.mvel.sh.ShellSession;

import java.util.Map;

public class Help implements Command {
    public Object execute(ShellSession session, String[] args) {
        for (String command : session.getCommands().keySet()) {
            System.out.println(command + " - " + session.getCommands().get(command).getDescription());
        }

        return null;
    }


    public String getDescription() {
        return "displays help for available shell commands";
    }

    public String getHelp() {
        return "No help yet";
    }
}
