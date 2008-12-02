package org.perftest;

import org.perftest.profiles.*;
import org.perftest.tests.SimpleNestedProperty;
import org.perftest.tests.Arithmetic;
import org.perftest.tests.NestedMapAccess;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        TestEngine te = new TestEngine();
        te.setLoop(25000);
        te.setSample(5);

        te.addProfile(new OGNLProfile());
        te.addProfile(new JEXLProfile());
        te.addProfile(new JUELProfile());
        te.addProfile(new GroovyProfile());
        te.addProfile(new MVELProfile());

        te.addTestDefinition(new SimpleNestedProperty());
        te.addTestDefinition(new Arithmetic());
        te.addTestDefinition(new NestedMapAccess());

        te.run();
    }
}
