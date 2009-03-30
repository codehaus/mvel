package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.tests.resources.Person;
import org.mvbus.MVBUS;

public class BasicTests extends TestCase {

    public void testEncodePerson() {
        Person p = new Person("Mike", 30, new String[] { "Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[] { "Mommy", "Mom"});
        Person father = new Person("John", 55, new String[] { "Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);


        System.out.println(MVBUS.marshalPretty(p));
    }
}
