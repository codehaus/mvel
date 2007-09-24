package org.mvel.jsr223;

import static org.mvel.MVEL.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Instances of ScriptEngineFactory are the Objects located by the
 * Script Engine Discovery Mechanism. See 4.2.4 of the Scripting
 * for the Java Platform Specification for more information.
 * @author Richard L. Burton III
 * @version 1.0
 * @since MVEL 1.2
 */
public class MVELScriptEngineFactory implements ScriptEngineFactory {

    /**
     * The string literl to be used for a null argument when building
     * the method syntax.
     */
    private static final String NULL_SYMBOL = "null";

    /**
     * The string literal used when terminating a statement.
     */
    private static final String STATEMENT_TERMINATOR = ";";

    /**
     * A list of filename extensions
     */
    static final List<String> extentions = Collections.unmodifiableList(Arrays.asList("mvel"));

    /**
     * A list of mimetypes, associated with scripts that can be executed by the
     * engine.
     */
    static final List<String> mimeTypes = Collections.unmodifiableList(Arrays.asList(
            "application/x-mvel",
            "application/x-mvel-source"));

    /**
     * A list of the full names of the ScriptEngine.
     */
    static final List<String> names = Collections.unmodifiableList(Arrays.asList("mvel"));

    /**
     * Returns the full name of the ScriptEngine.
     * @return The name of the engine implementation.
     */
    public String getEngineName() {
        return NAME;
    }

    /**
     * Returns the version of the ScriptEngine.
     * @return The ScriptEngine implementation version.
     */
    public String getEngineVersion() {
        return VERSION;
    }

    /**
     * Returns an immutable list of filename extensions, which generally identify scripts written
     * in the language supported by this ScriptEngine. The array is used by the ScriptEngineManager
     * to implement its getEngineByExtension method.
     * @return The list of extensions.
     */
    public List<String> getExtensions() {
        return extentions;
    }

    /**
     * Returns an immutable list of mimetypes, associated with scripts that can be executed by the
     * engine. The list is used by the ScriptEngineManager class to implement its getEngineByMimetype
     * method.
     * @return The list of mime types.
     */
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    /**
     * Returns an immutable list of short names for the ScriptEngine, which may
     * be used to identify the ScriptEngine by the ScriptEngineManager.
     * @return Returns an immutable list of short names for the ScriptEngine, which may be
     *         used to identify the ScriptEngine by the ScriptEngineManager.
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * Returns the name of the scripting langauge supported by this ScriptEngine
     * @return The name of the supported language.
     */
    public String getLanguageName() {
        return NAME;
    }

    /**
     * Returns the version of the scripting language supported by this ScriptEngine.
     * @return The version of the supported language.
     */
    public String getLanguageVersion() {
        return VERSION;
    }

