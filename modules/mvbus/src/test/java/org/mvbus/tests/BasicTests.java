package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.tests.resources.Person;
import org.mvbus.MVBUS;
import org.mvel2.MVEL;

import java.util.HashMap;

public class BasicTests extends TestCase {

    public void testEncodePerson() {
        Person p = new Person("Mike", 30, new String[] { "Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[] { "Mommy", "Mom"});
        Person father = new Person("John", 55, new String[] { "Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);

        String marshalled = MVBUS.marshalPretty(p);

        System.out.println(marshalled);

        Person u = (Person) MVEL.eval(marshalled);

        assertTrue(p.equals(u));
    }

    public void testEncodeHashMap() {
        HashMap map = new HashMap();
        map.put("dhanji", "prasanna");
        map.put("mark", "proctor");
        map.put("mike", "brock");

        String marshalled = MVBUS.marshalPretty(map);

        System.out.println(marshalled);

        HashMap m = (HashMap) MVEL.eval(marshalled);

        assertEquals("prasanna", m.get("dhanji"));
        assertEquals("proctor", m.get("mark"));
        assertEquals("brock", m.get("mike"));
    }

}

