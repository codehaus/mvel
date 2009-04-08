package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.tests.resources.Person;
import org.mvbus.MVBus;
import org.mvbus.PrintStyle;
import org.mvbus.Configuration;
import org.mvel2.MVEL;
import org.mvel2.optimizers.OptimizerFactory;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class BasicTests extends TestCase {

    public void testEncodePerson() {
        Person p = new Person("Mike", 30, new String[]{"Dorkus", "Jerkhead"});
        p.setActive(true);

        Person mother = new Person("Sarah", 50, new String[]{"Mommy", "Mom"});
        mother.setActive(false);

        Person father = new Person("John", 55, new String[]{"Dad", "Daddy"});
        mother.setActive(false);

        p.setMother(mother);
        p.setFather(father);

        MVBus bus = MVBus.createBus(PrintStyle.PRETTY);
        String marshalled = bus.encode(p);

        System.out.println(marshalled);
        Person u = bus.decode(Person.class, marshalled);

        assertTrue(p.equals(u));
    }

    public void testEncodeHashMap() {
        HashMap map = new HashMap();
        map.put("dhanji", "prasanna");
        map.put("mark", "proctor");
        map.put("mike", "brock");

        final MVBus bus = MVBus.createBus();
        String marshalled = bus.encode(map);

        System.out.println(marshalled);

        HashMap m = bus.decode(HashMap.class, marshalled);

        assertEquals("prasanna", m.get("dhanji"));
        assertEquals("proctor", m.get("mark"));
        assertEquals("brock", m.get("mike"));
    }

    public void testCompile1() {
        String toCompile = "org.mvbus.decode.DecodeTools.instantiate(org.mvbus.tests.resources.Person).{\n" +
                "        name = foo,\n" +
                "        age = 30,\n" +
                "        nicknames = new java.lang.String[] {\n" +
                "                \"Dorkus\",\"Jerkhead\"\n" +
                "        },\n" +
                "        mother = org.mvbus.decode.DecodeTools.instantiate(org.mvbus.tests.resources.Person).{\n" +
                "                name = \"Sarah\",\n" +
                "                age = 50,\n" +
                "                nicknames = new java.lang.String[] {\n" +
                "                        \"Mommy\",\"Mom\"\n" +
                "                },\n" +
                "                active = false\n" +
                "        },\n" +
                "        father = org.mvbus.decode.DecodeTools.instantiate(org.mvbus.tests.resources.Person).{\n" +
                "                name = \"John\",\n" +
                "                age = 55,\n" +
                "                nicknames = new java.lang.String[] {\n" +
                "                        \"Dad\",\"Daddy\"\n" +
                "                },\n" +
                "                active = false\n" +
                "        },\n" +
                "        active = true\n" +
                "}";

        OptimizerFactory.setDefaultOptimizer("ASM");

        Serializable s = MVEL.compileExpression(toCompile);

        Map m = new HashMap();
        m.put("foo", "Mike");

        Person p = (Person) MVEL.executeExpression(s, m);

        System.out.println(p);
        p = (Person) MVEL.executeExpression(s, m);

        System.out.println(p);
    }

}

