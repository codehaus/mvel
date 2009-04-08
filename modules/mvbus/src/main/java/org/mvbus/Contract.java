package org.mvbus;

import org.mvbus.encode.ContractMessagingEngine;

import java.util.List;
import java.io.IOException;

public class Contract<T> {
    public List<String> parameters;
    public String contractString;
    private ContractMessagingEngine engine;

    public Contract(List<String> parameters, String contractString, ContractMessagingEngine engine) {
        this.parameters = parameters;
        this.contractString = contractString;
        this.engine = engine;
    }

    public byte[] createMessage(T instance) throws IOException {
        return engine.encode(instance).getMessage();
    }
}
