package org.mvbus.tests;

import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;
import org.mvbus.MVBus;
import org.mvbus.tests.resources.Person;
import org.mvel2.MVEL;

public class PerformanceTests extends TestCase {
    public void testCompareToXStream() {
        if (Boolean.getBoolean("mvbus.noperftest")) return;

        Person p = new Person("Mike", 30, new String[]{"Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[]{"Mommy", "Mom"});
        Person father = new Person("John", 55, new String[]{"Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);

        final MVBus bus = MVBus.createBus();

        long time;
        String out;

        for (int x = 0; x < 3; x++) {
            time = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                bus.decode(Person.class, bus.encode(p));
            }
            time = System.currentTimeMillis() - time;

            System.out.println("MVBus Time:" + time);

            XStream xstream = new XStream();  

            time = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                xstream.fromXML(xstream.toXML(p));
            }
            time = System.currentTimeMillis() - time;

            System.out.println("XStream Time:" + time);
        }
    }
}
