package org.mvbus.encode.engines.mvel;

import org.mvbus.Configuration;
import org.mvbus.PrintStyle;
import org.mvbus.encode.EncodingEngine;
import org.mvbus.util.OutputAppender;
import org.mvel2.util.ParseTools;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This is the default workhorse, the Java to MVEL encoding engine.
 */
public abstract class MvelEncodingEngine implements EncodingEngine {
    protected Configuration config;
    protected boolean pretty = false;
    protected int tabDepth = 0;

    protected static final Class[] EMPTYCLS = new Class[0];

    public EncodingEngine init(Configuration config) {
        this.config = config;
        pretty = config.getStyle() == PrintStyle.PRETTY;
        return this;
    }

    public EncodingEngine encode(Object toEncode) {
        Class encodeClass = toEncode.getClass();

        if (config.canEncode(encodeClass)) {
            config.getEncoder(encodeClass).encode(this, toEncode);
        }
        else {
            try {
                encodeClass.getDeclaredConstructor(EMPTYCLS);
                append("new " + encodeClass.getName() + "().{");
            }
            catch (Exception e) {
                append("instantiate_obj(").append(encodeClass.getName()).append(").{");
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
                        append(",");
                        prettyCR();
                    }

                    append(fields[i].getName()).append(pretty ? " = " : "=");
                    stringify(fieldValue);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("unable to encode", e);
            }
            prettyOutdent();
            append("}");
        }
        return this;
    }

    public EncodingEngine stringify(Object value) {
        if (value == null) {
            append("null");
            return this;
        }
        Class type = value.getClass();

        if (String.class.isAssignableFrom(type)) {
            append("\"").append(String.valueOf(value)).append("\"");
        }
        else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
                || type == Byte.class) {
            append(String.valueOf(value));
        }
        else if (type.isArray()) {
            append("new ").append(type.getComponentType().getName()).append("[] {");
            prettyIndent();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                stringify(Array.get(value, i));
                if (i + 1 < length) append(",");
            }
            prettyOutdent();
            append("}");
        }
        else if (config.canEncode(type)) {
            config.getEncoder(type).encode(this, value);
        }
        else {
            encode(value);
        }
        return this;
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    public abstract String getEncoded();

    public abstract OutputAppender append(String str);

    public abstract void flush();

    protected void prettyCR() {
        if (pretty) {
            append("\n").append(ParseTools.repeatChar(' ', tabDepth * 8));
        }
    }

    protected void prettyIndent() {
        if (pretty) {
            append("\n").append(ParseTools.repeatChar(' ', ++tabDepth * 8));
        }
    }

    protected void prettyOutdent() {
        if (pretty) {
            append("\n").append(ParseTools.repeatChar(' ', --tabDepth * 8));
        }
    }


}
