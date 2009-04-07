package org.mvbus.util;


/**
 * A simple interface to create an abstract layer between the engine and the output method.
 */
public interface OutputAppender<T> {
    public OutputAppender append(String str);
    public T getTarget();
}
