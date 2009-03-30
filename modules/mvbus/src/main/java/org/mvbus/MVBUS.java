package org.mvbus;

import org.mvbus.encode.MVBUSEncoder;

public class MVBUS {
    public static String marshal(Object e) {
        MVBUSEncoder mve = new MVBUSEncoder();

        mve.encode(e);
        return mve.getEncoded();
    }

    public static String marshalPretty(Object e) {
        MVBUSEncoder mve = new MVBUSEncoder();
        mve.setPretty(true);
        mve.encode(e);
        return mve.getEncoded();
    }
}
