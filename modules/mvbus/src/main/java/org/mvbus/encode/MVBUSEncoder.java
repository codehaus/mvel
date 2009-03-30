package org.mvbus.encode;

import org.mvel2.util.StringAppender;
import org.mvel2.util.PropertyTools;
import org.mvel2.util.ParseTools;

import java.lang.reflect.Field;
import java.lang.reflect.Array;

public class MVBUSEncoder {
    private StringAppender appender;

    private boolean pretty = false;
    private int tabDepth = 0;

    public MVBUSEncoder() {
        appender = new StringAppender();
    }

    public void encode(Object toEncode) {
        Class encodeClass = toEncode.getClass();

        appender.append("new " + encodeClass.getName() + "().{");
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
                if (fieldValue == null) {
                    continue;
                }

                appender.append(fields[i].getName() + (pretty ? " = " : "="));
                stringify(fieldValue);

                if (i+1 < fields.length) {
                    appender.append(",");
                    prettyCR();
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("unable to encode", e);
        }
        prettyOutdent();
        appender.append("}");
    }

    private void stringify(Object value) {
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
                if (i+1 < length) appender.append(",");
            }
            prettyOutdent();
            appender.append("}");
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
