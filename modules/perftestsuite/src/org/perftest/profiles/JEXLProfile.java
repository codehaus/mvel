package org.perftest.profiles;

import org.perftest.Profile;
import org.perftest.Adapter;
import org.perftest.TestDefinition;
import org.perftest.adapters.JEXLAdapter;


public class JEXLProfile implements Profile {
    public String getName() {
        return "JEXL 1.1";
    }

    public int getCapabilities() {
        return 0;
    }

    public Adapter createTestAdapter(TestDefinition td) {
        return new JEXLAdapter(td.getExpression(), td.getVariables());
    }
}
