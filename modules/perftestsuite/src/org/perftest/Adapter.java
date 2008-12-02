package org.perftest;

public interface Adapter {
    public boolean runTestNoCache(int loop);

    public boolean prepareCache();
    public boolean runTestCache(int loop);

    public boolean prepareCompiled();
    public boolean runTestCompiled(int loop);
}
