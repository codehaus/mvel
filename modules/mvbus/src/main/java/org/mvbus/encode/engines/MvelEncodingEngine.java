package org.mvbus.encode.engines;

import org.mvbus.encode.EncodingEngine;
import org.mvbus.Configuration;
import org.mvbus.PrintStyle;
import org.mvbus.util.OutputAppender;
import org.mvel2.util.ParseTools;
import org.mvel2.util.StringAppender;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * This is the default workhorse, the Java to MVEL encoding engine.
 */
public class MvelEncodingEngine implements EncodingEngine {
    private Configuration config;

    private OutputAppender output;

    private StringAppender appender;
    private OutputStream outstream;
    private PrintWriter writer;

    private boolean pretty = false;
    private int tabDepth = 0;

    public MvelEncodingEngine() {
        appender = new StringAppender();
        output = new OutputAppender() {

            StringAppender a = appender;
            public OutputAppender append(String str) {
                a.append(str);
                return this;
            }
        };
    }

    public MvelEncodingEngine(OutputStream stream) {
        writer = new PrintWriter(outstream = stream);
        output = new OutputAppender() {

            PrintWriter w = writer;
            public OutputAppender append(String append) {
                w.append(append);
                return this;
            }
        };

    }

    private static final Class[] EMPTYCLS = new Class[0];

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
                output.append("new " + encodeClass.getName() + "().{");
            }
            catch (Exception e) {
                output.append("org.mvbus.decode.MVBUSDecoder.instantiate(").append(encodeClass.getName()).append(").{");
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
                        output.append(",");
                        prettyCR();
                    }

                    output.append(fields[i].getName()).append(pretty ? " = " : "=");
                    stringify(fieldValue);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("unable to encode", e);
            }
            prettyOutdent();
            output.append("}");
        }
        return this;
    }

    public EncodingEngine stringify(Object value) {
        if (value == null) {
            output.append("null");
            return this;
        }
        Class type = value.getClass();

        if (String.class.isAssignableFrom(type)) {
            output.append("\"").append(String.valueOf(value)).append("\"");
        }
        else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
                || type == Byte.class) {
            output.append(String.valueOf(value));
        }
        else if (type.isArray()) {
            output.append("new ").append(type.getComponentType().getName()).append("[] {");
            prettyIndent();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                stringify(Array.get(value, i));
                if (i + 1 < length) output.append(",");
            }
            prettyOutdent();
            output.append("}");
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

    /**
     * Will return the encoded stream for encoding sessions not using an OutputStream.
     * @return String of encoded object.
     */
    public String getEncoded() {
        if (appender != null) return appender.toString();
        return null;
    }

    public OutputStream getOutstream() {
        return outstream;
    }
    
    public OutputAppender append(String str) {
        return output.append(str);
    }

    public void flush() {
        writer.flush();
    }

    private void prettyCR() {
        if (pretty) {
            output.append("\n").append(ParseTools.repeatChar(' ', tabDepth * 8));
        }
    }

    private void prettyIndent() {
        if (pretty) {
            output.append("\n").append(ParseTools.repeatChar(' ', ++tabDepth * 8));
        }
    }

    private void prettyOutdent() {
        if (pretty) {
            output.append("\n").append(ParseTools.repeatChar(' ', --tabDepth * 8));
        }
    }


}
