package org.mvel.jdk14testproj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Base {
    public String data = "cat";
    public String number = "101";
    public List list;
    public List things;
    public Boolean fun =  new Boolean( false );
    public String sentence = "The quick brown fox jumps over the lazy dog!";
    public Foo foo = new Foo();

    public boolean ackbar = false;

    public Map funMap = new HashMap();

    public String barfoo;

    public String defnull = null;

    public int sarahl;

    public Object[] testArray = new Object[] { new Foo(), new Bar() };

    public Base() {
        this.list = new ArrayList();
        list.add("Happy");
        list.add("Happy!");
        list.add("Joy");
        list.add("Joy!");

        things = new ArrayList();
        things.add(new Thing("Bob"));
        things.add(new Thing("Smith"));
        things.add(new Thing("Cow"));


        funMap.put("foo", new Foo());
        funMap.put("foo_bar", new Foo());
    }


    public Foo getFoo() {
        return foo;
    }

    public boolean equalityCheck(Object a, Object b) {
        return a.equals(b);
    }
    
    public void populate() {
        barfoo = "sarah";
    }

    public String funMethod(String[] array) {
        return array[0];
    }

    public int sum(int[] nums) {
        int sum = 0;
        for (int i =0, length = nums.length; i < length; i++) {
            sum += i;
        }
        return sum;
    }

    public String readBack(String test) {
        return test;
    }

    public String appendTwoStrings(String a, String b) {
        return a + b;
    }
}
