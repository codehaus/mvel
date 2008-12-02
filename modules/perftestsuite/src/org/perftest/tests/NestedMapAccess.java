package org.perftest.tests;

import org.perftest.TestDefinition;

import java.util.Map;
import java.util.HashMap;

public class NestedMapAccess implements TestDefinition {
    public String getExpression() {
        return "foo['bar']['something']";
    }

    public int getRequiredCapabilities() {
        return 0;
    }

    public Object getRootObject() {
        return null;  
    }

    public Map getVariables() {
        Map map = new HashMap();
        Map foo = new HashMap();
        map.put("foo", foo);
        Map bar = new HashMap();
        foo.put("bar", bar);

        bar.put("something", "Hello!");

        return map;
    }
}
