package org.perftest.adapters;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.perftest.Adapter;

import java.util.Map;

public class GroovyAdapter implements Adapter {
    private String expression;
    private Map<String, Object> variables;

    Script script;

    public GroovyAdapter(String expression, Map<String, Object> variables) {
        this.expression = expression;
        this.variables = variables;
    }

    public boolean runTestNoCache(int loop) {
        return false;
    }

    public boolean prepareCache() {
        Binding binding = new Binding();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue());
        }
        GroovyShell shell = new GroovyShell(binding);
        script = shell.parse(expression);
        return true;
    }

    public boolean runTestCache(int loop) {
        for (int i = 0; i < loop; i++) {
            script.run();
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
