package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.Configuration;
import org.mvbus.MVBus;
import org.mvbus.encode.engines.json.JsonDecodingEngine;
import org.mvel2.MVEL;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class JsonDecoderToMapTest extends TestCase {

    public final void testDecodeAJsonString() {
        String json = "{ name:'dhanji', age : 28, names : [] }";

        final Map dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Map.class, json);

        assertNotNull(dude);
        assertEquals("dhanji", dude.get("name"));
        assertEquals(28, dude.get("age"));
        assertTrue(dude.get("names") instanceof List);
        assertTrue(((List)dude.get("names")).isEmpty());
    }

    public final void testDecodeAJsonStringWithListValues() {
        String json = "{ name:'dhanji', age : 28, names : [ 'dj', '' ] }";

        final Map dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Map.class, json);

        assertNotNull(dude);
        assertEquals("dhanji", dude.get("name"));
        assertEquals(28, dude.get("age"));
        assertTrue(dude.get("names") instanceof List);
        assertEquals(Arrays.asList("dj", ""), dude.get("names"));
    }

    public final void testDecodeJsonNestedObjectGraph() {
        String json =
                "{ name: 'mike', age : 200000, ride: { madeBy: \"Chevy\", vintage_year : 5555.0 } }";

        final Map dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Map.class, json);

        assertNotNull(dude);
        assertEquals("mike", dude.get("name"));
        assertEquals(200000, dude.get("age"));

        assertNotNull(dude.get("ride"));
        Map ride = (Map) dude.get("ride");
        assertEquals("Chevy", ride.get("madeBy"));
        assertEquals(5555.0, ride.get("vintageYear"));

    }


    public final void testDecodeJsonMaps() {
        String json =
                "{ name: 'mike', age : 200000, friends : { " +
                        "'dhanji' : 'insane', " +
                        "'mic' : 'cool', " +
                        "'terrence' : 'funny' " +
                "} }";

        final Map dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Map.class, json);

        assertNotNull(dude);
        assertEquals("mike", dude.get("name"));
        assertEquals(200000, dude.get("age"));

        final Map<String, Object> friends = (Map<String, Object>) dude.get("friends");
        assertNotNull(friends);

        assertEquals("insane", friends.get("dhanji"));
        assertEquals("cool", friends.get("mic"));
        assertEquals("funny", friends.get("terrence"));

    }
}