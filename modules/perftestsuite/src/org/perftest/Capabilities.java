package org.perftest;

public interface Capabilities {
    public static final int VARIABLES = 1;
    public static final int CACHING = 1 << 1;
    public static final int COMPILING = 1 << 2;
    public static final int COMPILE_W_CONTEXT = 1 << 3;
    public static final int COMPILE_W_VARS = 1 << 4;
}
