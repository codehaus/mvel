package org.mvbus.encode.contract.mvel;

import org.mvbus.encode.WireMessageData;
import static org.mvbus.encode.WireMessageData.decodeInteger;
import org.mvel2.MVEL;
import org.mvel2.util.ParseTools;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

//todo: redo everything :) -- super prototype
public class MvelContractMessageDecodingEngine {
    private Map<String, Serializable> contracts = new HashMap<String, Serializable>();

    public Object decode(byte[] encoding) {
        Serializable compiledContract = null;
        Map<String, Object> parms = new LinkedHashMap<String, Object>();
        int p = 0;

        int read;

        for (int i = 0; i < encoding.length;) {

            if (encoding[i] == WireMessageData.TYPE_CONTROL) {
                switch (decodeInteger(encoding, i)) {
                    case WireMessageData.MSG_START:
                        i += 5;
                        read = WireMessageData.readBlock(encoding, i);
                        String contractName = WireMessageData.decodeString(encoding, i, read);

                        if (!contracts.containsKey(contractName)) {
                            throw new RuntimeException("no such contract: " + contractName);
                        }
                        compiledContract = contracts.get(contractName);

                        i += read;

                        break;

                    case WireMessageData.TYPE_LIST:
                        i += 5;
                        read = WireMessageData.readBlock(encoding, i);
                        String type = (String) WireMessageData.getObject(encoding, i, read);
                        i += read;

                        read = WireMessageData.readBlock(encoding, i);
                        int length = (Integer) WireMessageData.getObject(encoding, i, read);
                        i += read;

                        try {
                            Class cls = Class.forName(type, false, Thread.currentThread().getContextClassLoader());
                            Object newList;
                            if (cls.isArray()) {
                                newList = Array.newInstance(ParseTools.getSubComponentType(cls), length);
                            }
                            else {
                                throw new RuntimeException("not yet supported");
                            }

                            int cursor = 0;
                            while (!(encoding[i] == WireMessageData.TYPE_CONTROL
                                    && (decodeInteger(encoding, i) == WireMessageData.TYPE_ENDMARK))) {

                                read = WireMessageData.readBlock(encoding, i);

                                Array.set(newList, cursor++, WireMessageData.getObject(encoding, i, read));
                                i += read;
                            }

                            parms.put("$_" + (p++), newList);

                        }
                        catch (Exception e) {
                            // handle all this at some point.
                            throw new RuntimeException(e);
                        }

                        break;

                    case WireMessageData.TYPE_ENDMARK:
                        i += 5;
                        break;

                    case WireMessageData.MSG_END:
                        i += 5;
                        break;

                    default:
                        i += 5;
                        break;
                }
            }
            else {
                read = WireMessageData.readBlock(encoding, i);
                parms.put("$_" + (p++), WireMessageData.getObject(encoding, i, read));
                i += read;
            }
        }

        return MVEL.executeExpression(compiledContract, parms);
    }

    public void addContract(String name, String contract) {
        contracts.put(name, MVEL.compileExpression(contract));
    }
}
