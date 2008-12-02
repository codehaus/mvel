package org.perftest.tests;

import org.perftest.TestDefinition;
import org.perftest.Capabilities;
import org.perftest.tests.domain.Foo;
import org.perftest.tests.domain.Bar;

import java.util.Map;
import java.util.HashMap;

public class SimpleNestedProperty implements TestDefinition {
    public String getExpression() {
        return "foo.bar.name";
    }

    public int getRequiredCapabilities() {
        return Capabilities.VARIABLES;
    }

    public Object getRootObject() {
        return null;
    }

    public Map getVariables() {
        Map map = new HashMap();

        Foo foo = new Foo();
        foo.setBar(new Bar());

        map.put("foo", foo);

        return map;
    }
}
