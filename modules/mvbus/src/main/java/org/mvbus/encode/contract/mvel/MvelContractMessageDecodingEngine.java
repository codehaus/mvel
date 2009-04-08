package org.mvbus.encode.contract.mvel;

import org.mvbus.encode.WireMessageData;
import org.mvel2.MVEL;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import java.io.IOException;

//todo: redo everything :) -- super prototype
public class MvelContractMessageDecodingEngine {
    private Map<String, Serializable> contracts = new HashMap<String, Serializable>();

    public Object decode(byte[] encoding) {
        Serializable compiledContract;
        Map<String, Object> parms = new HashMap<String, Object>();
        int p = 0;

        int read = 0;

        for (int i = 0; i < encoding.length;) {
            switch (WireMessageData.decodeInteger(encoding, i)) {
                case WireMessageData.MSG_START:
                    i += 5;
                    read = WireMessageData.readBlock(encoding, i);

                    System.out.println("HEADER:" + WireMessageData.decodeString(encoding, i, read));

                    i += read;

                    break;
                case WireMessageData.SEPERATOR:
                    i += 5;

                    System.out.println("SEPERATOR");
                    read = WireMessageData.readBlock(encoding, i);

                    System.out.println("OBJECT:" + WireMessageData.getObject(encoding, i, read));

                    i += read;

                    break;

                default:
                    i += 5;

            }

        }
        parms.size();

        return null;
    }

    public void addContract(String name, String contract) {
        contracts.put(name, MVEL.compileExpression(contract));
    }
}
