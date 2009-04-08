package org.mvbus.encode.contract.mvel;

import org.mvbus.Configuration;
import org.mvbus.PrintStyle;
import org.mvbus.encode.ContractMessagingEngine;
import org.mvbus.encode.Encoder;
import org.mvbus.encode.WireEncoder;
import org.mvbus.encode.WireMessageData;
import org.mvbus.util.OutputAppender;
import org.mvel2.util.StringAppender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MvelContractMessageEncodingEngine implements ContractMessagingEngine {
    private static final int MAX_MESSAGE_SIZE = 1024 * 10;

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(MAX_MESSAGE_SIZE);

    private OutputAppender<StringAppender> output = new OutputAppender<StringAppender>() {
        private StringAppender appender = new StringAppender();

        public OutputAppender append(String str) {
            appender.append(str);
            return this;
        }

        public StringAppender getTarget() {
            return appender;
        }
    };

    public String getEncoded() {
        return output.getTarget().toString();
    }
//
//    public OutputAppender append(String str) {
//        return output.append(str);
//    }

    public void flush() {
    }

    protected Configuration config;
    protected boolean pretty = false;
    protected int tabDepth = 0;

    protected static final Class[] EMPTYCLS = new Class[0];

    public ContractMessagingEngine init(Configuration config) {
        this.config = config;
        pretty = config.getStyle() == PrintStyle.PRETTY;
        return this;
    }

    public ContractMessagingEngine encode(Object toEncode) throws IOException {
        Class encodeClass = toEncode.getClass();

        if (config.canEncode(encodeClass)) {
            getWireEncoder(encodeClass).encode(this, toEncode);
        }
        else {
            byteArrayOutputStream.write(new StringAppender("$").append(encodeClass.getName()).toString().getBytes());
            byteArrayOutputStream.write(0);

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
                        byteArrayOutputStream.write(WireMessageData.SEPERATOR.message);
                    }

                    byteArrayOutputStream.write(1);
                    stringify(fieldValue);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("unable to encode", e);
            }
            byteArrayOutputStream.write(WireMessageData.MSG_END.message);
        }
        return this;
    }

    public ContractMessagingEngine stringify(Object value) throws IOException {
        if (value == null) {
            byteArrayOutputStream.write(0);
            return this;
        }
        Class type = value.getClass();

        if (String.class.isAssignableFrom(type)) {
            byteArrayOutputStream.write(String.valueOf(value).getBytes());
        }
        else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
                || type == Byte.class) {

            byteArrayOutputStream.write(String.valueOf(value).getBytes());

        }
        else if (type.isArray()) {
    //        append("new ").append(type.getComponentType().getName()).append("[] {");
            byteArrayOutputStream.write(WireMessageData.ARRAY.message);

            int length = Array.getLength(value);
            byteArrayOutputStream.write(length);

            byteArrayOutputStream.write(WireMessageData.ARRAYLEN.message);

            for (int i = 0; i < length; i++) {
                stringify(Array.get(value, i));
                if (i + 1 < length) byteArrayOutputStream.write(1);
            }
            byteArrayOutputStream.write(WireMessageData.SEPERATOR.message);
        }
        else if (config.canEncode(type)) {
            getWireEncoder(type).encode(this, value);
        }
        else {
            encode(value);
        }
        return this;
    }

    private WireEncoder getWireEncoder(Class type) {
        Encoder e = config.getEncoder(type);
        if (!(e instanceof WireEncoder)) {
            throw new RuntimeException("Encoder (" + e.getClass().getName() + ") does not implement the WireEncoder interface");
        }
        return (WireEncoder) e;
    }

    public byte[] getMessage() {
        return byteArrayOutputStream.toByteArray();
    }

}
