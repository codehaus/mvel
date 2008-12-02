package org.perftest.adapters;

import org.perftest.Adapter;

import javax.el.ValueExpression;
import javax.el.ExpressionFactory;
import java.util.Map;

public class JUELAdapter implements Adapter {
    private String expression;
    private Map<String, Object> variables;

    de.odysseus.el.util.SimpleContext context;
    private ValueExpression ve;

    public JUELAdapter(String expression, Map variables) {
        this.expression = "${" + expression + "}";
        this.variables = variables;
    }

    public boolean runTestNoCache(int loop) {
        for (int i = 0; i < loop; i++) {
            ExpressionFactory factory = new de.odysseus.el.ExpressionFactoryImpl();
            de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();

            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.setVariable(entry.getKey(), factory.createValueExpression(entry.getValue(), entry.getValue().getClass()));
            }

            factory.createValueExpression(context, expression, Object.class).getValue(context);
        }

        return true;
    }

    public boolean prepareCache() {
        ExpressionFactory factory = new de.odysseus.el.ExpressionFactoryImpl();
        context = new de.odysseus.el.util.SimpleContext();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            context.setVariable(entry.getKey(), factory.createValueExpression(entry.getValue(), entry.getValue().getClass()));
        }

        ve = factory.createValueExpression(context, expression, Object.class);

        return true;
    }

    public boolean runTestCache(int loop) {
        for (int i = 0; i < loop; i++) {
            ve.getValue(context);
        }

        return true;
    }

    public boolean prepareCompiled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean runTestCompiled(int loop) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
