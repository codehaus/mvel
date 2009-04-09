package org.mvbus.encode;

import org.mvel2.util.StringAppender;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Float.floatToIntBits;
import static java.lang.Float.intBitsToFloat;
import java.nio.ByteBuffer;

public class WireMessageData {
    public static final int CONTROL = 1;
    public static final int ENDBLOCK = 2;

    public static final int TYPE_STR = 6;
    public static final int TYPE_INT = 7;
    public static final int TYPE_LONG = 8;
    public static final int TYPE_SHORT = 9;
    public static final int TYPE_DOUBLE = 10;
    public static final int TYPE_FLOAT = 11;
    public static final int TYPE_BOOL = 12;
    public static final int TYPE_NULL = 13;

    public static final int LISTSTART = 10;

    public static final int MSG_START = 20;
    public static final int SEPERATOR = 21;
    public static final int ARRAY = 22;
    public static final int ARRAYLEN = 23;
    public static final int CHECKSUM = 24;
    public static final int MSG_END = 25;
    public static final int MSG_SIZE = 26;
    public static final int PARITY_CHECK = 27;


    /**
     * Decode an object by automatically detecting the encoded type at the specified offset.
     *
     * @param input
     * @param start
     * @param length
     * @return
     */
    public static Object getObject(byte[] input, int start, int length) {
        switch (input[start]) {
            case TYPE_INT:
                return decodeInteger(input, start);
            case TYPE_LONG:
                return decodeLong(input, start);
            case TYPE_DOUBLE:
                return decodeDouble(input, start);
            case TYPE_FLOAT:
                return decodeFloat(input, start);
            case TYPE_SHORT:
                return decodeShort(input, start);
            case TYPE_STR:
                return decodeString(input, start, length);
            case TYPE_BOOL:
                return decodeBoolean(input, start);
            case TYPE_NULL:
                return null;
        }
        return null;
    }

    public static byte[] encodeControlMsg(int messageType) {
        byte[] b = encodeInteger(messageType);
        b[0] = CONTROL;
        return b;
    }

    /**
     * Encode an object by automatically detecting type and choosing the appropriate encoding.
     *
     * @param object
     * @return
     */
    public static byte[] encodeObject(Object object) {
        if (object == null) return encodeNull();
        else if (object instanceof String) return encodeString((String) object);
        else if (object instanceof Integer) return encodeInteger((Integer) object);
        else if (object instanceof Double) return encodeDouble((Double) object);
        else if (object instanceof Long) return encodeLong((Long) object);
        else if (object instanceof Float) return encodeFloat((Float) object);
        else if (object instanceof Short) return encodeShort((Short) object);
        else if (object instanceof Boolean) return encodeBoolean((Boolean) object);
        else {
            // temporaliy encode everything else as a string.
            return encodeString(String.valueOf(object));
        }
    }

    /**
     * Encode a boolean.
     *
     * @param bool
     * @return
     */
    public static byte[] encodeBoolean(boolean bool) {
        byte[] b = new byte[5];
        b[0] = TYPE_BOOL;
        b[1] = (byte) (bool ? 1 : 0);
        return b;
    }

    /**
     * Encode an integer.
     *
     * @param value
     * @return
     */
    public static byte[] encodeInteger(int value) {
        byte[] b = new byte[5];
        b[0] = TYPE_INT;
        b[1] = (byte) ((value >> 24) & 0xFF);
        b[2] = (byte) ((value >> 16) & 0xFF);
        b[3] = (byte) ((value >> 8) & 0xFF);
        b[4] = (byte) ((value) & 0xFF);

        return b;
    }

    public static byte[] encodeShort(short value) {
        byte[] b = encodeInteger(value);
        b[0] = TYPE_SHORT;
        return b;
    }


    public static byte[] encodeLong(long value) {
        byte[] b = new byte[9];
        b[0] = TYPE_LONG;
        b[1] = (byte) ((value >> 56) & 0xFF);
        b[2] = (byte) ((value >> 48) & 0xFF);
        b[3] = (byte) ((value >> 40) & 0xFF);
        b[4] = (byte) ((value >> 32) & 0xFF);
        b[5] = (byte) ((value >> 24) & 0xFF);
        b[6] = (byte) ((value >> 16) & 0xFF);
        b[7] = (byte) ((value >> 8) & 0xFF);
        b[8] = (byte) ((value) & 0xFF);
        return b;
    }

