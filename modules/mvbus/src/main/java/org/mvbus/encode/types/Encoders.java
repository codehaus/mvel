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
    private final static Map<Class<?>, Class<?>> TTABLE = new HashMap<Class<?>, Class<?>>();

    static {
        // init default type encoders here
        TYPE_ENCODERS.put(Map.class, new MapEncoder());
        TYPE_ENCODERS.put(List.class, new ListEncoder());

        // TODO(dhanji): Need a more robust mechanism for detecting subtypes.
        // Consider super crawl & cache instead.
        TTABLE.put(HashMap.class, Map.class);
        TTABLE.put(ArrayList.class, List.class);
    }

    public static boolean canEncode(Class<?> type) {
        return TYPE_ENCODERS.containsKey(type)|| TTABLE.containsKey(type);
    }

    public static Encoder getEncoder(Class<?> type) {
        if (TTABLE.containsKey(type)) {
            return TYPE_ENCODERS.get(TTABLE.get(type));
        } else {
            return TYPE_ENCODERS.get(type);
        }
    }

    /**
     * @return A map of built-in encoders for common types such as {@link List}.
     */
    public static Map<Class<?>, Encoder<?>> all() {
        // Return a defensive copy, so our built-in types remain immutable and users can still
        // override default encoder configuration on a per-bus instance basis.
        return new HashMap<Class<?>, Encoder<?>>(TYPE_ENCODERS);
    }
}
