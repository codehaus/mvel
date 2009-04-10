package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.Configuration;
import org.mvbus.MVBus;
import org.mvbus.encode.engines.json.JsonDecodingEngine;

import java.util.List;
import java.util.Arrays;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class JsonDecoderTest extends TestCase {

    public final void testDecodeAJsonString() {
        String json = "{ name:'dhanji', age : 28, names = [] }";

        final Dude dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Dude.class, json);

        assertNotNull(dude);
        assertEquals("dhanji", dude.getName());
        assertEquals(28, dude.getAge());
        assertTrue(dude.getNames().isEmpty());
    }

    public final void testDecodeAJsonStringWithListValues() {
        String json = "{ name:'dhanji', age : 28, names = [ 'dj', 'brockm' ] }";

        final Dude dude = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Dude.class, json);

        assertNotNull(dude);
        assertEquals("dhanji", dude.getName());
        assertEquals(28, dude.getAge());
        assertEquals(2, dude.getNames().size());
        assertEquals(Arrays.asList("dj", "brockm"), dude.getNames());
    }

    public final void testDecodeJsonNormalizeVarsToCamelCase() {
        String json = "{ made_by: 'bmw', vintage_year : 1968.0 }";

        final Car car = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine());
            }
        }).decode(Car.class, json);

        assertNotNull(car);
        assertEquals("bmw", car.getMadeBy());
        assertEquals(1968.0, car.getVintageYear());

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
    }
}
