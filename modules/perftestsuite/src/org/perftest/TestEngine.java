package org.perftest;

import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestEngine {
    private List<TestDefinition> testDefinitions = new ArrayList<TestDefinition>();
    private List<Profile> loadedProfiles = new ArrayList<Profile>();
    private List<ResultGroup> results;
    private int capabilityFilter = -1;
    private int loop = 10000;
    private int sample = 3;

    public void run() {
        results = new LinkedList<ResultGroup>();
        ResultGroup group;
        Result result;

        long start;
        for (TestDefinition td : testDefinitions) {
            results.add(group = new ResultGroup(td, loop));

            System.out.println("Test Group: " + td.getExpression() + " (iterations: " + loop + ")");
            for (Profile p : loadedProfiles) {
                Adapter a = p.createTestAdapter(td);

                group.addResult(result = new Result("No Cache", p));

                for (int i = 0; i < sample; i++) {
                    start = currentTimeMillis();
                    result.setSupported(a.runTestNoCache(loop));
                    result.addMeasurement(currentTimeMillis() - start);
                }

                System.out.println(result.toString());

                group.addResult(result = new Result("Cached", p));

                if (a.prepareCache()) {
                    for (int i = 0; i < sample; i++) {
                        start = currentTimeMillis();
                        a.runTestCache(loop);
                        result.addMeasurement(currentTimeMillis() - start);
                    }
                }
                else {
                    result.setSupported(false);
                }

                System.out.println(result.toString());

                group.addResult(result = new Result("Compiled", p));

                if (a.prepareCompiled()) {
                    for (int i = 0; i < sample; i++) {
                        start = currentTimeMillis();
                        a.runTestCompiled(loop);
                        result.addMeasurement(currentTimeMillis() - start);
                    }
                }
                else {
                    result.setSupported(false);
                }

                System.out.println(result.toString());

            }
            System.out.println();
            //     System.out.println(group.toString());
        }

//        for (ResultGroup r : results) {
//            System.out.println(r.toString());
//
//        }
    }

    public void addTestDefinition(TestDefinition td) {
        this.testDefinitions.add(td);
    }

    public void addProfile(Profile profile) {
        this.loadedProfiles.add(profile);
    }

    public int getCapabilityFilter() {
        return capabilityFilter;
    }

    public void setCapabilityFilter(int capabilityFilter) {
        this.capabilityFilter = capabilityFilter;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    public int getSample() {
        return sample;
    }

    public void setSample(int sample) {
        this.sample = sample;
    }

    public List<ResultGroup> getResults() {
        return results;
    }

    public List<Profile> getLoadedProfiles() {
        return loadedProfiles;
    }

    public List<TestDefinition> getTestDefinitions() {
        return testDefinitions;
    }
}
