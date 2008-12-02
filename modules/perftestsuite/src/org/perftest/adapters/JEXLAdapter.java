package org.perftest.adapters;

import org.perftest.Adapter;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;

import java.util.Map;


public class JEXLAdapter implements Adapter {
    private String expression;
    private Map<String, Object> variables;

    private Expression e;
    JexlContext jc;


    public JEXLAdapter(String expression, Map<String, Object> variables) {
        this.expression = expression;
        this.variables = variables;
    }

    public boolean runTestNoCache(int loop) {
        Expression e;

        for (int i = 0; i < loop; i++) {
            try {
                e = ExpressionFactory.createExpression(expression);

                // Create a context and add data
                JexlContext jc = JexlHelper.createContext();

                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    jc.getVars().put(entry.getKey(), entry.getValue());
                }
                //  jc.getVars().put("foo", new Foo() );

                e.evaluate(jc);

            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return true;
    }

    public boolean prepareCache() {
        try {
            e = ExpressionFactory.createExpression(expression);
            jc = JexlHelper.createContext();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                jc.getVars().put(entry.getKey(), entry.getValue());
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }


        return true;
    }

    public boolean runTestCache(int loop) {
        for (int i = 0; i < loop; i++) {
            try {
                e.evaluate(jc);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
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
