package org.mvbus.tests;

import junit.framework.TestCase;
import org.mvbus.MVBus;
import org.mvbus.tests.resources.Person;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class IOTests extends TestCase {
    public void testSerializeToFile() throws IOException {
        Person dhanjiPerson = new Person("Dhanji Prasanna", 1000, new String[]{"DP"});

        File tmpFile = new File(System.getProperty("java.io.tmpdir") + "/mvbus_test.enc");
        tmpFile.createNewFile();

        FileOutputStream outStream = new FileOutputStream(tmpFile);

        MVBus.createBus().encodeToStream(dhanjiPerson, outStream);

        outStream.flush();
        outStream.close();

        FileInputStream inStream = new FileInputStream(tmpFile);

        Person p = MVBus.createBus().decodeFromStream(Person.class, inStream);

        tmpFile.deleteOnExit();

        assertTrue(dhanjiPerson.equals(p));
    }

}
