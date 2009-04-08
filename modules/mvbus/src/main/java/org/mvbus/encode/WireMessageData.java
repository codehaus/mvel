package org.mvbus.encode;

public class WireMessageData {
    public static final byte MSG_START = (byte) 0x00A0;
    public static final byte SEPERATOR = (byte) 0x00A1;
    public static final byte ARRAY = (byte) 0x00A2;
    public static final byte ARRAYLEN = (byte) 0x00A3;
    public static final byte CHECKSUM = (byte) 0x00A4;
    public static final byte MSG_END = (byte)  0x00A5;


//    MSG_START((byte) 0x00A0),
//    SEPERATOR((byte) 0x00A1),
//    ARRAY((byte) 0x00A2),
//    ARRAYLEN((byte) 0x00A3),
//    MSG_END((byte) 0x00A4);
//
//
//    WireMessageData(byte m) {
//        this.message = m;
//    }
//
//    public final byte message;
//
//    public byte value() {
//        return message;
//    }
}
