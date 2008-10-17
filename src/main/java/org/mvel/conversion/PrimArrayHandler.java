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
package org.mvel.conversion;

import org.mvel.ConversionException;
import org.mvel.ConversionHandler;
import static org.mvel.DataConversion.canConvert;
import static org.mvel.DataConversion.convert;
import static org.mvel.util.PropertyTools.getBaseComponentType;

import static java.lang.reflect.Array.*;
import java.util.HashMap;
import java.util.Map;

public class PrimArrayHandler implements ConversionHandler {
    private final Map<Class, Converter> CNV = new HashMap<Class, Converter>();

    private final Class primitiveType;

    public PrimArrayHandler(Class type) {
        this.primitiveType = getBaseComponentType(type);
    }

    public Object convertFrom(Object in) {
        return handleLooseTypeConversion(in.getClass(), in, primitiveType);
    }

    public boolean canConvertFrom(Class cls) {
        return cls.isArray();
    }


    /**
     * Messy method to handle primitive boxing for conversion. If someone can re-write this more
     * elegantly, be my guest.
     *
     * @param sourceType
     * @param input
     * @param targetType
     * @return
     */
    private static Object handleLooseTypeConversion(Class sourceType, Object input, Class targetType) {
        Class targType = getBaseComponentType(targetType);

        int len = getLength(input);
        Object target = newInstance(targType, len);

        if (len > 0 && canConvert(targetType, getBaseComponentType(sourceType))) {
            for (int i = 0; i < len; i++) {
                set(target, i, convert(get(input,i), targType));
            }
        }
        else {
            throw new ConversionException("cannot convert to type: "
                    + targetType.getComponentType().getName() + "[] from " + getBaseComponentType(sourceType).getName());
        }

        return target;
    }
}

