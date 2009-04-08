package org.mvbus.encode.contract.mvel;

import org.mvbus.util.OutputAppender;
import org.mvbus.encode.EncodingEngine;
import org.mvbus.encode.engines.mvel.MvelEncodingEngine;
import org.mvel2.util.StringAppender;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MvelContractEncodingEngine extends MvelEncodingEngine {

    private ArrayList<String> parameters = new ArrayList<String>();

    private OutputAppender<StringAppender> output = new OutputAppender<StringAppender>() {
        private StringAppender appender = new StringAppender();

        public OutputAppender append(String str) {
            appender.append(str);
            return this;
        }

        public StringAppender getTarget() {
            return appender;
        }
    };

    //todo: refactor this so there is no code duplication.
    public EncodingEngine encode(Object toEncode) {
        Class encodeClass = toEncode.getClass();

        if (config.canEncode(encodeClass)) {
            config.getEncoder(encodeClass).encode(this, toEncode);
        }
        else {
            try {
                encodeClass.getDeclaredConstructor(EMPTYCLS);
                append("new " + encodeClass.getName() + "().{");
            }
            catch (Exception e) {
                append("instantiate_obj(").append(encodeClass.getName()).append(").{");
            }

            prettyIndent();

            try {
                Field[] fields = encodeClass.getDeclaredFields();
                Object fieldValue;
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    fieldValue = fields[i].get(toEncode);

                    /**
                     * Don't bother initializing null fields.
                     */
                    if (fieldValue == null || (fields[i].getModifiers() & (Modifier.STATIC | Modifier.FINAL)) != 0) {
                        continue;
                    }

                    if (i != 0 && i < fields.length) {
                        append(",");
                        prettyCR();
                    }

                    append(fields[i].getName()).append(pretty ? " = " : "=");

                    parameters.add(fields[i].getName());

                    stringify(null);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("unable to encode", e);
            }
            prettyOutdent();
            append("}");
        }
        return this;
    }

    public EncodingEngine stringify(Object value) {
        append("$_" + (parameters.size()-1));
//        if (value == null) {
//            append("null");
//            return this;
//        }
//        Class type = value.getClass();
//
//        if (String.class.isAssignableFrom(type)) {
//            append("\"").append(String.valueOf(value)).append("\"");
//        }
//        else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
//                || type == Byte.class) {
//            append(String.valueOf(value));
//        }
//        else if (type.isArray()) {
//            append("new ").append(type.getComponentType().getName()).append("[] {");
//            prettyIndent();
//            int length = Array.getLength(value);
//            for (int i = 0; i < length; i++) {
//                stringify(Array.get(value, i));
//                if (i + 1 < length) append(",");
//            }
//            prettyOutdent();
//            append("}");
//        }
//        else if (config.canEncode(type)) {
//            config.getEncoder(type).encode(this, value);
//        }
//        else {
//            encode(value);
//        }
        return this;
    }

//
//    @Override
//    public EncodingEngine stringify(Object value) {
//        append("$_" + offsetIdx++);
//        return this;
//    }

    public String getEncoded() {
        return output.getTarget().toString();
    }

    public OutputAppender append(String str) {
        return output.append(str);
    }

    public void flush() {
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }
}
