package org.perftest.profiles;

import org.perftest.Profile;
import org.perftest.Adapter;
import org.perftest.TestDefinition;
import org.perftest.adapters.JUELAdapter;


public class JUELProfile implements Profile {
    public String getName() {
        return "JUEL 2.1.0";
    }

    public int getCapabilities() {
        return 0;  
    }

    public Adapter createTestAdapter(TestDefinition td) {
        return new JUELAdapter(td.getExpression(), td.getVariables());
    }
}
