package org.mvbus.decode;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;


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
