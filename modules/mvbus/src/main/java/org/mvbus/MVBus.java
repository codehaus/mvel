package org.mvbus;

import org.mvbus.encode.engines.MvelOutstreamEncodingEngine;
import org.mvbus.encode.engines.MvelSimpleEncodingEngine;
import org.mvel2.MVEL;
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

    public MVBus(Configuration config) {
        this.config = config;
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
        return new MVBus(config) {};
    }

    public <T> void encodeToStream(T instance, OutputStream stream) {
        new MvelOutstreamEncodingEngine(stream).init(config).encode(instance).flush();
    }

    public <T> String encode(T instance) {
        return new MvelSimpleEncodingEngine().init(config).encode(instance).getEncoded();
    }

    public <T> T decodeFromStream(Class<T> type, InputStream instream) throws IOException {
        return MVEL.eval(ParseTools.readIn(instream, null), type);
    }

    public <T> T decodeFromStream(Class<T> type, InputStream instream, String encoding) throws IOException {
        return MVEL.eval(ParseTools.readIn(instream, encoding), type);
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(Class<T> type, String script) {
        return MVEL.eval(script, type);
    }
}
