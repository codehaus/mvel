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
                "{ name: 'mike', age : 200000, ride: { madeBy: \"Chevy\", vintage_year : 5555 } }";

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

        final Dude dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Dude.class, json);

        assertNotNull(dude);
        assertEquals("mike", dude.getName());
        assertEquals(200000, dude.getAge());

        final Map<String, String> friends = dude.getFriends();
        assertNotNull(friends);

        assertEquals("insane", friends.get("dhanji"));
        assertEquals("cool", friends.get("mic"));
        assertEquals("funny", friends.get("terrence"));

    }

    public static class Car {
        private String madeBy;
        private double vintageYear;

        public String getMadeBy() {
            return madeBy;
        }

        public void setMadeBy(String madeBy) {
            this.madeBy = madeBy;
        }

        public double getVintageYear() {
            return vintageYear;
        }

        public void setVintageYear(double vintageYear) {
            this.vintageYear = vintageYear;
        }
    }

    public static class Dude {
        private String name;
        private int age;
        private List<String> names;
        private Car ride;

        public Map<String, String> getFriends() {
            return friends;
        }

        public void setFriends(Map<String, String> friends) {
            this.friends = friends;
        }

        private Map<String, String> friends = new HashMap<String, String>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public Car getRide() {
            return ride;
        }

        public void setRide(Car ride) {
            this.ride = ride;
        }
    }
}