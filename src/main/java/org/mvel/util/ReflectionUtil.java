package org.mvel.util;

import static java.lang.System.arraycopy;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities for working with reflection.
 */
public class ReflectionUtil {
    /**
     * This new method 'slightly' outperforms the old method, it was
     * essentially a perfect example of me wasting my time and a
     * premature optimization.  But what the hell...
     *
     * @param s
     * @return String
     */
    public static String getSetter(String s) {
        char[] chars = new char[s.length() + 3];

        chars[0] = 's';
        chars[1] = 'e';
        chars[2] = 't';

        if (s.charAt(0) > 'Z') {
            chars[3] = (char) (s.charAt(0) - ('z' - 'Z'));
        }
        else {
            chars[3] = s.charAt(0);
        }

        for (int i = s.length() - 1; i != 0; i--) {
            chars[i + 3] = s.charAt(i);
        }

        return new String(chars);
    }


    public static String getGetter(String s) {
        char[] c = s.toCharArray();
        char[] chars = new char[c.length + 3];

        chars[0] = 'g';
        chars[1] = 'e';
        chars[2] = 't';

        if (c[0] > 'Z') {
            chars[3] = (char) (c[0] - ('z' - 'Z'));
        }
        else {
            chars[3] = (c[0]);
        }

        arraycopy(c, 1, chars, 4, c.length - 1);

        return new String(chars);
    }


    public static String getIsGetter(String s) {
        char[] c = s.toCharArray();
        char[] chars = new char[c.length + 2];

        chars[0] = 'i';
        chars[1] = 's';

        if (s.charAt(0) > 'Z') {
            chars[2] = (char) (c[0] - ('z' - 'Z'));
        }
        else {
            chars[2] = c[0];
        }

        arraycopy(c, 1, chars, 3, c.length - 1);

        return new String(chars);
    }

    private static String parameterizeClassName(String s) {
        char[] chars = s.toCharArray();
        if (s.charAt(0) < 'a') {
            chars[0] = (char) (chars[0] + ('z' - 'Z'));
        }

        return new String(chars);
    }

    public static String parameterizeClassName(Class c) {
        return parameterizeClassName(c.getName().substring(c.getName().lastIndexOf('.') + 1));
    }


    public static String getParameterFromAccessor(String s) {
        if (s.charAt(0) == 'i') {
            char[] chars = new char[s.length() - 2];

            for (int i = chars.length; i > 0; i--) {
                chars[i - 1] = s.charAt(i + 1);
            }

            if (s.charAt(2) < 'a') {
                chars[0] = (char) (s.charAt(2) + ('z' - 'Z'));
            }

            return new String(chars);
        }
        else {
            char[] chars = new char[s.length() - 3];

            for (int i = chars.length; i > 0; i--) {
                chars[i - 1] = s.charAt(i + 2);
            }

            if (s.charAt(3) < 'a') {
                chars[0] = (char) (s.charAt(3) + ('z' - 'Z'));
            }

            return new String(chars);
        }
    }

    public static boolean isAssignable(Object instance, Class interfaceClass) {
        if (instance == null) return false;
        Class ref = instance.getClass();

        while (ref != Object.class) {
            if (interfaceClass.isAssignableFrom(ref)) return true;
            ref = ref.getSuperclass();
        }

        return false;
    }

    public static Set<Field> getAllFields(Class cls) {
        Set<Field> allFields = new HashSet<Field>(cls.getFields().length, 1.0f);
        for (Field fld : cls.getFields()) allFields.add(fld);
        return allFields;
    }
}
