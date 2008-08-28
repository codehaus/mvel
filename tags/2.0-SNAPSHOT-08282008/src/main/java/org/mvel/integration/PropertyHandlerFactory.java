package org.mvel.integration;

import java.util.Map;
import java.util.HashMap;

public class PropertyHandlerFactory {
    protected static Map<Class, PropertyHandler> propertyHandlerClass =
            new HashMap<Class, PropertyHandler>();

    public static PropertyHandler getPropertyHandler(Class clazz) {
        return propertyHandlerClass.get(clazz);
    }

    public static boolean hasPropertyHandler(Class clazz) {
        return propertyHandlerClass.containsKey(clazz);
    }

    public static void registerPropertyHandler(Class clazz, PropertyHandler propertyHandler) {
        propertyHandlerClass.put(clazz, propertyHandler);
    }
}
