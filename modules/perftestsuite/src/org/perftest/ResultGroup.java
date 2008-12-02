package org.perftest;

import org.mvel2.util.StringAppender;

import java.util.List;
import java.util.LinkedList;

public class ResultGroup {
    private TestDefinition testDefinition;
    private List<Result> results = new LinkedList<Result>();
    private long loops;

    public ResultGroup(TestDefinition testDefinition, long loops) {
        this.testDefinition = testDefinition;
        this.loops = loops;
    }

    public void addResult(Result r) {
        results.add(r);
    }

    public TestDefinition getTestDefinition() {
        return testDefinition;
    }

    public void setTestDefinition(TestDefinition testDefinition) {
        this.testDefinition = testDefinition;
    }

    public long getLoops() {
        return loops;
    }

    public void setLoops(long loops) {
        this.loops = loops;
    }

    public String toString() {
        StringAppender appender = new StringAppender("====\n");
        appender.append("Test Group: " + testDefinition.getExpression() + " (iterations:" + loops +")").append("\n");

        for (Result r : results) {
            appender.append(r.toString()).append("\n");
        }

        return appender.append("----\n").toString();

    }
}
