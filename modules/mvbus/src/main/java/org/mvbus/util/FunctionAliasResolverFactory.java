package org.mvbus.util;

import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;

import java.lang.reflect.Method;
import java.util.Set;

public class FunctionAliasResolverFactory implements VariableResolverFactory {
    private enum BuiltinFunctions {
        /**
         * Declare aliases for built-in functions here.
         */

        instantiate_obj("instantiate_obj", "org.mvbus.decode.DecodeTools.instantiate");

        public final String name;
        public final Method method;
        public final VariableResolver resolver;

        BuiltinFunctions(String functionName, String methodName) {
            this.name = functionName;
            this.method = MVEL.eval(methodName, Method.class);
            this.resolver = new VariableResolver() {
                public String getName() {
                    return name;
                }

                public Class getType() {
                    return Method.class;
                }

                public void setStaticType(Class type) {
                }

                public int getFlags() {
                    return 0;
                }

                public Object getValue() {
                    return method;
                }

                public void setValue(Object value) {
                }
            };
        }
    }


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
        return null;
    }

    public VariableResolverFactory setNextFactory(VariableResolverFactory resolverFactory) {
        throw new RuntimeException("Can't chain factories.");
    }

    public VariableResolver getVariableResolver(String name) {
        for (BuiltinFunctions bf : BuiltinFunctions.values()) {
            if (bf.name.equals(name)) {
                return bf.resolver;
            }
        }
        throw new PropertyAccessException("no such property or function: " + name);
    }

    public VariableResolver getIndexedVariableResolver(int index) {
        return null;
    }

    public boolean isTarget(String name) {
        for (BuiltinFunctions bf : BuiltinFunctions.values()) {
            if (bf.name.equals(name)) return true;
        }
        return false;
    }

    public boolean isResolveable(String name) {
        return isTarget(name);
    }

    public Set<String> getKnownVariables() {
        return null;
    }

    public int variableIndexOf(String name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isIndexedFactory() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
