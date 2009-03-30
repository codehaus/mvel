package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.tests.resources.Person;
import org.mvbus.MVBUS;
import com.thoughtworks.xstream.XStream;

public class PerformanceTests extends TestCase {
    public void testCompareToXStream() {
        Person p = new Person("Mike", 30, new String[]{"Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[]{"Mommy", "Mom"});
        Person father = new Person("John", 55, new String[]{"Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);

        long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            MVBUS.marshal(p);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("MVBus Time:" + time);

        XStream xstream = new XStream();

        time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            xstream.toXML(p);
        }
        time = System.currentTimeMillis() - time;

        System.out.println("XStream Time:" + time);

    }
}
