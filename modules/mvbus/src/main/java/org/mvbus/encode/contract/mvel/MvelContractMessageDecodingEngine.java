package org.mvbus.encode.contract.mvel;

import org.mvbus.encode.WireMessageData;
import static org.mvbus.encode.WireMessageData.decodeInteger;
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

        int read;

        for (int i = 0; i < encoding.length;) {
            if (encoding[i] != WireMessageData.TYPE_CONTROL) {
                throw new RuntimeException("Expected Control Message! (Message Type Encountered: " + encoding[i] + ")");
            }
            switch (decodeInteger(encoding, i)) {
                case WireMessageData.MSG_START:
                    i += 5;
                    read = WireMessageData.readBlock(encoding, i);
                    System.out.println("HEADER:" + WireMessageData.decodeString(encoding, i, read));
                    i += read;

                    break;
                case WireMessageData.SEPERATOR:
                    i += 5;
                    read = WireMessageData.readBlock(encoding, i);
                    if (encoding[i] == WireMessageData.TYPE_CONTROL) continue;
                    System.out.println("OBJECT:" + WireMessageData.getObject(encoding, i, read));

                    i += read;

                    break;

                case WireMessageData.TYPE_LIST:
                    i += 5;
                    read = WireMessageData.readBlock(encoding, i);
                    String type = (String) WireMessageData.getObject(encoding, i, read);

                    i += read;

                    System.out.println("LIST:" + type);

                    while (encoding[i] != WireMessageData.TYPE_CONTROL
                            && (decodeInteger(encoding, i) != WireMessageData.TYPE_ENDMARK)) {
                        read = WireMessageData.readBlock(encoding, i);
                        int op;
                        if ((op = decodeInteger(encoding, i)) != WireMessageData.SEPERATOR) {
                            throw new RuntimeException("badly formed message: " + op );
                        }
                        i += 5;
                        read = WireMessageData.readBlock(encoding, i);
                        System.out.println("LIST_OBJ:" + WireMessageData.getObject(encoding, i, read));
                        i += read;
                    }

                    try {
                        Class cls = Class.forName(type, false, Thread.currentThread().getContextClassLoader());

                 
                    }
                    catch (ClassNotFoundException e) {
                        // handle this at some point.
                        throw new RuntimeException(e);
                    }

                    break;

                case WireMessageData.TYPE_ENDMARK:
                    System.out.println("ENDMARK");
                    i += 5;
                    break;

                case WireMessageData.MSG_END:
                    System.out.println("MSG_END");
                    i += 5;
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
