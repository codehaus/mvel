package org.mvbus.tests;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import junit.framework.TestCase;
import org.mvbus.MVBus;
import org.mvbus.Contract;
import org.mvbus.Configuration;
import org.mvbus.encode.contract.mvel.MvelContractMessageDecodingEngine;
import org.mvbus.encode.engines.json.JsonDecodingEngine;
import org.mvbus.tests.resources.Person;
import org.mvbus.tests.resources.DensePerson;
import org.mvel2.MVEL;
import org.mvel2.optimizers.OptimizerFactory;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class PerformanceTests extends TestCase {

    public void testJsonDecodeVsXstream() {
        String mikeJson = "{ person: { name: 'Mike', age: 45, " +
                "name2: 'Mike', age2: 45, name3: 'Mike', age3: 45, name4: 'Mike', age4: 45, name5: 'Mike', age5: 45," +
                "name6: 'Mike', age6: 45, name7: 'Mike', age7: 45, name8: 'Mike', age8: 45, name9: 'Mike', age9: 45" +
                " } }";

        final XStream xStream = new XStream(new JettisonMappedXmlDriver());

        xStream.alias("person", DensePerson.class);
        final DensePerson p = (DensePerson) xStream.fromXML(mikeJson);

        final MVBus bus = MVBus.createBus(new Configuration() {
            protected void configure() {
                decodeUsing(new JsonDecodingEngine(false));
            }
        });

        // Validate...
//        assertEquals(p, bus.decode(DensePerson.class, mikeJsonPlain));

        System.out.println(bus.decode(Map.class, mikeJson));

        // Now clock them
        long start = System.currentTimeMillis();
        final int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            xStream.fromXML(mikeJson);
        }

        start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bus.decode(Map.class, mikeJson);
        }
        
        // Now clock them
        start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            xStream.fromXML(mikeJson);
        }
        System.out.println("Xstream Json: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bus.decode(Map.class, mikeJson);
        }
        System.out.println("MVbus Json: " + (System.currentTimeMillis() - start));
        
    }

    public void testCompareToXStream() {
        if (Boolean.getBoolean("mvbus.noperftest")) return;

        Person p = new Person("Mike", 30, new String[]{"Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[]{"Mommy", "Mom"});
        Person father = new Person("John", 55, new String[]{"Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);

        long time;
        String out = null;

        for (int x = 0; x < 3; x++) {

            MVBus bus = MVBus.createBus();
//
//            time = System.currentTimeMillis();
//            for (int i = 0; i < 2; i++) {
//                out = bus.encode(p);
//            }
//            time = System.currentTimeMillis() - time;
//
//            System.out.println("MVBus Encode Time:" + time);
//
//            bus = MVBus.createBus();
//
//            time = System.currentTimeMillis();
//            for (int i = 0; i < 10000; i++) {
//                bus.decode(out);
//            }
//            time = System.currentTimeMillis() - time;
//
//            System.out.println("MVBus Decode Time:" + time);

            Contract c = bus.createContract(p);
            MvelContractMessageDecodingEngine decoder = new MvelContractMessageDecodingEngine();
            decoder.addContract(Person.class.getName(), c.contractString);

       //     OptimizerFactory.setDefaultOptimizer("ASM");

            ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048);


            try {
                time = System.currentTimeMillis();
                for (int i = 0; i < 25000; i++) {
                    c.createMessage(outStream, p);
                    decoder.decode(outStream.toByteArray());
                    outStream.reset();
                }
                time = System.currentTimeMillis() - time;

                System.out.println("MVBus Contract Decode Time:" + time);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            XStream xstream = new XStream();
            out = new XStream().toXML(p);
//
//            time = System.currentTimeMillis();
//            for (int i = 0; i < 2; i++) {
//                out = new XStream().toXML(p);
//            }
//            time = System.currentTimeMillis() - time;
//            System.out.println("XStream Encode Time:" + time);

            xstream = new XStream();


            time = System.currentTimeMillis();
            for (int i = 0; i < 25000; i++) {
                xstream.fromXML(out);
            }
            time = System.currentTimeMillis() - time;

            System.out.println("XStream Decode Time:" + time);
        }
    }
}
