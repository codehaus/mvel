package org.mvbus;

import org.mvbus.MVBUSEncoder;

// TODO Can we get rid of this?
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