    /**
     * Returns the value of an attribute whose meaning may be implementation-specific.
     * Keys for which the value is defined in all implementations are:
     * <p/>
     * <ol>
     * <li>ScriptEngine.ENGINE
     * <li>ScriptEngine.ENGINE_VERSION
     * <li>ScriptEngine.NAME
     * <li>ScriptEngine.LANGUAGE
     * <li>ScriptEngine.LANGUAGE_VERSION
     * </ol>
     * The values for these keys are the Strings returned by getEngineName, getEngineVersion,
     * getName, getLanguageName and getLanguageVersion respectively.
     * <p/>
     * A reserved key, THREADING, whose value describes the behavior of the engine with respect
     * to concurrent execution of scripts and maintenance of state is also defined. These values
     * for the THREADING key are:
     * <p/>
     * <ol>
     * <li>null - The engine implementation is not thread safe, and cannot be used to execute scripts
     * concurrently on multiple threads.
     * <li>"MULTITHREADED" - The engine implementation is internally thread-safe and scripts may execute
     * concurrently although effects of script execution on one thread may be visible to scripts on other threads.
     * <li>"THREAD-ISOLATED" - The implementation satisfies the requirements of "MULTITHREADED", and also,
     * the engine maintains independent values for symbols in scripts executing on different threads.
     * <li>"STATELESS" - The implementation satisfies the requirements of "THREAD-ISOLATED". In addition,
     * script executions do not alter the mappings in the Bindings which is the engine scope of the ScriptEngine.
     * In particular, the keys in the Bindings and their associated values are the same before and after the
     * execution of the script.
     * <p/>
     * Implementations may define implementation-specific keys.
     * @param param The name of the parameter
     * @return The value for the given parameter. Returns null if no value is assigned to the key.
     */
    public Object getParameter(String param) {
        if (param.equals(ScriptEngine.ENGINE))
            return getEngineName();
        if (param.equals(ScriptEngine.ENGINE_VERSION))
            return getEngineVersion();
        if (param.equals(ScriptEngine.NAME))
            return getEngineName();
        if (param.equals(ScriptEngine.LANGUAGE))
            return getLanguageName();
        if (param.equals(ScriptEngine.LANGUAGE_VERSION))
            return getLanguageVersion();
        if (param.equals("THREADING"))
            return (isThreadSafe()) ? "MULTITHREADED" : null;

        return null;
    }

    /**
     * Returns a String which can be used to invoke a method of a Java object using the
     * syntax of the supported scripting language. For more information on the syntax
     * that MVEL uses, please see <a href="http://mvel.codehaus.org">http://mvel.codehaus.org</a>
     * @param object    The name representing the object whose method is to be invoked.
     *                  The name is the one used to create bindings using the put method
     *                  of ScriptEngine, the put method of an ENGINE_SCOPE  Bindings, or
     *                  the setAttribute method of ScriptContext. The identifier used in
     *                  scripts may be a decorated form of the specified one.
     * @param method    The name of the method to invoke
     * @param arguments The names of the arguments in the method call.
     * @return The String used to invoke the method in the syntax of the scripting language.
     */
    public String getMethodCallSyntax(String object, String method, String... arguments) {

        StringBuffer sb = new StringBuffer();
        if (object != null) {
            sb.append(object).append('.');
        }

        sb.append(method).append('(');

        if (arguments.length > 0) {
            sb.append(' ');
        }

        for (int i = 0; i < arguments.length; i++) {
            sb.append((arguments[i] == null) ? NULL_SYMBOL : arguments[i]).append(i < (arguments.length - 1) ? ", " : ' ');
        }

        sb.append(')');
        return sb.toString();
    }

    /**
     * Returns a String that can be used as a statement to display
     * the specified String using the syntax of the supported scripting
     * language.
     * @param message The String to be displayed by the returned statement.
     * @return The string used to display the String in the syntax of the scripting language.
     */
    public String getOutputStatement(String message) {
        return message;
    }

    /**
     * Returns A valid scripting language executable progam with given statements.
     * @param statements The statements to be executed. May be return values of calls
     *                   to the getMethodCallSyntax and getOutputStatement methods.
     * @return The Program that can be invoked by the engine.
     */
    public String getProgram(String... statements) {

        StringBuffer sb = new StringBuffer();

        for (String stmt : statements) {
            sb.append(stmt);
            if (!stmt.endsWith(STATEMENT_TERMINATOR))
                sb.append(STATEMENT_TERMINATOR);

            sb.append('\n');
        }

        // The purpose of this check is to remove the last
        // occurrence \n from the generated string.
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        } else {
            return "";
        }
    }

    /**
     * Returns an instance of the ScriptEngine associated with this ScriptEngineFactory.
     * A new ScriptEngine is generally returned, but implementations may pool, share or reuse engines.
     * In the case of MVFlex, a new instance is returned very time.
     * @return A new ScriptEngine instance.
     */
    public ScriptEngine getScriptEngine() {
        return new MVELScriptEngine(this);
    }

}
