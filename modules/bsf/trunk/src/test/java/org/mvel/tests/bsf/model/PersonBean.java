package org.mvel.tests.bsf.model;

/**
 * @author Richard L. Burton III
 * @version 1.0
 */
public class PersonBean {

    public static final String HELLO_WORLD = "Hello World!";

    private String name;

    private int age;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String sayHelloWorld() {
        return HELLO_WORLD;
    }

    public String sayHelloWorld(String who) {
        return HELLO_WORLD + " " + who;
    }

    public String sayHelloWorld(int age){
        return HELLO_WORLD + " age: " + age;
    }

}
