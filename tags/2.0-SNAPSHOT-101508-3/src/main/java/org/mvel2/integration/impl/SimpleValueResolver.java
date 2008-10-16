package org.mvel2.integration.impl;

import org.mvel2.integration.VariableResolver;

public class SimpleValueResolver implements VariableResolver {
    private Object value;

    public SimpleValueResolver(Object value) {
        this.value = value;
    }

    public String getName() {
        return null;
    }

    public Class getType() {
        return Object.class;
    }

    public void setStaticType(Class type) {
    }

    public int getFlags() {
        return 0;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
