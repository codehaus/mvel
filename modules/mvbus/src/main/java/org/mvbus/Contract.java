package org.mvbus;

import org.mvbus.encode.ContractMessagingEngine;
import org.mvbus.encode.contract.mvel.MvelContractMessageDecodingEngine;
import org.mvbus.encode.contract.mvel.MvelContractMessageEncodingEngine;

import java.util.List;
import java.io.IOException;

public class Contract<T> {
    public List<String> parameters;
    public String contractString;
    private Configuration config;

    public Contract(List<String> parameters, String contractString, Configuration engine) {
        this.parameters = parameters;
        this.contractString = contractString;
        this.config = engine;
    }

    public byte[] createMessage(T instance) throws IOException {
        return new MvelContractMessageEncodingEngine().init(config).encode(instance).getMessage();
    }
}
