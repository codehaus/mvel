package org.mvel.tests.main.res;

public class PojoStatic {
    private String value;

    public PojoStatic(String value) {
        this.value = value;
    }

    public PojoStatic() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String string) {
        this.value = string;
    }
}
