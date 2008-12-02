package org.perftest;

import org.mvel2.util.StringAppender;

import java.util.List;
import java.util.LinkedList;

public class Result {
    private String name;
    private Profile profile;
    private List<Long> measurements = new LinkedList<Long>();
    private boolean supported = true;

    public Result(String name, Profile profile) {
        this.name = name;
        this.profile = profile;
    }

    public void addMeasurement(long time) {
        measurements.add(time);
    }

    public long getAverage() {
        long sum = 0;
        for (Long l : measurements) {
            sum += l;
        }
        return sum / measurements.size();
    }

    public Profile getProfile() {
        return profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String toString() {
        StringAppender appender = new StringAppender();
        appender.append(profile.getName() + " [" + name + "]: ");
        if (supported) {
            appender.append("Average ms: " + getAverage());
        }
        else {
            appender.append("Not Supported");
        }
        return appender.toString();
    }
}
