package org.mvel.sh.command.basic;

import org.mvel.sh.CommandSet;
import org.mvel.sh.Command;

import java.util.Map;
import java.util.HashMap;

public class BasicCommandSet implements CommandSet {

    public Map<String, Command> load() {
        Map<String, Command> cmds = new HashMap<String, Command>();

        cmds.put("set", new Set());
        cmds.put("help", new Help());
        cmds.put("showvars", new ShowVars());
        cmds.put("exit", new Exit());

        return cmds;
    }
}

