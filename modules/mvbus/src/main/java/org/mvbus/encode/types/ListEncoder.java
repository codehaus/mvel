package org.mvbus.encode.types;

import org.mvbus.encode.TypeEncoder;
import org.mvbus.encode.MVBUSEncoder;
import org.mvel2.util.StringAppender;

import java.util.List;

public class ListEncoder implements TypeEncoder {
    public void encode(MVBUSEncoder encoder, Object inst) {
        List<Object> list = (List) inst;
        StringAppender append = encoder.getAppender();

        append.append("[");
        for (int i = 0; i < list.size(); i++) {
            encoder.stringify(list.get(i));
            if (i+1 < list.size()) append.append(", ");
        }
        append.append("]");

    }
}
