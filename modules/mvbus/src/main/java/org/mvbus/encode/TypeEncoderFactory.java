package org.mvbus.encode;

import org.mvbus.encode.types.MapEncoder;

import java.util.Map;
import java.util.HashMap;

public class TypeEncoderFactory {
    private final static Map<Class, TypeEncoder> TYPE_ENCODERS = new HashMap<Class, TypeEncoder>();
    private final static Map<Class, Class> TTABLE = new HashMap<Class, Class>();

    static {
        // init default type encoders here
        TYPE_ENCODERS.put(Map.class, new MapEncoder());
        TTABLE.put(HashMap.class, Map.class);
    }

    public static boolean hasEncoder(Class type) {
         return TTABLE.containsKey(type) || TYPE_ENCODERS.containsKey(type);
    }

    public static TypeEncoder getEncoder(Class type) {
        if (TTABLE.containsKey(type)) {
            return TYPE_ENCODERS.get(TTABLE.get(type));
        }
        else {
            return TYPE_ENCODERS.get(type);
        }
    }

}
