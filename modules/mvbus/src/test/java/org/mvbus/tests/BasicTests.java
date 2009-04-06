package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.tests.resources.Person;
import org.mvbus.MVBUS;
import org.mvbus.MvelBus;
import org.mvel2.MVEL;

import java.util.HashMap;

public class BasicTests extends TestCase {

    public void testEncodePerson() {
        Person p = new Person("Mike", 30, new String[] { "Dorkus", "Jerkhead"});
        p.setActive(true);

        Person mother = new Person("Sarah", 50, new String[] { "Mommy", "Mom"});
        mother.setActive(false);

        Person father = new Person("John", 55, new String[] { "Dad", "Daddy"});
        mother.setActive(false);

        p.setMother(mother);
        p.setFather(father);

        final MvelBus bus = MvelBus.createBus();
        String marshalled = bus.toMvel(p);

        System.out.println(marshalled);

        Person u = bus.fromMvel(Person.class, marshalled);

        assertTrue(p.equals(u));
    }

    public void testEncodeHashMap() {
        HashMap map = new HashMap();
        map.put("dhanji", "prasanna");
        map.put("mark", "proctor");
        map.put("mike", "brock");

        final MvelBus bus = MvelBus.createBus();
        String marshalled = bus.toMvel(map);

        System.out.println(marshalled);

        HashMap m = bus.fromMvel(HashMap.class, marshalled);

        assertEquals("prasanna", m.get("dhanji"));
        assertEquals("proctor", m.get("mark"));
        assertEquals("brock", m.get("mike"));
    }
}

