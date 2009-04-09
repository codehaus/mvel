package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.tests.resources.Person;
import org.mvbus.MVBus;
import org.mvbus.Contract;
import org.mvbus.encode.contract.mvel.MvelContractMessageDecodingEngine;

import java.io.*;

public class ContractTests extends TestCase {
    public void testSimpleContract() throws IOException {
        Person p = new Person("Mike", 30, new String[]{"Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[]{"Mommy", "Mom"});
        Person father = new Person("John", 55, new String[]{"Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);

        Contract contract = MVBus.createBus().createContract(p);

        System.out.println("contract:" + contract.contractString);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048);

        contract.createMessage(outStream, p);

        MvelContractMessageDecodingEngine decoder = new MvelContractMessageDecodingEngine();
        decoder.addContract(Person.class.getName(), contract.contractString);

        Object o = decoder.decode(outStream.toByteArray());

        assertTrue(p.equals(o));
    }

    public void testContractToDisk() throws IOException {
        Person p = new Person("Mike", 30, new String[]{"Dorkus", "Jerkhead"});
        Person mother = new Person("Sarah", 50, new String[]{"Mommy", "Mom"});
        Person father = new Person("John", 55, new String[]{"Dad", "Daddy"});

        p.setMother(mother);
        p.setFather(father);

        Contract contract = MVBus.createBus().createContract(p);

        File tmpFile = new File(System.getProperty("java.io.tmpdir") + "/mvbus_contract_test.enc");
        tmpFile.createNewFile();

        FileOutputStream outStream = new FileOutputStream(tmpFile);

        contract.createMessage(outStream, p, true);

        MvelContractMessageDecodingEngine decoder = new MvelContractMessageDecodingEngine();
        decoder.addContract(Person.class.getName(), contract.contractString);

        FileInputStream inStream = new FileInputStream(tmpFile);

        Object o = decoder.decode(inStream);

        assertTrue(p.equals(o));
    }
}
