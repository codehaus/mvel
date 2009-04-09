package org.mvbus.encode;

import java.io.IOException;
import java.io.OutputStream;

public interface ContractMessagingEngine {
    public <T extends OutputStream> ContractMessagingEngine encode(T stream, Object toEncode) throws IOException;
}
