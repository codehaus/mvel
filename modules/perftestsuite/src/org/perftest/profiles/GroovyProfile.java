package org.perftest.profiles;

import org.perftest.Profile;
import org.perftest.Adapter;
import org.perftest.TestDefinition;
import org.perftest.adapters.GroovyAdapter;

public class GroovyProfile implements Profile {

    public String getName() {
        return "Groovy 1.5.7";
    }

    public int getCapabilities() {
        return 0;
    }

    public Adapter createTestAdapter(TestDefinition td) {
        return new GroovyAdapter(td.getExpression(), td.getVariables());
    }
}
