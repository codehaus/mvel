package org.mvbus.decode;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.Map;


/**
 * Decoder utility class.  This class contains static methods that can be accessed by the decoding script.  It
 * is not actually used for direct decoding.
 *
 * TODO: Must support all common JVMs.
 *
 * TODO(dhanji): Can we call this class "Decoding" or "DecodeTools" 
 */
public class DecodeTools {
    private static final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

    private static final Map<Class<?>, Constructor> CONSTRUCTOR_CACHE
            = new WeakHashMap<Class<?>, Constructor>();

    public static Object instantiate(Class cls) throws Exception {
        Constructor c = CONSTRUCTOR_CACHE.get(cls);
        if (c == null) {
            (c = reflectionFactory.newConstructorForSerialization(cls, Object.class.getDeclaredConstructor(new Class[0])))
                    .setAccessible(true);
            CONSTRUCTOR_CACHE.put(cls,c);
        }

        /**
         * We will only support the Sun JVM for this early prototype.  But this needs to be supported for
         * all JVMs.
         */

        return c.newInstance((Object[])null);
    }
}
