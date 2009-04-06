package org.mvbus;

import org.mvbus.encode.Encoder;
import org.mvel2.MVEL;

import java.util.Map;

/**
 * The front-facing API of MvelBus. All bus transcoding should be done via instances
 * of this abstract class.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public abstract class MVBus {
    private final PrintStyle style;
    private final Map<Class<?>, Encoder<?>> encoders;

    public MVBus(PrintStyle style, Map<Class<?>, Encoder<?>> encoders) {
        this.style = style;
        this.encoders = encoders;
    }

    public static MVBus createBus() {

        // build our bus with default configs.
        return createBus(Configuration.DEFAULT);
    }

    public static MVBus createBus(Configuration config) {
        // Grab the configuration
        config.configure();

        // Now build our bus accordingly.
        return new MVBus(config.style, config.encoders) {};
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
        return MVEL.eval(script, type);
    }
}
