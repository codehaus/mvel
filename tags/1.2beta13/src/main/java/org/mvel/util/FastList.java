package org.mvel.util;

import java.util.AbstractList;

public class FastList extends AbstractList {
    private Object[] elements;
    private int size = 0;


    public FastList(int size) {
        elements = new Object[size];
    }

    public FastList(Object[] elements) {
        this.size = (this.elements = elements).length;
    }

    public Object get(int index) {
        return elements[index];
    }

    public int size() {
        return size;
    }


    public boolean add(Object o) {
        elements[size++] = o;
        return true;
    }
}