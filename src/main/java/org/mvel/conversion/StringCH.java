package org.mvel.conversion;

import org.mvel.ConversionHandler;
import org.mvel.compiler.BlankLiteral;

import static java.lang.String.valueOf;

public class StringCH implements ConversionHandler {
    public Object convertFrom(Object in) {
        return valueOf(in);
    }


    public boolean canConvertFrom(Class cls) {
        return cls != BlankLiteral.class;
    }
}
