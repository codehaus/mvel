package org.mvbus.util;

import java.io.IOException;


public interface WireOutput<T> {
    public void append(byte[] b) throws IOException;
    public void controlMessage(int type) throws IOException;
    public void encodeObject(Object object) throws IOException;
}
