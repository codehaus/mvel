package org.mvbus.encode.engines.json;

import org.mvbus.Configuration;
import org.mvbus.encode.DecodingEngine;
import org.mvel2.MVEL;
import org.mvel2.util.ParseTools;
import org.mvel2.util.StringAppender;

import java.util.HashMap;

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

        StringAppender mvel = new JsonTransliterator<T>(type)
                .parse(json);

        System.out.println(mvel.toString());

        return (T) MVEL.eval(mvel.toString(), new HashMap<String, Object>());

    }

    public <T> T decode(Class<T> type, String characters) {
        return decode(type, characters.toCharArray());
    }

}


