package org.mvel.jsr223;

import org.mvel.CompileException;
import org.mvel.ExecutableStatement;
import org.mvel.MVEL;
import org.mvel.PropertyAccessException;
import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Map;

/**
 * This implementation of ScriptEngine also implements the optional interfaces
 * of javax.script <code>javax.script.Compilable</code> and <code>javax.script.Invocable</code>.
 * Since MVEL 1.2 does not support functions, only invokeMethod method on the Invocable interface
 * is implemented. All other methods defined on this interface throw exceptions.
 * <p/>
 * @author Richard L. Burton III
 * @version 1.0
 * @since MVEL 1.2
 */
public class MVELScriptEngine extends AbstractScriptEngine implements Compilable, Invocable {

    /**
     * This is used for when a null is passed into convertToStringArray
     */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * The string literal to be used when invoking a method with invokeMethod.
     */
    private static final String THIS_SYMBOL = "this";

    /**
     * The buffer size to use when reading in the java.io.Reader.
     */
    private static final int CHAR_BUFFER_SIZE = 512;

    /**
     * The ScriptEngineFactory that created the ScriptEngine.
     */
    private ScriptEngineFactory scriptingEngineFactory;

    /**
     * Constructs an instance with null reference to the ScriptEngineFactory.
     */
    public MVELScriptEngine() {
        this(null);
    }

    /**
     * Constructs an instance of the MVELScriptEngine that holds reference to that ScriptingEngineFactory.
     * @param scriptingEngineFactory The ScriptingEngineFactory that created the ScriptEngine.
     */
    protected MVELScriptEngine(ScriptEngineFactory scriptingEngineFactory) {
        this.scriptingEngineFactory = scriptingEngineFactory;
    }

    /**
     * Causes the immediate execution of the script whose source is the String passed as the
     * first argument. The script may be reparsed or recompiled before execution. State left
     * in the engine from previous executions, including variable values and compiled procedures
     * may be visible during this execution.
     * @param script        The script to be executed by the script engine.
     * @param scriptContext A ScriptContext exposing sets of attributes in different scopes.
     *                      The meanings of the scopes ScriptContext.GLOBAL_SCOPE, and
     *                      ScriptContext.ENGINE_SCOPE are defined in the specification.
     *                      The ENGINE_SCOPE Bindings of the ScriptContext contains the bindings of
     *                      scripting variables to application objects to be used during this script
     *                      execution.
     * @return The value returned from the execution of the script.
     * @throws ScriptException      if an error occurrs in script. ScriptEngines should create and throw
     *                              ScriptException wrappers for checked Exceptions thrown by underlying
     *                              scripting implementations.
     * @throws NullPointerException if either argument is null.
     */
    public Object eval(String script, ScriptContext scriptContext) throws ScriptException {

        if (script == null) {
            throw new NullPointerException("The expression argument can not be null.");
        }

        if (scriptContext == null) {
            throw new NullPointerException("The ScriptContext argument can not be null.");
        }

        try {
            ExecutableStatement executableStatement = (ExecutableStatement) MVEL.compileExpression(script);
            Map<String, Object> scriptContextEngineView = new MVELScriptContextMap(scriptContext);
            VariableResolverFactory variableResolverFactory = new MapVariableResolverFactory(scriptContextEngineView);
            return executableStatement.getValue(scriptContext, variableResolverFactory);
        } catch (CompileException ce) {
            throw new ScriptException(ce.getMessage(), new String(ce.getExpr()), ce.getCursor());
        }
    }

    /**
     * Same as eval(String, ScriptContext) where the source of the script is read from a Reader.
     * @param reader        The source of the script to be executed by the script engine.
     * @param scriptContext The ScriptContext passed to the script engine.
     * @return The value returned from the execution of the script.
     * @throws ScriptException      if an error occurrs in script.
     * @throws NullPointerException if either argument is null.
     */
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {

        if (reader == null) {
            throw new NullPointerException("The Reader argument can not be null.");
        }

        if (scriptContext == null) {
            throw new NullPointerException("The ScriptContext argument can not be null.");
        }

        StringBuilder builder = new StringBuilder();
        CharBuffer cb = CharBuffer.allocate(CHAR_BUFFER_SIZE);

        try {
            while (reader.read(cb) != -1) {
                builder.append(cb.array());
            }

            return eval(builder.toString(), scriptContext);
        } catch (IOException e) {
            throw new ScriptException(e);
        } catch (CompileException ce) {
            throw new ScriptException(ce.getMessage(), new String(ce.getExpr()), ce.getCursor());
        }
    }

    /**
     * Returns an uninitialized Bindings.
     * @return A Bindings that can be used to replace the state of this ScriptEngine.
     */
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    /**
     * Returns a ScriptEngineFactory for the class to which this ScriptEngine belongs.
     * @return The ScriptEngineFactory
     */
    public ScriptEngineFactory getFactory() {
        if (scriptingEngineFactory == null)
            scriptingEngineFactory = new MVELScriptEngineFactory();
        return scriptingEngineFactory;
    }

    /**
     * Compiles the script (source represented as a String) for later execution.
     * @param expression The source of the script, represented as a String.
     * @return An subclass of CompiledScript to be executed later using one of the eval methods
     *         of CompiledScript.
     * @throws ScriptException      if compilation fails.
     * @throws NullPointerException if the argument is null.
     */
    public CompiledScript compile(String expression) throws ScriptException {

        if (expression == null) {
            throw new NullPointerException("The expression argument can not be null.");
        }

        try {
            ExecutableStatement executableStatement = (ExecutableStatement) MVEL.compileExpression(expression);
            return new MVELCompiledScript(this, executableStatement);
        } catch (CompileException ce) {
            throw new ScriptException(ce.getMessage(), new String(ce.getExpr()), ce.getCursor());
        }
    }

