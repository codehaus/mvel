package org.mvel.jsr223;

import org.mvel.ExecutableStatement;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.integration.VariableResolverFactory;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.ScriptEngine;
import java.util.Map;

/**
 * The optional interface <code>CompiledScript</code> is implemented by MVEL's
 * JSR 223 extention jar. If the ASM dependency is in the class path, then this
 * ExecutableStatement will truly be compiled into a Class on the fly. Otherwise,
 * this each invocation of eval(..) will parse up the expression provided. For more
 * information, check out <a href="http://mvel.codehaus.org">http://mvel.codehaus.org<a>.
 * @author Richard L. Burton III
 * @version 1.0
 * @since MVEL 1.2
 */
public class MVELCompiledScript extends CompiledScript {

    /**
     * The ScriptEngine that created the CompiledScript.
     */
    private ScriptEngine scriptEngine;

    /**
     * The compiled ExecutableStatement object.
     */
    private ExecutableStatement executableStatement;

    /**
     * Constructs a CompiledScript with reference to the ScriptEngine that
     * created it.
     * @param scriptEngine        The ScriptEngine that created the CompiledScript.
     * @param executableStatement The ExecutableStatement to be executed.
     */
    public MVELCompiledScript(ScriptEngine scriptEngine, ExecutableStatement executableStatement) {
        this.scriptEngine = scriptEngine;
        this.executableStatement = executableStatement;
    }

    /**
     * Executes the program stored in this CompiledScript object.
     * @param scriptContext A ScriptContext that is used in the same way as the
     *                      ScriptContext passed to the eval methods of ScriptEngine.
     * @return The value returned by the script execution, if any. Should return
     *         null  if no value is returned by the script execution.
     * @throws ScriptException      if an error occurs.
     * @throws NullPointerException if context is null.
     */
    public Object eval(ScriptContext scriptContext) throws ScriptException {
        Map<String, Object> scriptContextEngineView = new MVELScriptContextMap(scriptContext);
        VariableResolverFactory variableResolverFactory = new MapVariableResolverFactory(scriptContextEngineView);
        return executableStatement.getValue(scriptContext, variableResolverFactory);
    }

    /**
     * Returns the ScriptEngine wbose compile method created this CompiledScript. The
     * CompiledScript will execute in this engine.
     * @return The ScriptEngine that created this CompiledScript
     */
    public ScriptEngine getEngine() {
        return scriptEngine;
    }

}
