package org.mvbus.encode;


/**
 * This is the workhorse. Instances of this interface do the actual work of encoding
 * types. While the canonical implementation of this engine will convert Java objects
 * to MVEL script, it is conceivable that a parallel implementation may support other
 * wire formats, for example, Json or XML.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public interface EncodingEngine {
    void encode(Object o);
    
    void stringify(Object o);

    // Create overloads for primitive types.
    void append(String aString);
}