    /**
     * Compiles the script (source read from Reader) for later execution. Functionality is identical
     * to compile(String) other than the way in which the source is passed.
     * @param reader The reader from which the script source is obtained.
     * @return An implementation of CompiledScript to be executed later using one of its eval methods
     *         of CompiledScript.
     * @throws ScriptException      if compilation fails.
     * @throws NullPointerException if the argument is null.
     */
    public CompiledScript compile(Reader reader) throws ScriptException {

        if (reader == null) {
            throw new NullPointerException("The Reader argument can not be null.");
        }

        StringBuilder builder = new StringBuilder();
        CharBuffer cb = CharBuffer.allocate(CHAR_BUFFER_SIZE);
        try {
            while (reader.read(cb) != -1) {
                builder.append(cb.array());
            }
        } catch (IOException e) {
            throw new ScriptException(e);
        }

        try {
            ExecutableStatement executableStatement = (ExecutableStatement) MVEL.compileExpression(builder.toString());
            return new MVELCompiledScript(this, executableStatement);
        } catch (CompileException ce) {
            throw new ScriptException(ce.getMessage(), new String(ce.getExpr()), ce.getCursor());
        }
    }

    /**
     * Calls a method on a script object compiled during a previous script execution, which is
     * retained in the state of the ScriptEngine
     * @param thiz   The name of the procedure to be called.
     * @param method If the procedure is a member of a class defined in the script and thiz is
     *               an instance of that class returned by a previous execution or invocation,
     *               the named method is called through that instance.
     * @param args   Arguments to pass to the procedure. The rules for converting the arguments
     *               to scripting variables are implementation-specific.
     * @return The value returned by the procedure. The rules for converting the scripting
     *         variable returned by the script method to a Java Object are implementation-specific.
     * @throws ScriptException          if an error occurrs during invocation of the method.
     * @throws NoSuchMethodException    if method with given name or matching argument types cannot be found.
     * @throws NullPointerException     if the method name is null.
     * @throws IllegalArgumentException if the specified thiz is null or the specified Object does not
     *                                  represent a scripting object.
     */
    public Object invokeMethod(Object thiz, String method, Object... args) throws ScriptException, NoSuchMethodException {

        if (thiz == null) {
            throw new IllegalArgumentException("The target object can not be null.");
        }

        if (method == null) {
            throw new NullPointerException("The method name argument can not be null.");
        }

        String[] arguments = convertToStringArray(args);
        String syntax = scriptingEngineFactory.getMethodCallSyntax(THIS_SYMBOL, method, arguments);

        try {
            return MVEL.eval(syntax, thiz);
        } catch (PropertyAccessException pae) {
            throw new NoSuchMethodException(pae.getMessage());
        }
    }

    /**
     * Used to call top-level procedures and functions defined in scripts.
     * @param name The name of the procedure to be called.
     * @param args Arguments to pass to the procedure or function
     * @return The value returned by the procedure or function
     * @throws ScriptException       if an error occurrs during invocation of the method.
     * @throws NoSuchMethodException if method with given name or matching argument types cannot be found.
     * @throws NullPointerException  if method name is null.
     */
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        throw new ScriptException("This operation is unsupported.");
    }

    /**
     * Returns an implementation of an interface using functions compiled in the interpreter. The methods
     * of the interface may be implemented using the invokeFunction method.
     * @param clazz The Class object of the interface to return.
     * @return An instance of requested interface - null if the requested interface is unavailable, i. e.
     *         if compiled functions in the ScriptEngine cannot be found matching the ones in the requested
     *         interface.
     * @throws IllegalArgumentException if the specified Class object is null or is not an interface.
     */
    public <T> T getInterface(Class<T> clazz) {
        throw new IllegalArgumentException("This operation is unsupported.");
    }

    /**
     * Returns an implementation of an interface using member functions of a scripting object compiled in the
     * interpreter. The methods of the interface may be implemented using the invokeMethod method.
     * @param thiz  The scripting object whose member functions are used to implement the methods of the interface.
     * @param clazz - The Class object of the interface to return.
     * @return An instance of requested interface - null if the requested interface is unavailable, i. e. if compiled
     *         methods in the ScriptEngine cannot be found matching the ones in the requested interface.
     * @throws IllegalArgumentException if the specified Class object is null or is not an interface, or if the specified
     *                                  Object is null or does not represent a scripting object.
     */
    public <T> T getInterface(Object thiz, Class<T> clazz) {
        throw new IllegalArgumentException("This operation is unsupported.");
    }

    /**
     * This helper method converts an Object[] into a String[].
     * @param args The Object[] to convert.
     * @return The converted array.
     */
    private String[] convertToStringArray(Object... args) {

        if(args != null){
            String[] stringArguments = new String[args.length];
            int i = 0;
            for (Object arg : args) {
                if (arg instanceof String || arg instanceof Character) {
                    stringArguments[i++] = "'" + arg + "'";
                } else {
                    stringArguments[i++] = String.valueOf(arg);
                }
            }
            return stringArguments;
        }

        return EMPTY_STRING_ARRAY;
    }

}
