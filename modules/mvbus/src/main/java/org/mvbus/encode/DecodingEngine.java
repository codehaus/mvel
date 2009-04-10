package org.mvbus.encode;

import org.mvbus.Configuration;
import org.mvbus.util.OutputAppender;


/**
 * This is the workhorse. Instances of this interface do the actual work of decoding
 * types. While the canonical implementation of this engine will convert Java objects
 * to MVEL script, it is conceivable that a parallel implementation may support other
 * wire formats, for example, Json or XML.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public interface DecodingEngine {
    <T> T decode(Class<T> type, String characters);

    <T> T decode(Class<T> type, char[] characters);
}