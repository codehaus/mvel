package org.mvbus;

import org.mvbus.encode.Encoder;
import org.mvbus.encode.engines.MvelEncodingEngine;
import org.mvel2.MVEL;

import java.util.Map;
import java.io.OutputStream;

/**
 * The front-facing API of MvelBus. All bus transcoding should be done via instances
 * of this abstract class.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public abstract class MVBus {
    private final Configuration config;
//    private final PrintStyle style;
//    private final Map<Class<?>, Encoder<?>> encoders;

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

    public <T> void encodeToString(T instance, OutputStream stream) {

    }

    public <T> String encode(T instance) {
        // TODO(dhanji): inject with a concurrent encoder cache that detects subtypes properly.
        MvelEncodingEngine mve = new MvelEncodingEngine();
        mve.init(config);
        mve.encode(instance);
        return mve.getEncoded();
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(Class<T> type, String script) {
        return MVEL.eval(script, type);
    }
}
