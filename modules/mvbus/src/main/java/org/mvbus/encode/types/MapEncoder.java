package org.mvbus.encode.types;

import org.mvbus.encode.TypeEncoder;
import org.mvbus.encode.MVBUSEncoder;
import org.mvel2.util.StringAppender;

import java.util.Map;

public class MapEncoder implements TypeEncoder {
    public void encode(MVBUSEncoder encoder, Object inst) {
        Map<Object,Object> map = (Map) inst;
        StringAppender a = encoder.getAppender();

        a.append("[");

        int size = map.size();
        int count = 0;
        for (Map.Entry<Object,Object> entry : map.entrySet()) {
            encoder.stringify(entry.getKey());
            a.append(":");
            encoder.stringify(entry.getValue());

            if (++count < size) a.append(", ");
        }
        a.append("]");
    }
}
