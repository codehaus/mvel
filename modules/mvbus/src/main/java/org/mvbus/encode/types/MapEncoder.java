package org.mvbus.encode.types;

import org.mvbus.encode.EncodingEngine;
import org.mvbus.encode.Encoder;

import java.util.Map;

/**
 * A standard java.util.Map encoder. Converts any map and its key/value
 * pairs to an MVEL expression. Recursively encodes each key and value.
 */
class MapEncoder implements Encoder<Map<?, ?>> {
    public void encode(EncodingEngine encoder, Map<?, ?> map) {
        encoder.append("[");

        int size = map.size();
        int count = 0;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            encoder.stringify(entry.getKey());
            encoder.append(":");
            encoder.stringify(entry.getValue());

            if (++count < size)
                encoder.append(", ");
        }
        encoder.append("]");
    }
}
