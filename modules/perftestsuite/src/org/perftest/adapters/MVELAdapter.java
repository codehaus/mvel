package org.perftest.adapters;

import org.mvel2.MVEL;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.optimizers.OptimizerFactory;
import org.perftest.Adapter;

import java.util.Map;

public class MVELAdapter implements Adapter {
    private String expression;
    private Map variables;
    private MapVariableResolverFactory factory;
    private Object root;

    private ExecutableStatement s;

    public MVELAdapter(String expression, Object root, Map variables) {
        this.expression = expression;
        this.root = root;
        this.factory = new MapVariableResolverFactory(this.variables = variables);
    }

    public boolean runTestNoCache(int loop) {
        for (int i = 0; i < loop; i++) {
            MVEL.eval(expression, root, variables);
        }
        return true;
    }


    public boolean prepareCache() {
        OptimizerFactory.setDefaultOptimizer("reflective");
        s = (ExecutableStatement) MVEL.compileExpression(expression);
        return true;
    }

    public boolean runTestCache(int loop) {
        for (int i = 0; i < loop; i++) {
            s.getValue(root, root, factory);
        }
        return true;
    }


    public boolean prepareCompiled() {
        OptimizerFactory.setDefaultOptimizer("ASM");
        s = (ExecutableStatement) MVEL.compileExpression(expression);
        return true;
    }

    public boolean runTestCompiled(int loop) {
        for (int i = 0; i < loop; i++) {
            s.getValue(root, root, factory);
        }
        return true;
    }
}
