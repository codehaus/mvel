package org.perftest;

import java.util.Map;

public interface TestDefinition {
    public String getExpression();
    public int getRequiredCapabilities();
    public Object getRootObject();
    public Map getVariables();
}
