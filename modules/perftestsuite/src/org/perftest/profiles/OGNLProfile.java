package org.perftest.profiles;

import org.perftest.Adapter;
import static org.perftest.Capabilities.*;
import org.perftest.Profile;
import org.perftest.TestDefinition;
import org.perftest.adapters.OGNLAdapter;
import org.perftest.adapters.OGNLAdapterNullRoot;

public class OGNLProfile implements Profile {
    public String getName() {
        return "OGNL 2.7.2";
    }

    public int getCapabilities() {
        return CACHING | VARIABLES | COMPILING | COMPILE_W_CONTEXT;
    }

    public Adapter createTestAdapter(TestDefinition td) {
        if (td.getRootObject() == null) {
            return new OGNLAdapterNullRoot(td.getExpression(), td.getVariables(), td.getRequiredCapabilities());
        }
        else {
            return new OGNLAdapter(td.getExpression(), td.getRootObject(), td.getVariables(), td.getRequiredCapabilities());
        }
    }
}
