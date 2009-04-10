package org.mvbus;

import org.mvbus.encode.engines.mvel.MvelOutstreamEncodingEngine;
import org.mvbus.encode.engines.mvel.MvelSimpleEncodingEngine;
import org.mvbus.encode.contract.mvel.MvelContractEncodingEngine;
import org.mvbus.encode.contract.mvel.MvelContractMessageEncodingEngine;
import org.mvbus.encode.DecodingEngine;
import org.mvbus.util.FunctionAliasResolverFactory;
import org.mvel2.MVEL;
import org.mvel2.MVELInterpretedRuntime;
import org.mvel2.util.ParseTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The front-facing API of MvelBus. All bus transcoding should be done via instances
 * of this abstract class.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public abstract class MVBus {
    private final Configuration config;
    private final DecodingEngine decodingEngine;


    public MVBus(Configuration config) {
        this.config = config;

        this.decodingEngine = config.getDecodingEngine();
    }

    public static MVBus createBus() {
        // build our bus with default configs.
        return createBus(Configuration.DEFAULT);
    }

    public static MVBus createBus(final PrintStyle printStyle) {
        return createBus(new Configuration() {
            protected void configure() {
                print(printStyle);
            }
        });
    }

    public static MVBus createBus(final Configuration config) {
        // Grab the configuration
        config.configure();

        // Now build our bus accordingly.
        return new MVBus(config) {
        };
    }

    // TODO(dhanji): Make the encoding engine pluggable too.
    public <T> Contract createContract(T instance) {
         MvelContractEncodingEngine m = (MvelContractEncodingEngine)
                 new MvelContractEncodingEngine().init(config).encode(instance);

         return new Contract(m.getParameters(), m.getEncoded(), config);
    }

    public <T> void encode(T instance, OutputStream stream) {
        new MvelOutstreamEncodingEngine(stream).init(config).encode(instance).flush();
    }

    public <T> String encode(T instance) {
        return new MvelSimpleEncodingEngine().init(config).encode(instance).getEncoded();
    }

    public <T> T decode(Class<T> type, InputStream instream) throws IOException {
        return decodingEngine.decode(type, ParseTools.readIn(instream, null));
    }

    public <T> T decode(Class<T> type, InputStream instream, String encoding) throws IOException {
        return decodingEngine.decode(type, ParseTools.readIn(instream, encoding));
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(Class<T> type, String script) {
        return decodingEngine.decode(type, script);
    }

    public Object decode(String script) {
        return decodingEngine.decode(Object.class, script);
    }
}
