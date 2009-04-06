package org.mvbus.encode;

import org.mvbus.encode.EncodingEngine;

/**
 * Converts a Java type to an Mvel script fragment and prints it out to the provided Bus.
 * Used to customize the output of user defined data types.
 *
 * @author brockm@gmail.com
 * @author dhanji@gmail.com
 */
public interface Encoder<T> {
    public void encode(EncodingEngine encoder, T inst);
}
