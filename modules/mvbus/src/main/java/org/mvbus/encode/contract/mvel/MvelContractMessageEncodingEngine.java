package org.mvbus.encode.contract.mvel;

import org.mvbus.Configuration;
import org.mvbus.PrintStyle;
import org.mvbus.encode.ContractMessagingEngine;
import org.mvbus.encode.Encoder;
import org.mvbus.encode.WireEncoder;
import org.mvbus.encode.WireMessageData;
import static org.mvbus.encode.WireMessageData.encodeControlMsg;
import org.mvbus.util.WireOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.zip.CRC32;

public class MvelContractMessageEncodingEngine implements ContractMessagingEngine {

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

    public <T extends OutputStream> ContractMessagingEngine encode(final T stream, Object toEncode, boolean parity) throws IOException {
        WireOutput out;
        if (parity) {
            out = new WireOutput<T>() {
                T s = stream;
                CRC32 crc = new CRC32();

                public void append(byte[] b) throws IOException {
                    crc.update(b);
                    s.write(b);
                }

                public void controlMessage(int type) throws IOException {
                    stream.write(encodeControlMsg(type));
                }

                public void encodeObject(Object object) throws IOException {
                    append(WireMessageData.encodeObject(object));
                }

                public long getChecksum() {
                    return crc.getValue();
                }
            };

            out.controlMessage(WireMessageData.PARITY_CHECK);
        }
        else {
            out = new WireOutput<T>() {
                T s = stream;

                public void append(byte[] b) throws IOException {
                    s.write(b);
                }

                public void controlMessage(int type) throws IOException {
                    stream.write(encodeControlMsg(type));
                }

                public void encodeObject(Object object) throws IOException {
                    byte[] b = WireMessageData.encodeObject(object);
                    stream.write(b);
                }

                public long getChecksum() {
                    return 0;
                }
            };
        }

        _encode(out, toEncode);

        out.controlMessage(WireMessageData.MSG_END);
        if (parity) {
            out.controlMessage(WireMessageData.CHECKSUM);
            out.encodeObject(out.getChecksum());
        }

        return this;
    }

    private ContractMessagingEngine _encode(WireOutput out, Object toEncode) throws IOException {
        Class encodeClass = toEncode.getClass();

        if (config.canEncode(encodeClass)) {
            getWireEncoder(encodeClass).encode(this, toEncode);
        }
        else {
            out.controlMessage(WireMessageData.MSG_START);
            out.encodeObject(encodeClass.getName());

            try {
                Field[] fields = encodeClass.getDeclaredFields();
                Object fieldValue;
                for (Field field : fields) {
                    field.setAccessible(true);
                    fieldValue = field.get(toEncode);
                    if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) != 0) {
                        continue;
                    }
                    stringify(out, fieldValue);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("unable to encode", e);
            }
        }


        return this;
    }

    public ContractMessagingEngine stringify(WireOutput out, Object value) throws IOException {
        if (value == null) {
            out.encodeObject(null);

            return this;
        }
        Class type = value.getClass();

        if (String.class.isAssignableFrom(type)) {
            out.encodeObject(String.valueOf(value));
        }
        else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
                || type == Byte.class) {

            out.encodeObject(value);
        }
        else if (type.isArray()) {
            int length = Array.getLength(value);

            out.controlMessage(WireMessageData.LISTSTART);
            out.encodeObject(type.getName());
            out.encodeObject(length);

            for (int i = 0; i < length; i++) {
                stringify(out, Array.get(value, i));
            }

            out.controlMessage(WireMessageData.ENDBLOCK);

        }
        else if (config.canEncode(type)) {
            getWireEncoder(type).encode(this, value);
        }
        else {
            _encode(out, value);
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


}
