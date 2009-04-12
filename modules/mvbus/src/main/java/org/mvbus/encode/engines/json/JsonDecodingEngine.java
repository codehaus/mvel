package org.mvbus.encode.engines.json;

import org.mvbus.encode.DecodingEngine;

import java.util.List;
import java.util.Map;

/**
 * The Json flavor of our bus's pluggable decoding system. Expects data to be in Json character
 * format.
 *
 * Not the world's greatest Json implementation but basically transliterates Json into an MVEL
 * script and executes it against the given type.
 * 
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class JsonDecodingEngine implements DecodingEngine {

    @SuppressWarnings("unchecked")
    public <T> T decode(Class<T> type, char[] json) {

        RewriteBridge<T> bridge = (List.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type) )

                ? new MapAndListBridge<T>(type)
                : new MvelBridge<T>(type);

        bridge = new JsonTransliterator<T>(type, bridge)
                .parse(json);


        return bridge.getDecoded(type);

    }

    public <T> T decode(Class<T> type, String characters) {
        return decode(type, characters.toCharArray());
    }

}


