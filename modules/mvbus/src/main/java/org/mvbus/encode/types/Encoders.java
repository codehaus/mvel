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
    static {
        // init default type encoders here
        TYPE_ENCODERS.put(Map.class, new MapEncoder());
        TYPE_ENCODERS.put(List.class, new ListEncoder());
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
