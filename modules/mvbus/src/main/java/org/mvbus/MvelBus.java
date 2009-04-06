package org.mvbus;

import org.mvbus.MVBUSEncoder;
import org.mvbus.encode.Encoder;
import org.mvel2.MVEL;

import java.util.Map;

/**
 * The front-facing API of MvelBus. All bus transcoding should be done via instances
 * of this abstract class.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public abstract class MvelBus {
    private final PrintStyle style;
    private final Map<Class<?>, Encoder<?>> encoders;

    public MvelBus(PrintStyle style, Map<Class<?>, Encoder<?>> encoders) {
        this.style = style;
        this.encoders = encoders;
    }

    public static MvelBus createBus() {

        // build our bus with default configs.
        return createBus(Configuration.DEFAULT);
    }

    public static MvelBus createBus(Configuration config) {
        // Grab the configuration
        config.configure();

        // Now build our bus accordingly.
        return new MvelBus(config.style, config.encoders) {};
    }

    public <T> String toMvel(T instance) {
        // TODO(dhanji): inject with a concurrent encoder cache that detects subtypes properly.
        MVBUSEncoder mve = new MVBUSEncoder();
        if (PrintStyle.PRETTY == style) {
            mve.setPretty(true);
        }
        mve.encode(instance);
        return mve.getEncoded();
    }

    @SuppressWarnings("unchecked")
    public <T> T fromMvel(Class<T> type, String script) {
        return (T) MVEL.eval(script);
    }
}
