package org.mvbus.encode.types;

import org.mvbus.encode.Encoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for retrieving standard encoders built into MVEL.
 */
public class Encoders {
    private Encoders() {
    }

    private final static Map<Class<?>, Encoder<?>> TYPE_ENCODERS
            = new HashMap<Class<?>, Encoder<?>>();
//    private final static Map<Class<?>, Class<?>> TTABLE = new HashMap<Class<?>, Class<?>>();

    static {
        // init default type encoders here
        TYPE_ENCODERS.put(Map.class, new MapEncoder());
        TYPE_ENCODERS.put(List.class, new ListEncoder());
    }

//    public static boolean canEncode(Class<?> clazz) {
//        if (clazz == null) return false;
//        if (!TYPE_ENCODERS.containsKey(clazz)) {
//            do {
//                for (Class c : clazz.getInterfaces()) {
//                    if (TYPE_ENCODERS.containsKey(c)) {
//                        TYPE_ENCODERS.put(clazz, TYPE_ENCODERS.get(c));
//                        return true;
//                    }
//                }
//            }
//            while ((clazz = clazz.getSuperclass()) != null && clazz != Object.class);
//            return false;
//        }
//        else {
//            return true;
//        }
//    }
//
//    public static Encoder getEncoder(Class<?> type) {
//        return TYPE_ENCODERS.get(type);
//    }

    /**
     * @return A map of built-in encoders for common types such as {@link List}.
     */
    public static Map<Class<?>, Encoder<?>> all() {
        // Return a defensive copy, so our built-in types remain immutable and users can still
        // override default encoder configuration on a per-bus instance basis.
        return new HashMap<Class<?>, Encoder<?>>(TYPE_ENCODERS);
    }
}
