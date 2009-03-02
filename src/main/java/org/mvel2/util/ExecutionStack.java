/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mvel2.util;

import org.mvel2.CompileException;
import org.mvel2.math.MathProcessor;
import static org.mvel2.math.MathProcessor.doOperations;

import static java.lang.String.valueOf;

public class ExecutionStack {
    private StackElement element;
    private int size = 0;

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(Object o) {
        size++;
        StackElement el = element;
        if (el != null) {
            while (el.next != null) {
                el = el.next;
            }
            el.next = new StackElement(null, o);
        }
        else {
            element = new StackElement(null, o);
        }
    }

    public void push(Object o) {
        size++;
        element = new StackElement(element, o);
    }

    public Object pushAndPeek(Object o) {
        size++;
        element = new StackElement(element, o);
        return o;
    }

    public void push(Object obj1, Object obj2) {
        size += 2;
        element = new StackElement(new StackElement(element, obj1), obj2);
    }

    public void push(Object obj1, Object obj2, Object obj3) {
        size += 3;
        element = new StackElement(new StackElement(new StackElement(element, obj1), obj2), obj3);
    }

    public Object peek() {
        if (size == 0) return null;
        else return element.value;
    }

    public Boolean peekBoolean() {
        if (size == 0) return null;
        if (element.value instanceof Boolean) return (Boolean) element.value;
        throw new CompileException("expected Boolean; but found: " + (element.value == null ? "null" : element.value.getClass().getName()));
    }

    public void copy2(ExecutionStack es) {
        element = new StackElement(new StackElement(element, es.element.value), es.element.next.value);
        size += 2;
        es.element = es.element.next.next;
        es.size -= 2;
    }

    public void copyx2(ExecutionStack es) {
        element = new StackElement(new StackElement(element, es.element.next.value), es.element.value);
        size += 2;
        es.element = es.element.next.next;
        es.size -= 2;
    }

    public Object peek2() {
        return element.next.value;
    }

    public Object pop() {
        if (size-- == 0) return null;
        try {
            return element.value;
        }
        finally {
            element = element.next;
        }
    }

    public Boolean popBoolean() {
        if (size-- == 0) return null;
        try {
            if (element.value instanceof Boolean) return (Boolean) element.value;
            throw new CompileException("expected Boolean; but found: " + (element.value == null ? "null" : element.value.getClass().getName()));
        }
        finally {
            element = element.next;
        }
    }

    public Object pop2() {
        try {
            size -= 2;
            return element.value;
        }
        finally {
            element = element.next.next;
        }
    }

    public void discard() {
        if (size != 0) {
            size--;
            element = element.next;
        }
    }

    public int size() {
        return size;
    }

    public boolean isReduceable() {
        return size > 1;
    }

    public void clear() {
        size = 0;
        element = null;
    }

    public void xswap_op() {
        xswap();
        op();
    }

    public void op() {
        element = new StackElement(element.next.next.next, doOperations(element.next.next.value, (Integer) element.value, element.next.value));
        size -= 2;
    }

    public void op(int operator) {
        element = new StackElement(element.next.next, doOperations(element.next.value, operator, element.value));
        size--;
    }

    public void xswap() {
        StackElement e = element.next;
        StackElement relink = e.next;
        e.next = element;
        element = e;
        e.next.next = relink;
    }

    public String toString() {
        StackElement el = element;

        if (element == null) return "<EMPTY>";

        StringAppender appender = new StringAppender().append("[");
        do {
            appender.append(valueOf(el.value));
            if (el.next != null) appender.append(", ");
        }
        while ((el = el.next) != null);

        appender.append("]");

        return appender.toString();
    }
}
