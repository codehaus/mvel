package org.mvel.tests.bsf;

import junit.framework.TestCase;
import org.mvel.bsf.MVELScriptEngineFactory;
import org.mvel.tests.bsf.model.PersonBean;

import javax.script.*;
import java.util.List;
import java.util.Map;
import java.io.Reader;
import java.io.StringReader;

/**
 * This TestCase is used to test the ScriptEngine object and
 * the various methods.
 * @author Richard L. Burton III
 * @version 1.0
 */
public class MVELScriptEngineTestCase extends TestCase {

    ScriptEngineFactory factory;

    ScriptEngine scriptEngine;

    protected void setUp() throws Exception {
        factory = new MVELScriptEngineFactory();
        scriptEngine = factory.getScriptEngine();
    }

    public void testNoArgumentEval() throws ScriptException {

        Object result = scriptEngine.eval("[1,2,2]");
        assertTrue(result instanceof List);

        result = scriptEngine.eval("['Mike' : 'Sarah', 'Foo' : 'Bar']");
        assertTrue(result instanceof Map);

        result = scriptEngine.eval("{'Hello', 'Goodbye'}");
        assertTrue(result instanceof Object[]);

        result = scriptEngine.eval("1==1");
        assertEquals(result, Boolean.TRUE);

        Reader reader = new StringReader("[1,2,2]");
        result = scriptEngine.eval(reader);
        assertTrue(result instanceof List);

        reader = new StringReader("['Mike' : 'Sarah', 'Foo' : 'Bar']");
        result = scriptEngine.eval(reader);
        assertTrue(result instanceof Map);

        reader = new StringReader("{'Hello', 'Goodbye'}");
        result = scriptEngine.eval(reader);
        assertTrue(result instanceof Object[]);

    }

    public void testScriptBindings() throws ScriptException {

        ScriptContext context = scriptEngine.getContext();
        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

        bindings.put("name", "Burton");
        Object result = scriptEngine.eval("name = 'richard'", context);
        assertEquals(bindings.get("name"), "richard");
        assertEquals("richard", result);

        PersonBean person = new PersonBean();
        person.setName("Richard");
        bindings.put("person", person);

        result = scriptEngine.eval("person.setName('Burton')");
        assertNull(result);
        assertEquals("Burton", person.getName());

    }

    public void testEvalWithNulls() throws ScriptException {

        ScriptContext context = null;
        Bindings bindings = null;
        String expression = null;
        Reader reader = null;

        try {
            scriptEngine.eval("[1,2]", context);
        } catch (Exception npe) {
            assertTrue(npe instanceof NullPointerException);
        }

        try {
            scriptEngine.eval("[1,2]", bindings);
        } catch (Exception npe) {
            assertTrue(npe instanceof NullPointerException);
        }

        try {
            scriptEngine.eval(expression, scriptEngine.getContext());
        } catch (Exception npe) {
            assertTrue(npe instanceof NullPointerException);
        }

        try {
            scriptEngine.eval(reader, scriptEngine.getContext());
        } catch (Exception npe) {
            assertTrue(npe instanceof NullPointerException);
        }

    }


}
