package org.mvbus.encode.contract.mvel;

import org.mvbus.encode.WireMessageData;
import static org.mvbus.encode.WireMessageData.decodeInteger;
import org.mvbus.BadMessageException;
import org.mvel2.MVEL;
import org.mvel2.util.ParseTools;

import java.io.Serializable;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
            read = inStream.read(curr);
            totalRead += read;
            if (read < curr.length) break;
            else {
                blocks.add(curr = new byte[BLOCK_SIZE]);
            }
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
            System.arraycopy(block, 0, newByte, offset, copySize);
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
            if (encoding[i] == WireMessageData.CONTROL) {
                switch (decodeInteger(encoding, i)) {
                    case WireMessageData.MSG_START:
                        i += 5;
                        read = WireMessageData.readBlock(encoding, i);
                        if (parity) crc32.update(encoding, i, read);

                        String contractName = WireMessageData.decodeString(encoding, i, read);
                        
                        if (!contracts.containsKey(contractName)) {
                            throw new RuntimeException("no such contract: " + contractName);
                        }
                        compiledContract = contracts.get(contractName);

                        i += read;

                        break;

                    case WireMessageData.LISTSTART:
                        i += 5;
                        read = WireMessageData.readBlock(encoding, i);
                        if (parity) crc32.update(encoding, i, read);

                        String type = (String) WireMessageData.getObject(encoding, i, read);

                        i += read;

                        read = WireMessageData.readBlock(encoding, i);
                        if (parity) crc32.update(encoding, i, read);

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
                            while (!(encoding[i] == WireMessageData.CONTROL
                                    && (decodeInteger(encoding, i) == WireMessageData.ENDBLOCK))) {

                                read = WireMessageData.readBlock(encoding, i);
                                if (parity) crc32.update(encoding, i, read);


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

                    case WireMessageData.ENDBLOCK:
                        i += 5;
                        break;

                    case WireMessageData.MSG_END:
                        i += 5;
                        break;

                    case WireMessageData.PARITY_CHECK:
                        i += 5;
                        parity = true;
                        crc32 = new CRC32();
                        break;

                    case WireMessageData.CHECKSUM:
                        i += 5;
                        if (parity) {

                            long checksumData = WireMessageData.decodeLong(encoding, i);
                            if (crc32.getValue() != checksumData) {
                                throw new BadMessageException("bad message: crc32 checksum failure: " +
                                        "(recv:" + crc32.getValue() + "):(block:" + checksumData + ")");
                            }
                        }
                        break;

                    default:
                        i += 5;
                        break;
                }
            }
            else {
                read = WireMessageData.readBlock(encoding, i);

                if (parity) {
                    crc32.update(encoding, i, read);
                }

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
