package org.mvbus;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class MvelBusApiTest extends TestCase {

    public final void testSimpleEncodeAndDecode() {
        Dude dude = new Dude("libertarian", 12, new ArrayList<Dude>());

        final MVBus bus = MVBus.createBus();

        // to & from!
        String script = bus.encode(dude);
        Dude readOut = bus.decode(Dude.class, script);

        assertEquals(dude, readOut);
        System.out.println(script);
    }


    public static class Dude {
        private String affiliation;
        private int age;
        private List<Dude> friends = new ArrayList<Dude>();

        public Dude(String affiliation, int age, List<Dude> friends) {
            this.affiliation = affiliation;
            this.age = age;
            this.friends = friends;
        }

        public void setAffiliation(String affiliation) {
            this.affiliation = affiliation;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setFriends(List<Dude> friends) {
            this.friends = friends;
        }

        public List<Dude> getFriends() {
            return friends;
        }

        public int getAge() {
            return age;
        }

        public String getAffiliation() {
            return affiliation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Dude)) return false;

            Dude dude = (Dude) o;

            if (age != dude.age) return false;
            if (affiliation != null ? !affiliation.equals(dude.affiliation) : dude.affiliation != null)
                return false;
            return !(friends != null ? !friends.equals(dude.friends) : dude.friends != null);

        }
    }
}
