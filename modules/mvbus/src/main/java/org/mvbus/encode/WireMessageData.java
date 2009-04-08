package org.mvbus.encode;

public enum WireMessageData {
    MSG_START((byte) 0x00A0),
    SEPERATOR((byte) 0x00A1),
    ARRAY((byte) 0x00A2),
    ARRAYLEN((byte) 0x00A3),
    MSG_END((byte) 0x00A4);


    WireMessageData(byte m) {
        this.message = m;
    }

    public byte message;
}
