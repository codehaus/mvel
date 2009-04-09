package org.mvbus;


/**
 * This exception is thrown in the event of a malformed message or failed checksum.
 */
public class BadMessageException extends RuntimeException {
    public BadMessageException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BadMessageException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BadMessageException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BadMessageException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
