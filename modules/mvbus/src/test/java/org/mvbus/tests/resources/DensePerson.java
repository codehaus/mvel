package org.mvbus.tests.resources;

public class DensePerson {
    private String name;
    private int age;
    private String name2;
    private int age2;
    private String name3;
    private int age3;
    private String name4;
    private int age4;
    private String name5;
    private int age5;

    private String name6;
    private int age6;
    private String name7;
    private int age7;
    private String name8;
    private int age8;
    private String name9;
    private int age9;
    private String name0;

    public String getName6() {
        return name6;
    }

    public void setName6(String name6) {
        this.name6 = name6;
    }

    public int getAge6() {
        return age6;
    }

    public void setAge6(int age6) {
        this.age6 = age6;
    }

    public String getName7() {
        return name7;
    }

    public void setName7(String name7) {
        this.name7 = name7;
    }

    public int getAge7() {
        return age7;
    }

    public void setAge7(int age7) {
        this.age7 = age7;
    }

    public String getName8() {
        return name8;
    }

    public void setName8(String name8) {
        this.name8 = name8;
    }

    public int getAge8() {
        return age8;
    }

    public void setAge8(int age8) {
        this.age8 = age8;
    }

    public String getName9() {
        return name9;
    }

    public void setName9(String name9) {
        this.name9 = name9;
    }

    public int getAge9() {
        return age9;
    }

    public void setAge9(int age9) {
        this.age9 = age9;
    }

    public String getName0() {
        return name0;
    }

    public void setName0(String name0) {
        this.name0 = name0;
    }

    public int getAge0() {
        return age0;
    }

    public void setAge0(int age0) {
        this.age0 = age0;
    }

    private int age0;

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

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public int getAge2() {
        return age2;
    }

    public void setAge2(int age2) {
        this.age2 = age2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public int getAge3() {
        return age3;
    }

    public void setAge3(int age3) {
        this.age3 = age3;
    }

    public String getName4() {
        return name4;
    }

    public void setName4(String name4) {
        this.name4 = name4;
    }

    public int getAge4() {
        return age4;
    }

    public void setAge4(int age4) {
        this.age4 = age4;
    }

    public String getName5() {
        return name5;
    }

    public void setName5(String name5) {
        this.name5 = name5;
    }

    public int getAge5() {
        return age5;
    }

    public void setAge5(int age5) {
        this.age5 = age5;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DensePerson)) return false;

        DensePerson that = (DensePerson) o;

        if (age != that.age) return false;
        if (age2 != that.age2) return false;
        if (age3 != that.age3) return false;
        if (age4 != that.age4) return false;
        if (age5 != that.age5) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (name2 != null ? !name2.equals(that.name2) : that.name2 != null) return false;
        if (name3 != null ? !name3.equals(that.name3) : that.name3 != null) return false;
        if (name4 != null ? !name4.equals(that.name4) : that.name4 != null) return false;
        if (name5 != null ? !name5.equals(that.name5) : that.name5 != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (name2 != null ? name2.hashCode() : 0);
        result = 31 * result + age2;
        result = 31 * result + (name3 != null ? name3.hashCode() : 0);
        result = 31 * result + age3;
        result = 31 * result + (name4 != null ? name4.hashCode() : 0);
        result = 31 * result + age4;
        result = 31 * result + (name5 != null ? name5.hashCode() : 0);
        result = 31 * result + age5;
        return result;
    }
}