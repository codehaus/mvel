package org.mvbus.encode.engines.json;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
interface RewriteBridge<T> {
    void beginMap();

    void beginType(Class<?> type);

    void valueLhs(String lhs, boolean inMap);

    void valueRhs(String rhs, boolean inSeries, boolean inJsonList);

    void endType(String rhs, boolean inMap);

    <T> T getDecoded(Class<T> type);

    void beginList();

    void endList();
}
