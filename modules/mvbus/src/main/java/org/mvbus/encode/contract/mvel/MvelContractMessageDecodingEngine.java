package org.mvbus.encode.contract.mvel;

import org.mvbus.encode.WireMessageData;
import org.mvel2.MVEL;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

//todo: redo everything :) -- super prototype
public class MvelContractMessageDecodingEngine {
    private Map<String, Serializable> contracts = new HashMap<String, Serializable>();

    public Object decode(byte[] encoding) {
        Serializable compiledContract;

        for (int i = 0; i < encoding.length; i++) {
            switch (encoding[i]) {
                case WireMessageData.MSG_START:
                    System.out.println("MSG_START:");

                    for (int x = ++i; x < encoding.length; x++) {
                        if (encoding[x] == WireMessageData.SEPERATOR) {
                            String contractName = new String(encoding, i, x - i);

                            if (!contracts.containsKey(contractName)) {
                                throw new RuntimeException("no known contract: " + contractName);
                            }
                            compiledContract = contracts.get(contractName);

                            i = x + 1;
                            break;
                        }
                    }

                    break;

                case WireMessageData.SEPERATOR:
                    System.out.println("SEPERATOR");
                    break;

                case WireMessageData.MSG_END:
                    System.out.println("MSG_END");
                    break;
            }


        }
        return null;
    }

    public void addContract(String name, String contract) {
        contracts.put(name, MVEL.compileExpression(contract));
    }
}
