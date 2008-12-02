package org.perftest;

import java.util.Map;

public interface Profile {
    public String getName();
    public int getCapabilities();

    public Adapter createTestAdapter(TestDefinition td);
}
