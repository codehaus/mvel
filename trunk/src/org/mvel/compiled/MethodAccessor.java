package org.mvel.compiled;

import org.mvel.AccessorNode;
import org.mvel.DataConversion;
import org.mvel.ExpressionParser;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

public class MethodAccessor implements AccessorNode {
    private AccessorNode nextNode;

    private Method method;
    private Class[] parameterTypes;
    private Serializable[] compiledParameters;
    private boolean coercionNeeded = false;

    public Object getValue(Object ctx, Object elCtx, Map vars) throws Exception {
        if (!coercionNeeded) {
            try {
                if (nextNode != null) {
                    return nextNode.getValue(
                            method.invoke(ctx, ExpressionParser.executeAllExpression(compiledParameters, elCtx, vars))
                            , elCtx, vars);
                }
                else {
                    return method.invoke(ctx, ExpressionParser.executeAllExpression(compiledParameters, elCtx, vars));
                }
            }
            catch (IllegalArgumentException e) {
                coercionNeeded = true;
                return getValue(ctx, elCtx, vars);
            }

        }
        else {
            if (nextNode != null) {
                return nextNode.getValue(
                        method.invoke(ctx, executeAndCoerce(compiledParameters, parameterTypes, elCtx, vars)), elCtx, vars);
            }
            else {
                return method.invoke(ctx, executeAndCoerce(compiledParameters, parameterTypes, elCtx, vars));
            }
        }
    }

    private static Object[] executeAndCoerce
            (Serializable[] compiled, Class[] target, Object
                    elCtx, Map
                    vars) {
        Object[] values = new Object[compiled.length];
        for (int i = 0; i < compiled.length; i++) {
            values[i] = DataConversion.convert(ExpressionParser.executeExpression(compiled[i], elCtx, vars), target[i]);
        }
        return values;
    }

    public Method getMethod
            () {
        return method;
    }

    public void setMethod
            (Method
                    method) {
        this.method = method;
        this.parameterTypes = this.method.getParameterTypes();
    }


    public Serializable[] getCompiledParameters
            () {
        return compiledParameters;
    }

    public void setCompiledParameters
            (Serializable[] compiledParameters) {
        this.compiledParameters = compiledParameters;
    }

    public MethodAccessor() {
    }

    public AccessorNode getNextNode
            () {
        return nextNode;
    }

    public AccessorNode setNextNode
            (AccessorNode
                    nextNode) {
        return this.nextNode = nextNode;
    }
}


