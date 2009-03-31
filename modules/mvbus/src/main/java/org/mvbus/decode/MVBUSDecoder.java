package org.mvbus.decode;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;


/**
 * Decoder utility class.  This class contains static methods that can be accessed by the decoding script.  It
 * is not actually used for direct decoding.
 *
 * TODO: Must support all common JVMs.  
 */
public class MVBUSDecoder {
    public static Object instantiate(Class cls) throws Exception {
        /**
         * We will only support the Sun JVM for this early prototype.  But this needs to be supported for
         * all JVMs.
         */
        Constructor evilConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(cls, Object.class.getConstructor((Class[]) null));
        evilConstructor.setAccessible(true);
        
        return evilConstructor.newInstance((Object[])null);
    }
}
