package org.mvbus.encode;

import org.mvbus.encode.types.MapEncoder;
import org.mvbus.encode.types.ListEncoder;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class TypeEncoderFactory {
    private final static Map<Class, TypeEncoder> TYPE_ENCODERS = new HashMap<Class, TypeEncoder>();
    private final static Map<Class, Class> TTABLE = new HashMap<Class, Class>();

    static {
        // init default type encoders here
        TYPE_ENCODERS.put(Map.class, new MapEncoder());
        TYPE_ENCODERS.put(List.class, new ListEncoder());

        TTABLE.put(HashMap.class, Map.class);
        TTABLE.put(ArrayList.class, List.class);
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
