package org.mvbus.encode.engines.json;

import org.mvel2.util.ParseTools;
import org.mvel2.util.StringAppender;
import org.mvel2.util.ReflectionUtil;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class JsonTransliterator<T> {
    private final StringAppender mvel;
    private final Class<T> type;

    // state variables for parsing
    private boolean lhs = true;
    private boolean inList = false;

    private int start = 0;

    private String lastIdent;

    JsonTransliterator(Class<T> type) {
        this.mvel = new StringAppender();
        this.type = type;
    }

    StringAppender parse(Class<?> type, char[] json) {
        // Nothing fancy, just transliterate to MVEL and then eval this.
        for (int i = 0; i < json.length; i++) {
            char c = json[i];

            if ('{' == c) {
                // First new up the class we're interested in.
                mvel.append(" org.mvbus.decode.DecodeTools.instantiate( ");
                mvel.append(resolvePropertyType(type));
                mvel.append(" ).{ ");

                // move cursor forward.
                start = i + 1;

                //pretty..

            } else if (':' == c) {
                // time to print an =

                if (lhs) {
                    lhs = false;
                }
                mvel.append(capture(json, start, i));
                mvel.append('=');

                start = i + 1;
            } else if (',' == c) {

                mvel.append(capture(json, start, i));

                // We don't want to loggle the LHS state if inside a list.
                mvel.append(',');

                start = i + 1;
            } else if (ParseTools.isWhitespace(c)) {

                // Is there anything really to capture?
                final String capture = capture(json, start, i);

                if (capture.length() > 0) {
                    if (lhs) {
                        lhs = false;
                    }

                    mvel.append(capture);
                }

                start = i;
            } else if ('[' == c) {
                inList = true;
            } else if (']' == c) {
                inList = false;
            } else if ('}' == c) {

                mvel.append(capture(json, start, i));
                mvel.append(" } ");

                start = i + 1;
            }
        }

        return mvel;
    }

    // Determines the type of the property being written from the given Java type target.
    private String resolvePropertyType(Class<?> type) {

        // We're still at the root object.
        if (null == lastIdent)
            return type.getName();

        return ParseTools.getBestCandidate(new Class[]{ Object.class }, 
                ReflectionUtil.getSetter(lastIdent), type,
                type.getMethods(), false)

                .getParameterTypes()[0]
                .getName();
    }


    private String capture(char[] json, int start, int end) {
        // tokenize if we hit a delimiter
        if (start < end) {

            // Scan beginning of string to detect if it is an identifier or not.
            boolean identStart = false;
            for (int i = start; i < end; i++, start++) {
                char c = json[i];

                if (Character.isJavaIdentifierPart(c)) {
                    identStart = true;
                    break;
                } else if (ParseTools.isWhitespace(c) && !identStart) {
                    // leading whitespace, ignore.
                } else {
                    // Some other character, probably a literal or delimiter. time to give up.
                    break;
                }
            }

            if (!identStart) {
                return new String(json, start, end - start).trim();
            }

            // otherwise convert to camel case!
            StringAppender buffer = new StringAppender();
            for (int i = start; i < end; i++) {
                char c = json[i];

                if ('_' == c) {
                    buffer.append(Character.toUpperCase(json[++i]));
                } else {
                    buffer.append(c);
                }

            }

            return lastIdent = buffer.toString().trim();
        }

        return "";
    }
}
