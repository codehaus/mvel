package org.mvbus.encode.engines.mvel;

import org.mvbus.encode.DecodingEngine;
import org.mvbus.util.FunctionAliasResolverFactory;
import org.mvel2.MVELInterpretedRuntime;
import org.mvel2.MVEL;

/**
 * The MVEL flavor of a wire protocol decoder. Assumes the data coming across the wire is in
 * MVEL script form.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class  MvelDecodingEngine implements DecodingEngine {
    private static final FunctionAliasResolverFactory factory = new FunctionAliasResolverFactory();

    @SuppressWarnings("unchecked")
    public <T> T decode(Class<T> type, String script) {
        return (T) new MVELInterpretedRuntime(script, null, factory).parse();
    }

    public <T> T decode(Class<T> type, char[] characters) {
        return MVEL.eval(characters, factory, type);
    }
}
