package org.mvbus.encode;

import org.mvel2.util.StringAppender;

public class WireMessageData {
    public static final int TYPE_ENDMARK = 1;
    public static final int TYPE_STR = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_BOOL = 4;
    public static final int TYPE_NULL = -1;


    public static final int MSG_START = 0xFF00;
    public static final int SEPERATOR = 0xFF01;
    public static final int ARRAY = 0xFF02;
    public static final int ARRAYLEN = 0xFF02;
    public static final int CHECKSUM = 0xFF03;
    public static final int MSG_END = 0xFF04;


    public static Object getObject(byte[] input, int start, int length) {
        switch (input[start]) {
            case TYPE_INT:
               return decodeInteger(input, start);
            case TYPE_STR:
               return decodeString(input, start, length);
            case TYPE_BOOL:
               return decodeBoolean(input, start);
        }
        return null;
    }

    public static byte[] encodeObject(Object object) {
        if (object == null) return encodeNull();
        else if (object instanceof Integer)return encodeInteger((Integer) object);
        else if (object instanceof String) return encodeString((String) object);
        else if (object instanceof Boolean) return encodeBoolean((Boolean) object);
        else {
            // temporaliy encode everything else as a string.
            return encodeString(String.valueOf(object));
        }
    }


    public static byte[] encodeBoolean(boolean bool) {
        byte[] b = encodeInteger(bool ? 1 : 0);
        b[0] = TYPE_BOOL;
        return b;
    }

    public static byte[] encodeInteger(int value) {
        byte[] b = new byte[5];
        b[0] = TYPE_INT;
        b[1] = (byte) ((value >> 24) & 0xFF);
        b[2] = (byte) ((value >> 16) & 0xFF);
        b[3] = (byte) ((value >> 8) & 0xFF);
        b[4] = (byte) ((value) & 0xFF);

        return b;
    }

    public static byte[] encodeNull() {
        byte[] b = new byte[5];
        b[0] = TYPE_NULL;
        return b;
    }

    public static int decodeInteger(byte[] b, int start) {
        return (((((int) b[start+4]) & 0xFF) << 32) +
                ((((int) b[start+3]) & 0xFF) << 40) +
                ((((int) b[start+2]) & 0xFF) << 48) +
                ((((int) b[start+1]) & 0xFF) << 56));
    }

    public static byte[] encodeString(String s) {
        char[] ca = s.toCharArray();
        byte[] encArray = new byte[(ca.length*5)+5];

        int i = 0;
        for (; i < ca.length; i++) {
             writeBlock(encArray, i*5, encodeInteger(ca[i]));
        }

        encArray[0] = TYPE_STR;
        encArray[i * 5] = TYPE_ENDMARK;

        return encArray;
    }

    public static String decodeString(byte[] input, int start, int length) {
        StringAppender appender = new StringAppender();
        int end = start + (length-5);

        for (int i = start; i < end; i += 5) {
            appender.append((char) decodeInteger(input, i));
        }

        return appender.toString();
    }

    public static boolean decodeBoolean(byte[] input, int start) {
        return decodeInteger(input, start) == 1;
    }

    public static int writeBlock(byte[] output, int offset, byte[] buf) {
        int i = 0;
        while (i < buf.length) {
            output[i+offset] = buf[i++];
        }
        return i;
    }

    public static int readBlock(byte[] input, int offset) {
        int i = 0;
        switch (input[offset]) {
            case TYPE_STR:
                while ((offset+i) < input.length) {
                    if (input[offset + i] == TYPE_ENDMARK) {
                        return i + 5;
                    }
                    i += 5;
                }
                return i;

            default:
                return 5;
        }
    }

}
