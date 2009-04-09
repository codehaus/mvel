package org.mvbus.encode.contract.mvel;

import org.mvbus.BadMessageException;
import static org.mvbus.encode.WireMessageData.*;
import org.mvel2.MVEL;
import static org.mvel2.util.ParseTools.getSubComponentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import static java.lang.System.arraycopy;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.CRC32;

//todo: redo everything :) -- super prototype
public class MvelContractMessageDecodingEngine {
    private static final int BLOCK_SIZE = 512;
    private Map<String, Serializable> contracts = new HashMap<String, Serializable>();

    public Object decode(InputStream inStream) throws IOException {
        LinkedList<byte[]> blocks = new LinkedList<byte[]>();
        byte[] curr = new byte[BLOCK_SIZE];
        blocks.add(curr);

        int totalRead = 0;
        int read;

        for (int i = 0; i < curr.length; i++) {
            totalRead += (read = inStream.read(curr));
            if (read < curr.length) break;
            blocks.add(curr = new byte[BLOCK_SIZE]);
        }

        byte[] newByte = new byte[totalRead];
        int offset = 0;
        int copySize;
        for (byte[] block : blocks) {
            if ((totalRead - (block.length + offset)) < 0) {
                copySize = totalRead % block.length;
            }
            else {
                copySize = block.length;
            }
            arraycopy(block, 0, newByte, offset, copySize);
            offset += block.length;
        }

        return decode(newByte);
    }

    public Object decode(byte[] encoding) {
        Serializable compiledContract = null;
        Map<String, Object> parms = new LinkedHashMap<String, Object>();
        int p = 0;

        boolean parity = false;
        CRC32 crc32 = null;

        int read;

        for (int i = 0; i < encoding.length;) {
            if (encoding[i] == CONTROL) {
                switch (decodeInteger(encoding, i)) {
                    case MSG_START:
                        read = readBlock(encoding, i += 5);
                        if (parity) crc32.update(encoding, i, read);

                        String contractName = decodeString(encoding, i, read);

                        if (!contracts.containsKey(contractName)) {
                            throw new RuntimeException("no such contract: " + contractName);
                        }
                        compiledContract = contracts.get(contractName);

                        i += read;

                        break;

                    case LISTSTART:
                        //todo: This section should support all list-like data structures (arrays, lists, sets)
                        read = readBlock(encoding, i += 5);
                        if (parity) crc32.update(encoding, i, read);

                        String type = (String) getObject(encoding, i, read);

                        read = readBlock(encoding, i += read);
                        if (parity) crc32.update(encoding, i, read);

                        int length = (Integer) getObject(encoding, i, read);

                        i += read;

                        try {
                            Class cls = Class.forName(type, false, Thread.currentThread().getContextClassLoader());
                            Object newList;
                            if (cls.isArray()) {
                                newList = Array.newInstance(getSubComponentType(cls), length);
                            }
                            else {
                                throw new RuntimeException("not yet supported");
                            }

                            int cursor = 0;
                            while (!(encoding[i] == CONTROL
                                    && (decodeInteger(encoding, i) == ENDBLOCK))) {

                                read = readBlock(encoding, i);
                                if (parity) crc32.update(encoding, i, read);

                                Array.set(newList, cursor++, getObject(encoding, i, read));
                                i += read;
                            }

                            parms.put("$_" + (p++), newList);

                        }
                        catch (Exception e) {
                            //todo: handle all this at some point.
                            throw new RuntimeException(e);
                        }

                        break;

                    case ENDBLOCK:
                        i += 5;
                        break;

                    case MSG_END:
                        i += 5;
                        break;

                    case PARITY_CHECK:
                        i += 5;
                        parity = true;
                        /**
                         * Lazily initialize the CRC32 class.
                         */
                        crc32 = new CRC32();
                        break;

                    case CHECKSUM:
                        i += 5;
                        if (parity) {
                            if (crc32.getValue() != decodeLong(encoding, i)) {
                                throw new BadMessageException("bad message: crc32 checksum failure.");
                            }
                        }
                        break;

                    default:
                        i += 5;
                        break;
                }
            }
            else {
                read = readBlock(encoding, i);

                if (parity) {
                    crc32.update(encoding, i, read);
                }

                parms.put("$_" + (p++), getObject(encoding, i, read));
                i += read;
            }
        }

        return MVEL.executeExpression(compiledContract, parms);
    }


    public void addContract(String name, String contract) {
        contracts.put(name, MVEL.compileExpression(contract));
    }
}
