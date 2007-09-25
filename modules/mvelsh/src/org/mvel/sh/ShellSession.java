package org.mvel.sh;

import org.mvel.MVEL;
import org.mvel.TemplateInterpreter;
import org.mvel.integration.impl.DefaultLocalVariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.sh.command.basic.BasicCommandSet;
import org.mvel.sh.command.file.FileCommandSet;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

        DefaultLocalVariableResolverFactory lvrf = new DefaultLocalVariableResolverFactory(variables);
        lvrf.appendFactory(new MapVariableResolverFactory(env));

        StringBuffer inBuffer = new StringBuffer();
        String[] inTokens;

        PrintStream out = System.out;

        Object outputBuffer;

        boolean multi = false;

        int multiIndentSize = 0;

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                if (!multi) {
                    String prompt = TemplateInterpreter.evalToString(env.get("$PROMPT"), variables);
                    multiIndentSize = prompt.length();
                    out.append(prompt);

                }
                else {
                    out.append(">");
                    out.append(indent((multiIndentSize - 1) + (depth * 4)));
                }

                inBuffer.append(new BufferedReader(new InputStreamReader(System.in)).readLine());

                inTokens = inBuffer.toString().split("\\s");

                if (commands.containsKey(inTokens[0])) {
                    String[] passParameters;
                    if (inTokens.length > 1) {
                        passParameters = new String[inTokens.length - 1];
                        System.arraycopy(inTokens, 1, passParameters, 0, passParameters.length);
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

                        if (Boolean.parseBoolean(env.get("$USE_OPTIMIZER_ALWAYS"))) {
                            outputBuffer = MVEL.executeExpression(MVEL.compileExpression(inBuffer.toString()), lvrf);
                        }
                        else {
                            outputBuffer = MVEL.eval(inBuffer.toString(), lvrf);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Eval Error: " + e.getMessage());
                        //    e.printStackTrace();

                        ByteArrayOutputStream stackTraceCap = new ByteArrayOutputStream();
                        PrintStream capture = new PrintStream(stackTraceCap);

                        e.printStackTrace(capture);


                        env.put("$LAST_STACK_TRACE", new String(stackTraceCap.toByteArray()));



                        inBuffer.delete(0, inBuffer.length());

                        continue;
                    }

                    if (outputBuffer != null && "true".equals(env.get("$ECHO"))) {
                        out.println(String.valueOf(outputBuffer));
                    }
                }

                inBuffer.delete(0, inBuffer.length());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("unexpected exception. exiting.");
        }

    }


    public boolean shouldDefer(StringBuffer inBuf) {
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
}
