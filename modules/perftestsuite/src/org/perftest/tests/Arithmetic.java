package org.perftest.tests;

import org.perftest.TestDefinition;

import java.util.Map;
import java.util.HashMap;

public class Arithmetic implements TestDefinition {
    public String getExpression() {
        return "x + y * z";
    }

    public int getRequiredCapabilities() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getRootObject() {
        return null;
    }

    public Map getVariables() {
        Map vars = new HashMap();
        vars.put("x", 10);
        vars.put("y", 20);
        vars.put("z", 30);

        return vars;
    }
}
