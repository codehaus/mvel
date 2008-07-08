/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
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
 *
 */
package org.mvel;

import org.mvel.conversion.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class DataConversion {
    private static final Map<Class, ConversionHandler> CONVERTERS
            = new HashMap<Class, ConversionHandler>();

    static {
        ConversionHandler ch;

        CONVERTERS.put(Integer.class, ch = new IntegerCH());
        CONVERTERS.put(int.class, ch);

        CONVERTERS.put(Short.class, ch = new ShortCH());
        CONVERTERS.put(short.class, ch);

        CONVERTERS.put(Long.class, ch = new LongCH());
        CONVERTERS.put(long.class, ch);

        CONVERTERS.put(Character.class, ch = new CharCH());
        CONVERTERS.put(char.class, ch);

        CONVERTERS.put(Byte.class, ch = new ByteCH());
        CONVERTERS.put(byte.class, ch);

        CONVERTERS.put(Float.class, ch = new FloatCH());
        CONVERTERS.put(float.class, ch);

        CONVERTERS.put(Double.class, ch = new DoubleCH());
        CONVERTERS.put(double.class, ch);

        CONVERTERS.put(Boolean.class, ch = new BooleanCH());
        CONVERTERS.put(boolean.class, ch);

        CONVERTERS.put(String.class, new StringCH());

        CONVERTERS.put(Object.class, new ObjectCH());

        CONVERTERS.put(char[].class, new CharArrayCH());

        CONVERTERS.put(String[].class, new StringArrayCH());

        CONVERTERS.put(Integer[].class, new IntArrayCH());

        CONVERTERS.put(int[].class, new PrimArrayHandler(int[].class));
        CONVERTERS.put(long[].class, new PrimArrayHandler(long[].class));
        CONVERTERS.put(double[].class, new PrimArrayHandler(double[].class));
        CONVERTERS.put(float[].class, new PrimArrayHandler(float[].class));
        CONVERTERS.put(short[].class, new PrimArrayHandler(short[].class));
        CONVERTERS.put(boolean[].class, new PrimArrayHandler(boolean[].class));

        CONVERTERS.put(BigDecimal.class, new BigDecimalCH());
        CONVERTERS.put(BigInteger.class, new BigIntegerCH());
    }

    public static boolean canConvert(Class toType, Class convertFrom) {
        return (CONVERTERS.containsKey(toType)
                && CONVERTERS.get(toType).canConvertFrom(convertFrom))
                || toType.isAssignableFrom(convertFrom);
    }

    public static <T> T convert(Object in, Class<T> toType) {
        if (in == null) return null;
        if (toType == in.getClass() || toType.isAssignableFrom(in.getClass())) {
            return (T) in;
        }
        return (T) CONVERTERS.get(toType).convertFrom(in);
    }

    public static void addConversionHandler(Class type, ConversionHandler handler) {
        CONVERTERS.put(type, handler);
    }
}
