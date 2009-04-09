package org.mvbus;

import org.mvbus.encode.ContractMessagingEngine;
import org.mvbus.encode.contract.mvel.MvelContractMessageDecodingEngine;
import org.mvbus.encode.contract.mvel.MvelContractMessageEncodingEngine;

import java.util.List;
import java.io.IOException;
import java.io.OutputStream;

public class Contract<T> {
    public List<String> parameters;
    public String contractString;
   // private Configuration config;
    private ContractMessagingEngine engine;

    public Contract(List<String> parameters, String contractString, Configuration config) {
        this.parameters = parameters;
        this.contractString = contractString;
     //   this.config = engine;
        //todo: support pluggable engines
        this.engine = new MvelContractMessageEncodingEngine().init(config);
    }

    public void createMessage(OutputStream stream, T instance) throws IOException {
        engine.encode(stream, instance);
    }
}
