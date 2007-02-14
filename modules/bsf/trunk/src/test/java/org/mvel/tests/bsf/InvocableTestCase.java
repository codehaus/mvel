package org.mvel.tests.bsf;

import junit.framework.TestCase;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import javax.script.ScriptException;

import org.mvel.bsf.MVELScriptEngineFactory;
import org.mvel.tests.bsf.model.PersonBean;

/**
 * @author Richard L. Burton III
 * @version 1.0
 */
public class InvocableTestCase extends TestCase {

    ScriptEngineFactory factory;

    ScriptEngine scriptEngine;

    protected void setUp() throws Exception {
        factory = new MVELScriptEngineFactory();
        scriptEngine = factory.getScriptEngine();
    }

    public void testGetInterface() throws NoSuchMethodException, ScriptException {

        Invocable invocable = (Invocable) scriptEngine;

        try {
            invocable.getInterface(ScriptEngine.class);
        } catch (Exception iae) {
            assertTrue(iae instanceof IllegalArgumentException);
        }

        try {
            invocable.getInterface(null, ScriptEngine.class);
        } catch (Exception iae) {
            assertTrue(iae instanceof IllegalArgumentException);
        }
    }

    public void testInvokeFunction() {

        Invocable invocable = (Invocable) scriptEngine;

        try {
            invocable.invokeFunction("helloWorld", "Richard", "Burton");
        } catch (Exception se) {
            assertTrue(se instanceof ScriptException);
        }
    }

    public void testInvokeMethod() throws NoSuchMethodException, ScriptException {
        Invocable invocable = (Invocable) scriptEngine;

        PersonBean person = new PersonBean();
        Object result = invocable.invokeMethod(person, "sayHelloWorld");
        assertEquals(PersonBean.HELLO_WORLD, result);

        result = invocable.invokeMethod(person, "sayHelloWorld", "Richard");
        assertEquals(PersonBean.HELLO_WORLD + " Richard", result);

        result = invocable.invokeMethod(person, "sayHelloWorld", 27);
        assertEquals(PersonBean.HELLO_WORLD + " age: " + 27, result);

        // TODO: How should this be handled?
        // TODO: This is like calling invocable.invokeMethod(person, "sayHelloWorld");
        result = invocable.invokeMethod(person, "sayHelloWorld", null);
        assertEquals(PersonBean.HELLO_WORLD, result);

        try {
            person = new PersonBean();
            invocable.invokeMethod(person, "sayBrokenLikeStruts1x");
        } catch (Exception iae) {
            assertTrue(iae instanceof NoSuchMethodException);
        }
    }

}
