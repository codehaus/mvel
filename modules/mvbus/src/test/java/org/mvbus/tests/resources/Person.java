package org.mvbus.tests.resources;

public class Person {
    private String name;
    private int age;
    private String[] nicknames;
    private Person mother;
    private Person father;

    public Person() {
    }

    public Person(String name, int age, String[] nicknames) {
        this.name = name;
        this.age = age;
        this.nicknames = nicknames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String[] getNicknames() {
        return nicknames;
    }

    public void setNicknames(String[] nicknames) {
        this.nicknames = nicknames;
    }

    public Person getMother() {
        return mother;
    }

    public void setMother(Person mother) {
        this.mother = mother;
    }

    public Person getFather() {
        return father;
    }

    public void setFather(Person father) {
        this.father = father;
    }

    public boolean equals(Object o) {
        if (o instanceof Person) {
            Person p = (Person) o;
            return name.equals(p.name) && age == p.age && arrayEquals(nicknames, p.nicknames)
                    && father.equals(p.father) && mother.equals(p.mother);
        }
        return false;
    }

    private boolean arrayEquals(Object[] a1, Object[] a2) {
        if (a1 == null && a2 == null) return true;
        else if (a1 == null || a2 == null) return false;
        else if (a1.length != a2.length) return false;
        else {
            for (int i = 0; i < a1.length; i++) {
                if (!a1[i].equals(a2[i])) return false;
            }

            return true;
        }
    }
}
