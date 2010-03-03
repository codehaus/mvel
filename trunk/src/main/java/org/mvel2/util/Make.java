package org.mvel2.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Make {
    public static class Map<K, V> {
        public static <K,V> Map<K,V> s() {
            return start();
        }

        public static <K, V> Map<K, V> start() {
            return start(HashMap.class);
        }

        public static <K, V> Map<K, V> start(Class<? extends java.util.Map> mapImpl) {
            try {
                return new Map(mapImpl.newInstance());
            }
            catch (Throwable t) {
                throw new RuntimeException("error creating instance", t);
            }
        }

        private java.util.Map<K,V> mapInstance;

        private Map(java.util.Map<K,V> mapInstance) {
            this.mapInstance = mapInstance;
        }

        public Map<K, V> _(K key, V value) {
            mapInstance.put(key, value);
            return this;
        }

        public java.util.Map<K,V> f() {
            return finish();
        }

        public java.util.Map<K,V> finish() {
            return mapInstance;
        }
    }

    public static class String {
        public static String s() {
            return start();
        }

        public static String start() {
            return new String(new StringAppender());
        }

        public java.lang.String f() {
            return finish();
        }

        public java.lang.String finish() {
            return stringAppender.toString();
        }

        private StringAppender stringAppender;

        String(StringAppender stringAppender) {
            this.stringAppender = stringAppender;
        }

        public String _(char c) {
            stringAppender.append(c);
            return this;
        }

        public String _(CharSequence cs) {
            stringAppender.append(cs);
            return this;
        }

        public String _(String s) {
            stringAppender.append(s);
            return this;
        }
    }

    public static class List<V> {
        public static List s() {
            return start();
        }

        public static List start() {
            return start(ArrayList.class);
        }

        public static <V> List<V> start(Class<? extends java.util.List> listImpl) {
            try {
                return new List(listImpl.newInstance());
            }
            catch (Throwable t) {
                throw new RuntimeException("error creating instance", t);
            }
        }

        private java.util.List<V> listInstance;

        List(java.util.List<V> listInstance) {
            this.listInstance = listInstance;
        }

        public List<V> _(V value) {
            listInstance.add(value);
            return this;
        }

        public java.util.List<V> f() {
            return finish();
        }

        public java.util.List<V> finish() {
            return listInstance;
        }
    }
}
