package org.perftest.adapters;

import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.enhance.ExpressionAccessor;
import org.perftest.Adapter;
import org.perftest.Capabilities;

import java.util.Map;

public class OGNLAdapter implements Adapter {
    private String expression;
    private Map variables;
    private Object root;
    private int capabilities;

    private Object s;

    private OgnlContext ctx;
    private ExpressionAccessor a;

    public OGNLAdapter(String expression, Object root, Map variables, int capabilities) {
        this.expression = expression;
        this.variables = variables;
        this.root = root;
        this.capabilities = capabilities;
    }

    public boolean runTestNoCache(int loop) {
        for (int i = 0; i < loop; i++) {
            try {
                Object o = Ognl.getValue(expression, variables, root);
                System.out.println(o);
            }
            catch (OgnlException e) {
                System.out.println(expression + ":" + variables + ":" + root);
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

        for (int i = 0; i < loop; i++) {
            try {
                Ognl.getValue(s, variables, root);
            }
            catch (OgnlException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    public boolean prepareCompiled() {
        if ((capabilities & Capabilities.COMPILE_W_VARS) != 0) return false;

        ctx = new OgnlContext();
        Node n;
        ExpressionAccessor a;

        try {
            n = Ognl.compileExpression(ctx, root, expression);
            a = n.getAccessor();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public boolean runTestCompiled(int loop) {

        for (int i = 0; i < loop; i++) {
            a.get(ctx, root);
        }

        return true;
    }
}
