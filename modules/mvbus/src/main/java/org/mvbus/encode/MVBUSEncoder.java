package org.mvbus.encode;

import org.mvel2.util.StringAppender;
import org.mvel2.util.PropertyTools;
import org.mvel2.util.ParseTools;

import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.io.ObjectInputStream;

public class MVBUSEncoder {
    private StringAppender appender;

    private boolean pretty = false;
    private int tabDepth = 0;

    public MVBUSEncoder() {
        appender = new StringAppender();
    }

    private static final Class[] EMPTYCLS = new Class[0];


    public void encode(Object toEncode) {
        Class encodeClass = toEncode.getClass();

        if (TypeEncoderFactory.hasEncoder(encodeClass)) {
            TypeEncoderFactory.getEncoder(encodeClass).encode(this, toEncode);
        }
        else {
            try {
                encodeClass.getDeclaredConstructor(EMPTYCLS);
                appender.append("new " + encodeClass.getName() + "().{");
            }
            catch (Exception e) {
                appender.append("org.mvbus.decode.MVBUSDecoder.instantiate(" + encodeClass.getName() + ").{");
            }

            prettyIndent();

            try {
                Field[] fields = encodeClass.getDeclaredFields();
                Object fieldValue;
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    fieldValue = fields[i].get(toEncode);

                    /**
                     * Don't bother initializing null fields.
                     */
                    if (fieldValue == null || (fields[i].getModifiers() & (Modifier.STATIC | Modifier.FINAL)) != 0) {
                        continue;
                    }

                    if (i != 0 && i < fields.length) {
                        appender.append(",");
                        prettyCR();
                    }

                    appender.append(fields[i].getName() + (pretty ? " = " : "="));
                    stringify(fieldValue);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("unable to encode", e);
            }
            prettyOutdent();
            appender.append("}");
        }
    }

    public void stringify(Object value) {
        if (value == null) {
            appender.append("null");
            return;
        }
        Class type = value.getClass();

        if (String.class.isAssignableFrom(type)) {
            appender.append("\"").append(String.valueOf(value)).append("\"");
        }
        else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
                || type == Byte.class) {
            appender.append(String.valueOf(value));
        }
        else if (type.isArray()) {
            appender.append("new " + type.getComponentType().getName() + "[] {");
            prettyIndent();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                stringify(Array.get(value, i));
                if (i + 1 < length) appender.append(",");
            }
            prettyOutdent();
            appender.append("}");
        }
        else if (TypeEncoderFactory.hasEncoder(type)) {
            TypeEncoderFactory.getEncoder(type).encode(this, value);
        }
        else {
            encode(value);
        }
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    public String getEncoded() {
        return appender.toString();
    }

    public StringAppender getAppender() {
        return appender;
    }

    private void prettyCR() {
        if (pretty) {
            appender.append("\n").append(ParseTools.repeatChar(' ', tabDepth * 8));
        }
    }

    private void prettyIndent() {
        if (pretty) {
            appender.append("\n").append(ParseTools.repeatChar(' ', ++tabDepth * 8));
        }
    }

    private void prettyOutdent() {
        if (pretty) {
            appender.append("\n").append(ParseTools.repeatChar(' ', --tabDepth * 8));
        }
    }
}
