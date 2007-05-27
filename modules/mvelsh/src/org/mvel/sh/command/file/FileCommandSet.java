package org.mvel.sh.command.file;

import org.mvel.sh.CommandSet;
import org.mvel.sh.Command;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Mike Brock
 * Date: 6-Feb-2007
 * Time: 10:51:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileCommandSet implements CommandSet {

    public Map<String, Command> load() {
        Map<String, Command> cmd = new HashMap<String, Command>();

        cmd.put("ls", new DirList());
        cmd.put("cd", new ChangeWorkingDir());

        return cmd;
    }
}
