package org.mvbus.encode.engines.json;

import org.mvel2.util.StringAppender;
import org.mvel2.MVEL;

import java.util.HashMap;

/**
 * A small pluggable fragment generator that is used by the Json decoder
 * to generate mvel script.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class MvelBridge<T> implements RewriteBridge<T> {
    private final StringAppender mvel = new StringAppender();

    public MvelBridge(Class<T> type) {

    }

    public void beginMap() {
        mvel.append("[");
    }

    public void beginType(Class<?> type) {
        mvel.append(" org.mvbus.decode.DecodeTools.instantiate( ");
        mvel.append(type.getName());
        mvel.append(" ).{ ");
    }

    public void valueLhs(String lhs, boolean inMap) {
        mvel.append(lhs);
        mvel.append(inMap ? ':' : '=');
    }

    public void valueRhs(String rhs, boolean inSeries, boolean inJsonList) {
        mvel.append(rhs);

        if (inSeries)
            mvel.append(',');
    }

    public void endType(String rhs, boolean inMap) {
        mvel.append(rhs);
        mvel.append(inMap ? "]" : " } ");
    }

    @SuppressWarnings("unchecked")
    public <T> T getDecoded(Class<T> type) {
       System.out.println(mvel.toString());
        
        return (T) MVEL.eval(mvel.toString(), new HashMap<String, Object>());
    }

    public void beginList() {
        mvel.append("[");
    }

    public void endList() {
        mvel.append("]");
    }
}
