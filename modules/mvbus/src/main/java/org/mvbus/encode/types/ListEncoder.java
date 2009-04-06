package org.mvbus.encode.types;

import org.mvbus.encode.Encoder;
import org.mvbus.EncodingEngine;
import org.mvel2.util.StringAppender;

import java.util.List;

/**
 * A Standard java.util.List encoder. Converts anything contained
 * in the list, recursively.
 */
class ListEncoder implements Encoder<List<?>> {

    public void encode(EncodingEngine encoder, List<?> list) {

        encoder.append("[");
        for (int i = 0; i < list.size(); i++) {
            encoder.stringify(list.get(i));

            if (i + 1 < list.size())
                encoder.append(", ");
        }
        encoder.append("]");
    }
}
