package org.mvbus.util;

import org.mvbus.decode.DecodeTools;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.PropertyAccessException;

import java.lang.reflect.Method;
import java.util.Set;

public class FunctionAliasResolverFactory implements VariableResolverFactory {
    private static final Method instantiateMethod;

    static {
        try {
            instantiateMethod = DecodeTools.class.getMethod("instantiate", Class.class);
        }
        catch (Exception e) {
            throw new RuntimeException("This shouldn't happen", e);
        }
    }

    private static final VariableResolver instantiationFunction = new VariableResolver() {
        public String getName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Class getType() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setStaticType(Class type) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getFlags() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getValue() {
              return instantiateMethod;
        }

        public void setValue(Object value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public VariableResolver createVariable(String name, Object value) {
        throw new RuntimeException("Can't declare variables");
    }

    public VariableResolver createIndexedVariable(int index, String name, Object value) {
        throw new RuntimeException("Can't declare variables");
    }

    public VariableResolver createVariable(String name, Object value, Class<?> type) {
        throw new RuntimeException("Can't declare variables");
    }

    public VariableResolver createIndexedVariable(int index, String name, Object value, Class<?> typee) {
        throw new RuntimeException("Can't declare variables");
    }

    public VariableResolver setIndexedVariableResolver(int index, VariableResolver variableResolver) {
        throw new RuntimeException("Can't declare variables");
    }

    public VariableResolverFactory getNextFactory() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public VariableResolverFactory setNextFactory(VariableResolverFactory resolverFactory) {
        throw new RuntimeException("Can't chain factories.");
    }

    public VariableResolver getVariableResolver(String name) {
        if (name.equals("instantiate_obj")) {
            return instantiationFunction;
        }
        else {
         throw new PropertyAccessException("no such function: " + name);
        }
    }

    public VariableResolver getIndexedVariableResolver(int index) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTarget(String name) {
        return "instantiate_obj".equals(name);
    }

    public boolean isResolveable(String name) {
        return "instantiate_obj".equals(name);
    }

    public Set<String> getKnownVariables() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int variableIndexOf(String name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isIndexedFactory() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
