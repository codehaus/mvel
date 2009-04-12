package org.mvbus.encode.engines.json;

import org.mvel2.MVEL;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * A small pluggable fragment generator that is used by the Json decoder
 * to generate Maps and Lists.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class MapAndListBridge<T> implements RewriteBridge<T> {
    private final Class<T> type;

    private Map<String, Object> root = new HashMap<String, Object>();

    // State variables.
    private String lhs;

    // Value is either a Map or a List
    private Map<String, Object> currentMap;
    private static final String ROOT = "root";

    public MapAndListBridge(Class<T> type) {
        this.type = type;
    }

    public void beginMap() {
        if (null == currentMap) {
            root.put(ROOT, currentMap = new HashMap<String, Object>());
        }

        // May want to do an rhs put if a current map already exists...
    }

    public void beginType(Class<?> type) {
        if (null == currentMap) {
            root.put(ROOT, currentMap = new HashMap<String, Object>());
        }
    }

    public void valueLhs(String lhs, boolean inMap) {
        this.lhs = lhs;
    }

    public void valueRhs(String rhs, boolean inSeries, boolean inJsonList) {
        if (lhs != null) {

            // Append to a list rather than a simple put
            if (inJsonList) {
                List<Object> list = (List<Object>) currentMap.get(lhs);

                if (null == list) {
                    list = new ArrayList<Object>();
                    currentMap.put(lhs, list);
                }

                // Should we attempt to coerce this value?
                list.add(MVEL.eval(rhs));


            } else {
                // This is an empty list, so will be taken care of elsewhere.
                if (!"".equals(rhs))
                    // This is a put. Coerce the right hand side value using MVEL (assumes it is a literal).
                    currentMap.put(lhs, MVEL.eval(rhs));
                lhs = null;
            }
        }

    }

    public void endType(String rhs, boolean inMap) {
        valueRhs(rhs, false, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T getDecoded(Class<T> type) {
        System.out.println(root.get(ROOT));

        return (T) root.get(ROOT);
    }

    public void beginList() {
        if (null != lhs) {
            currentMap.put(lhs, new ArrayList<Object>());
        }
    }

    public void endList() {
    }
}