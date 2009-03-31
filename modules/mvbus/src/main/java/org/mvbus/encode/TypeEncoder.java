package org.mvbus.encode;

public interface TypeEncoder {
    public void encode(MVBUSEncoder encoder, Object inst);
}