    public static byte[] encodeDouble(double value) {
        return encodeLong(doubleToLongBits(value));
    }

    public static byte[] encodeFloat(float value) {
        return encodeInteger(floatToIntBits(value));
    }

    /**
     * Encode a null value.
     *
     * @return
     */
    public static byte[] encodeNull() {
        byte[] b = new byte[5];
        b[0] = TYPE_NULL;
        return b;
    }

    /**
     * Decode an integer from the encoded data at the specified offset.
     *
     * @param b
     * @param start
     * @return
     */
    public static int decodeInteger(byte[] b, int start) {
        return (((((int) b[start + 4]) & 0xFF) << 32) +
                ((((int) b[start + 3]) & 0xFF) << 40) +
                ((((int) b[start + 2]) & 0xFF) << 48) +
                ((((int) b[start + 1]) & 0xFF) << 56));
    }

    public static short decodeShort(byte[] b, int start) {
        return (short) decodeInteger(b, start);
    }

    public static long decodeLong(byte[] b, int start) {
        return ((((long) b[start + 8]) & 0xFF) +
                ((((long) b[start + 7]) & 0xFF) << 8) +
                ((((long) b[start + 6]) & 0xFF) << 16) +
                ((((long) b[start + 5]) & 0xFF) << 24) +
                ((((long) b[start + 4]) & 0xFF) << 32) +
                ((((long) b[start + 3]) & 0xFF) << 40) +
                ((((long) b[start + 2]) & 0xFF) << 48) +
                ((((long) b[start + 1]) & 0xFF) << 56));

    }

    public static float decodeFloat(byte[] b, int start) {
        return intBitsToFloat(decodeInteger(b, start));
    }

    public static double decodeDouble(byte[] b, int start) {
        return longBitsToDouble(decodeLong(b, start));
    }

    /**
     * Encode a string.
     *
     * @param s
     * @return
     */
    public static byte[] encodeString(String s) {
        char[] ca = s.toCharArray();
        byte[] encArray = new byte[(ca.length * 5) + 5];

        int i = 0;
        for (; i < ca.length; i++) {
            writeBlock(encArray, i * 5, encodeInteger(ca[i]));
        }

        encArray[0] = TYPE_STR;
        encArray[i * 5] = ENDBLOCK;

        return encArray;
    }

    /**
     * Decode a String from the encoded data at the specified offset.
     *
     * @param input
     * @param start
     * @param length
     * @return
     */
    public static String decodeString(byte[] input, int start, int length) {
        StringAppender appender = new StringAppender();
        int end = start + (length - 5);

        for (int i = start; i < end; i += 5) {
            appender.append((char) decodeInteger(input, i));
        }

        return appender.toString();
    }

    /**
     * Decode a boolean from the encoded data at the specified offet.
     *
     * @param input
     * @param start
     * @return
     */
    public static boolean decodeBoolean(byte[] input, int start) {
        return input[start + 1] == 1;
    }

    /**
     * Write the specified buffer to the output.
     *
     * @param output
     * @param offset
     * @param buf
     * @return
     */
    public static int writeBlock(byte[] output, int offset, byte[] buf) {
        int i = 0;
        while (i < buf.length) {
            output[i + offset] = buf[i++];
        }
        return i;
    }

    /**
     * Determine how long the next block is.
     *
     * @param input
     * @param offset
     * @return - offset length of the next block
     */
    public static int readBlock(byte[] input, int offset) {
        int i = 0;
        switch (input[offset]) {
            case TYPE_STR:
                while ((offset + i) < input.length) {
                    if (input[offset + i] == ENDBLOCK) {
                        return i + 5;
                    }
                    i += 5;
                }
                return i;

            case TYPE_LONG:
                return 9;
            default:
                return 5;
        }
    }

}
