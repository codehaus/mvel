/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.mvel2.util;

import static java.lang.String.valueOf;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.isPublic;
import java.util.Collection;
import java.util.Map;

public class PropertyTools {
    public static boolean isEmpty(Object o) {
        if (o != null) {
            if (o instanceof Object[]) {
                return ((Object[]) o).length == 0 ||
                        (((Object[]) o).length == 1 && isEmpty(((Object[]) o)[0]));
            }
            else {
                return ("".equals(valueOf(o)))
                        || "null".equals(valueOf(o))
                        || (o instanceof Collection && ((Collection) o).size() == 0)
                        || (o instanceof Map && ((Map) o).size() == 0);
            }
        }
        return true;
    }

    public static Method getSetter(Class clazz, String property) {
        property = ReflectionUtil.getSetter(property);

        for (Method meth : clazz.getMethods()) {
            if ((meth.getModifiers() & PUBLIC) == 0
                    && meth.getParameterTypes().length != 0) continue;

            if (property.equals(meth.getName())) {
                return meth;
            }
        }

        return null;
    }

    public static boolean hasGetter(Field field) {
        Method meth = getGetter(field.getDeclaringClass(), field.getName());
        return meth != null && field.getType().isAssignableFrom(meth.getReturnType());
    }

    public static boolean hasSetter(Field field) {
        Method meth = getSetter(field.getDeclaringClass(), field.getName());
        return meth != null && meth.getParameterTypes().length == 1 &&
                field.getType().isAssignableFrom(meth.getParameterTypes()[0]);
    }

    public static Method getGetter(Class clazz, String property) {
        String isGet = ReflectionUtil.getIsGetter(property);
        property = ReflectionUtil.getGetter(property);

        for (Method meth : clazz.getMethods()) {
            if ((meth.getModifiers() & PUBLIC) == 0
                    || meth.getParameterTypes().length != 0
                    ) {
            }
            else if (property.equals(meth.getName()) ||
                    isGet.equals(meth.getName())) {
                return meth;
            }
        }

        return null;
    }


    public static Member getFieldOrAccessor(Class clazz, String property) {
        if (property.charAt(property.length() - 1) == ')') return getGetter(clazz, property);

        for (Field f : clazz.getFields()) {
            if (property.equals(f.getName())) {
                if ((f.getModifiers() & PUBLIC) != 0) return f;
                break;
            }
        }

        return getGetter(clazz, property);
    }

    public static Member getFieldOrWriteAccessor(Class clazz, String property) {
        Field field;
        try {
            if ((field = clazz.getField(property)) != null &&
                    isPublic(field.getModifiers())) {
                return field;
            }
        }
        catch (NullPointerException e) {
            return null;
        }
        catch (NoSuchFieldException e) {
            // do nothing.
        }

        return getSetter(clazz, property);
    }


    public static boolean contains(Object toCompare, Object testValue) {
        if (toCompare == null)
            return false;
        else if (toCompare instanceof String)
            return ((String) toCompare).contains(valueOf(testValue));
            //    return ((String) toCompare).indexOf(valueOf(testValue)) > -1;
        else if (toCompare instanceof Collection)
            return ((Collection) toCompare).contains(testValue);
        else if (toCompare instanceof Map)
            return ((Map) toCompare).containsKey(testValue);
        else if (toCompare.getClass().isArray()) {
            for (Object o : ((Object[]) toCompare)) {
                if (testValue == null && o == null) return true;
                else if (o != null && o.equals(testValue)) return true;
            }
        }
        return false;
    }


}
