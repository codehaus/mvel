package org.mvel.tests.bsf;

import junit.framework.TestCase;
import org.mvel.jsr223.MVELScriptEngineFactory;

import javax.script.*;
import java.io.StringReader;
import java.util.List;

/**
 * @author Richard L. Burton III
 * @version 1.0
 */
public class CompiledScriptTestCase extends TestCase {

    ScriptEngineFactory factory;

    ScriptEngine scriptEngine;

    protected void setUp() throws Exception {
        factory = new MVELScriptEngineFactory();
        scriptEngine = factory.getScriptEngine();
    }

    public void testCompileString() throws ScriptException {
        Compilable compilable = (Compilable) scriptEngine;

        CompiledScript compiledScript = compilable.compile("[1,2,3]");
        Object result = compiledScript.eval();
        assertTrue(result instanceof List);
    }

    public void testCompileReader() throws ScriptException {
        Compilable compilable = (Compilable) scriptEngine;

        StringReader reader = new StringReader("[1,2,3]");
        CompiledScript compiledScript = compilable.compile(reader);

        Object result = compiledScript.eval();
        assertTrue(result instanceof List);
    }

}
