package org.mvel.jdk14testproj;

public class Bar {
    private String name = "dog";
    private boolean woof = true;
    private int age = 14;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWoof() {
        return woof;
    }

    public void setWoof(boolean woof) {
        this.woof = woof;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isFoo(Object obj) {
        return obj instanceof Foo;
    }
}
