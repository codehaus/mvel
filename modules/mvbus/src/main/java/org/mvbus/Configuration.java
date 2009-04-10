package org.mvbus;

import org.mvbus.encode.Encoder;
import org.mvbus.encode.DecodingEngine;
import org.mvbus.encode.engines.mvel.MvelDecodingEngine;
import org.mvbus.encode.types.Encoders;
import org.mvbus.decode.DecodeTools;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public abstract class Configuration {
    // defaults, can be overidden.
    PrintStyle style = PrintStyle.COMPACT;

    Map<Class<?>, Encoder<?>> encoders = Encoders.all();
    Map<Class<?>, Encoder<?>> encoderCache = new HashMap<Class<?>, Encoder<?>>();

    private DecodingEngine decodingEngine = new MvelDecodingEngine();

    static final Configuration DEFAULT = new Configuration() {
        @Override
        protected void configure() {
        }
    };

    /**
     * Override this method to register your own encoders, provide
     * configuration, etc., for the Bus you are about to create.
     * Here are some examples:
     * <pre>
     * <p/>
     *   MvelBus.createBus(new Configuration() {
     * <p/>
     *     {@literal @}Override
     *     protected void configure() {
     *       print(PRETTY);
     * <p/>
     *       encode(Address.class).using(new MyAddressEncoder());
     *       encode(Country.class).using(new YourCountryEncoder());
     *       //etc.
     *     }
     *   });
     */
    protected abstract void configure();

    /**
     * Specify the output format. By default MvelBus will print in
     * {@link org.mvbus.PrintStyle#COMPACT} form.
     */
    protected final void print(PrintStyle style) {
        this.style = style;
    }

    protected final EncoderBindingBuilder encode(final Class<?> type) {
        return new EncoderBindingBuilder() {
            public void using(Encoder encoder) {
                encoders.put(type, encoder);
            }
        };
    }

    protected final void decodeUsing(DecodingEngine decodingEngine) {
       this.decodingEngine = decodingEngine;
    }

    public interface EncoderBindingBuilder {

        void using(Encoder encoder);

    }

    DecodingEngine getDecodingEngine() {
        return decodingEngine;
    }

    // TODO(dhanji): this algorithm can be made more correct by not returning at the first
    // encountered match. What we want is the *narrowest* matching type. Currently, this won't
    // work for subinterfaces properly either (like SortedSet, for example).
    public boolean canEncode(Class<?> clazz) {
        if (clazz == null) return false;
        if (!encoders.containsKey(clazz) && !encoderCache.containsKey(clazz)) {
            do {
                for (Class c : clazz.getInterfaces()) {
                    if (encoders.containsKey(c)) {
                        encoderCache.put(clazz, encoders.get(c));
                        return true;
                    }
                }
            }
            while ((clazz = clazz.getSuperclass()) != null && clazz != Object.class);
            return false;
        }
        else {
            return true;
        }
    }

    public Encoder getEncoder(Class<?> type) {
        if (encoderCache.containsKey(type)) {
            return encoderCache.get(type);
        }
        else {
            return encoders.get(type);
        }
    }

    public PrintStyle getStyle() {
        return style;
    }
}
