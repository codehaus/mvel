package org.mvbus.encode;

import java.io.IOException;

public interface ContractMessagingEngine {
    public ContractMessagingEngine encode(Object toEncode) throws IOException;
    public byte[] getMessage();
}
