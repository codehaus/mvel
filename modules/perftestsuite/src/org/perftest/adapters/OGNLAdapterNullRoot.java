package org.perftest.adapters;

import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlContext;
import ognl.enhance.ExpressionAccessor;
import org.perftest.Adapter;

import java.util.Map;

public class OGNLAdapterNullRoot implements Adapter {
    private String expression;
    private Map variables;

    private int capabilities;

        private Object s;

    private OgnlContext ctx;
    private ExpressionAccessor a;

    public OGNLAdapterNullRoot(String expression, Map variables, int capabilities) {
        this.expression = expression;
        this.variables = variables;
        this.capabilities = capabilities;
    }

    public boolean runTestNoCache(int loop) {
        for (int i = 0; i < loop; i++) {
            try {
               Ognl.getValue(expression, variables);
            }
            catch (OgnlException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }


    public boolean prepareCache() {
        try {
            s = Ognl.parseExpression(expression);
        }
        catch (OgnlException e) {
            throw new RuntimeException(e);
        }
        return true;
    }


    public boolean runTestCache(int loop) {
        Object s;

        try {
            s = Ognl.parseExpression(expression);
        }
        catch (OgnlException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < loop; i++) {
            try {
                Ognl.getValue(s, variables);
            }
            catch (OgnlException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    public boolean prepareCompiled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean runTestCompiled(int loop) {
        return false;
    }
}