package org.mvel.sh;

public interface DefaultEnvironment {
    public static final String PROMPT =
            "[${new java.text.SimpleDateFormat('hh:mmaa').format(new java.util.Date(System.currentTimeMillis()))}] mvel$ ";

}
