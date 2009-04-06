package org.mvbus;

import org.mvbus.encode.Encoder;
import org.mvbus.encode.types.Encoders;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public abstract class Configuration {
    // defaults, can be overidden.
    PrintStyle style = PrintStyle.COMPACT;
    Map<Class<?>, Encoder<?>> encoders = Encoders.all();

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
     *
     *   MvelBus.createBus(new Configuration() {
     *
     *     {@literal @}Override
     *     protected void configure() {
     *       print(PRETTY);
     *
     *       encode(Address.class).using(new MyAddressEncoder());
     *       encode(Country.class).using(new YourCountryEncoder());
     *       //etc.
     *     }
     *   });
     *
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

    public interface EncoderBindingBuilder {
        void using(Encoder encoder);
    }
}
