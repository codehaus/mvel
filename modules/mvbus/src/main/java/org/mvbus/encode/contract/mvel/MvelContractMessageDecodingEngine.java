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
        byte[] buf = new byte[5];

        for (int i = 0; i < encoding.length; i += read) {
            read = WireMessageData.readBlock(encoding, i,  buf);

            switch (WireMessageData.decodeInteger(encoding, i)) {
                case WireMessageData.MSG_START:
                    System.out.println("msg_start");
                    break;
                case WireMessageData.SEPERATOR:
                    System.out.println("seperator");
                    break;

            }

        }
        parms.size();

        return null;
    }

    public void addContract(String name, String contract) {
        contracts.put(name, MVEL.compileExpression(contract));
    }
}
