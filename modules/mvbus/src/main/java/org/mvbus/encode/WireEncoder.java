package org.mvbus.encode;

public interface WireEncoder<T> extends Encoder {
    public byte[] encode(ContractMessagingEngine engine, T instance);
    public Object decode(byte[] data, int offset);
}
