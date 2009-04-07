package org.mvbus.encode;

import org.mvbus.Configuration;
import org.mvbus.util.OutputAppender;


/**
 * This is the workhorse. Instances of this interface do the actual work of encoding
 * types. While the canonical implementation of this engine will convert Java objects
 * to MVEL script, it is conceivable that a parallel implementation may support other
 * wire formats, for example, Json or XML.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public interface EncodingEngine {
    EncodingEngine init(Configuration config);

    EncodingEngine encode(Object o);
    
    EncodingEngine stringify(Object o);

    // Create overloads for primitive types.
    OutputAppender append(String aString);

    String getEncoded();

    /**
     * Flush any active streams.
     */
    void flush();
}
