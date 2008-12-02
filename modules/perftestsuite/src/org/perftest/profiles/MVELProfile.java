package org.perftest.profiles;

import org.perftest.Profile;
import org.perftest.Adapter;
import org.perftest.TestDefinition;
import org.perftest.adapters.MVELAdapter;
import static org.perftest.Capabilities.*;

import java.util.Map;

public class MVELProfile implements Profile {
    public String getName() {
        return "MVEL 2.0";
    }

    public int getCapabilities() {
        return CACHING | VARIABLES | COMPILING | COMPILE_W_CONTEXT | COMPILE_W_VARS;
    }

    public Adapter createTestAdapter(TestDefinition td) {
         return new MVELAdapter(td.getExpression(), td.getRootObject(), td.getVariables());
    }
}
