package org.mvel.sh;

import static org.mvel.MVEL.compileExpression;
import static org.mvel.MVEL.executeExpression;
import org.mvel.MVEL;
import static org.mvel.TemplateInterpreter.evalToString;
import org.mvel.integration.impl.DefaultLocalVariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.sh.command.basic.BasicCommandSet;
import org.mvel.sh.command.file.FileCommandSet;
import org.mvel.util.StringAppender;

import java.io.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.arraycopy;
import java.util.*;
import static java.util.ResourceBundle.getBundle;

/**
 * @author Christopher Brock
 */
public class ShellSession {
    public static final String PROMPT_VAR = "$PROMPT";
    private static final String[] EMPTY = new String[0];

    private final Map<String, Command> commands = new HashMap<String, Command>();
    private Map<String, Object> variables;
    private Map<String, String> env;

    private int depth;

    public void run() {
        System.out.println("Starting session...");

        variables = new HashMap<String, Object>();
        env = new HashMap<String, String>();

        commands.putAll(new BasicCommandSet().load());
        commands.putAll(new FileCommandSet().load());

        env.put(PROMPT_VAR, DefaultEnvironment.PROMPT);
        env.put("$OS_NAME", System.getProperty("os.name"));
        env.put("$OS_VERSION", System.getProperty("os.version"));
        env.put("$JAVA_VERSION", System.getProperty("java.version"));
        env.put("$CWD", new File(".").getAbsolutePath());
        env.put("$ECHO", "true");
        env.put("$USE_OPTIMIZER_ALWAYS", "false");
        env.put("$PATH", "");

        try {
            ResourceBundle bundle = getBundle(".mvelsh.properties");

            Enumeration<String> enumer = bundle.getKeys();
            String key;
            while (enumer.hasMoreElements()) {
                env.put(key = enumer.nextElement(), bundle.getString(key));
            }
        }
        catch (MissingResourceException e) {
            System.out.println("No config file found.  Loading default config.");
        }

        DefaultLocalVariableResolverFactory lvrf = new DefaultLocalVariableResolverFactory(variables);
        lvrf.appendFactory(new MapVariableResolverFactory(env));

        StringAppender inBuffer = new StringAppender();
        String[] inTokens;
        PrintStream out = System.out;
        Object outputBuffer;
        boolean multi = false;
        int multiIndentSize = 0;

        final PrintStream sysPrintStream = System.out;
        final PrintStream sysErrorStream = System.err;
        final InputStream sysInputStream = System.in;

        InputStreamReader reader = new InputStreamReader(System.in);
        BufferedReader readBuffer = new BufferedReader(reader);

        String prompt;
        String execPath;
        File execFile;

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                if (!multi) {
                    multiIndentSize = (prompt = evalToString(env.get("$PROMPT"), variables)).length();
                    out.append(prompt);
                }
                else {
                    out.append(">").append(indent((multiIndentSize - 1) + (depth * 4)));
                }

                if (commands.containsKey((inTokens =
                        inBuffer.append(readBuffer.readLine()).toString().split("\\s"))[0])) {

                    String[] passParameters;
                    if (inTokens.length > 1) {
                        arraycopy(inTokens, 1, passParameters = new String[inTokens.length - 1], 0, passParameters.length);
                    }
                    else {
                        passParameters = EMPTY;
                    }

                    try {
                        commands.get(inTokens[0]).execute(this, passParameters);
                    }
                    catch (CommandException e) {
                        out.append("Error: ").append(e.getMessage()).append("\n");
                    }
                }
                else {
                    try {

                        if (shouldDefer(inBuffer)) {
                            multi = true;
                            continue;
                        }
                        else {
                            multi = false;
                        }

                        if (parseBoolean(env.get("$USE_OPTIMIZER_ALWAYS"))) {
                            outputBuffer = executeExpression(compileExpression(inBuffer.toString()), lvrf);
                        }
                        else {
                            outputBuffer = MVEL.eval(inBuffer.toString(), lvrf);
                        }
                    }
                    catch (Exception e) {
                        if ((execPath = inTokens[0]).startsWith("./"))
                            execPath = new File(env.get("$CWD")).getAbsolutePath() + execPath.substring(execPath.indexOf('/'));

                        if ((execFile = new File(execPath)).exists() && execFile.isFile()) {
                            String[] execString = new String[inTokens.length - 1];

                            if (inTokens.length > 1) {
                                arraycopy(inTokens, 1, execString, 1, inTokens.length - 1);
                            }

                            try {
                                final Process p = getRuntime().exec(execFile.getAbsolutePath(), execString);
                                final OutputStream outStream = p.getOutputStream();

                                final InputStream inStream = p.getInputStream();
                                final InputStream errStream = p.getErrorStream();

                                final RunState runState = new RunState();

                                final Thread pollingThread = new Thread(new Runnable() {
                                    public void run() {
                                        byte[] buf = new byte[25];
                                        int read;

                                        while (true) {
                                            try {
                                                while ((read = inStream.read(buf)) > 0) {
                                                    for (int i = 0; i < read; i++) {
                                                        sysPrintStream.print((char) buf[i]);
                                                    }
                                                    sysPrintStream.flush();
                                                }
                                            }
                                            catch (Exception e) {
                                                break;
                                            }
                                        }

                                        System.out.println("Process Exited: Returning to MVELSH - Press Enter");
                                    }
                                });

                                Thread watchThread = new Thread(new Runnable() {

                                    public void run() {
                                        try {
                                            p.waitFor();
                                        }
                                        catch (InterruptedException e) {
                                            // nothing;
                                        }

                                        runState.setRunning(false);

                                        try {
                                            inStream.close();
                                            outStream.close();
                                        }
                                        catch (IOException e) {
                                            // nothing;
                                        }
                                    }
                                });

                                pollingThread.setPriority(Thread.MIN_PRIORITY);
                                pollingThread.start();

                                watchThread.setPriority(Thread.MIN_PRIORITY);
                                watchThread.start();

                                while (runState.isRunning()) {
                                    try {
                                        char[] input = readBuffer.readLine().toCharArray();
                                        for (char anInput : input) {
                                            outStream.write((byte) anInput);
                                        }

                                        outStream.write((byte) '\n');
                                        outStream.flush();
                                    }
                                    catch (Exception e2) {
                                        break;
                                    }
                                }

                                try {
                                    pollingThread.notify();
                                }
                                catch (Exception ne) {

                                }

                                inBuffer.reset();
                                continue;
                            }
                            catch (Exception e2) {
                                // fall through;
                            }
                        }

                        System.out.println("Eval Error: " + e.getMessage());

                        ByteArrayOutputStream stackTraceCap = new ByteArrayOutputStream();
                        PrintStream capture = new PrintStream(stackTraceCap);

                        e.printStackTrace(capture);

                        env.put("$LAST_STACK_TRACE", new String(stackTraceCap.toByteArray()));

                        inBuffer.reset();

                        continue;
                    }

                    if (outputBuffer != null && "true".equals(env.get("$ECHO"))) {
                        out.println(String.valueOf(outputBuffer));
                    }
                }

                inBuffer.reset();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("unexpected exception. exiting.");
        }

    }


    public boolean shouldDefer(StringAppender inBuf) {
        char[] buffer = new char[inBuf.length()];
        inBuf.getChars(0, inBuf.length(), buffer, 0);

        depth = 0;
        for (char aBuffer : buffer) {
            switch (aBuffer) {
                case'{':
                    depth++;
                    break;
                case'}':
                    depth--;
                    break;
            }
        }

        return depth > 0;
    }

    public String indent(int size) {
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < size; i++) sbuf.append(" ");
        return sbuf.toString();
    }


    public Map<String, Command> getCommands() {
        return commands;
    }


    public Map<String, Object> getVariables() {
        return variables;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public static final class RunState {
        private boolean running = true;


        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }
}
