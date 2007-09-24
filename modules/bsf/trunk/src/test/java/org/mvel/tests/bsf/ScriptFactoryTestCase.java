package org.mvel.tests.bsf;

import junit.framework.TestCase;

import javax.script.*;

import org.mvel.jsr223.MVELScriptEngineFactory;
import org.mvel.MVEL;
import org.mvel.tests.bsf.model.PersonBean;

/**
 * @author Richard L. Burton III
 * @version 1.0
 */
public class ScriptFactoryTestCase extends TestCase {

    ScriptEngineFactory factory;

    ScriptEngine scriptEngine;

    protected void setUp() throws Exception {
        factory = new MVELScriptEngineFactory();
        scriptEngine = factory.getScriptEngine();
    }

    public void testGetProgram() {
        String result = factory.getProgram("name = 'richard'", "age=2");
        assertEquals("name = 'richard';\nage=2;", result);

        result = factory.getProgram("name = 'richard';", "age=2;");
        assertEquals("name = 'richard';\nage=2;", result);

    }

    public void testGetProperty() {

        Object result = factory.getParameter(ScriptEngine.NAME);
        assertEquals(MVEL.NAME, result);

        result = factory.getParameter(ScriptEngine.ENGINE_VERSION);
        assertEquals(MVEL.VERSION, result);

    }

    public void testMethodCallSyntax() throws ScriptException {

        Bindings bindings = scriptEngine.createBindings();
        String syntax = factory.getMethodCallSyntax("person", "sayHelloWorld");
        PersonBean person = new PersonBean();
        bindings.put("person", person);

        Object result = scriptEngine.eval(syntax, bindings);
        assertEquals(PersonBean.HELLO_WORLD, result);
    }

}
