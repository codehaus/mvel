package org.mvbus.encode.engines.json;

import org.mvbus.Configuration;
import org.mvbus.encode.DecodingEngine;
import org.mvel2.MVEL;
import org.mvel2.util.ParseTools;
import org.mvel2.util.StringAppender;

import java.util.HashMap;

/**
 * The Json flavor of our bus's pluggable decoding system. Expects data to be in Json character
 * format.
 *
 * Not the world's greatest Json implementation but basically transliterates Json into an MVEL
 * script and executes it against the given type.
 * 
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class JsonDecodingEngine implements DecodingEngine {

    public <T> T decode(Class<T> type, char[] json) {

        StringAppender mvel = new StringAppender();

        String var = "target"; // Name of current object in graph


        // Nothing fancy, just transliterate to MVEL and then eval this.
        boolean lhs = true;
        boolean inList = false;
        int start = 0;
        for (int i = 0; i < json.length; i++) {
            char c = json[i];

            if ('{' == c) {
                // First new up the class we're interested in.
                mvel.append(var);
                mvel.append(" = new ");
                mvel.append(type.getName());
                mvel.append("();");

                // move cursor forward.
                start = i + 1;

                //pretty..

            } else if (':' == c) {
                // time to print an =

                if (lhs) {
                    mvel.append(var);
                    mvel.append('.');
                    lhs = false;
                }
                mvel.append(capture(json, start, i));
                mvel.append('=');

                start = i + 1;
            } else if (',' == c || '}' == c) {

                mvel.append(capture(json, start, i));

                // We don't want to loggle the LHS state if inside a list.
                if (inList) {
                    mvel.append(',');
                } else {
                    mvel.append(';');
                    lhs = true;
                }

                start = i + 1;
            } else if (ParseTools.isWhitespace(c)) {

                // Is there anything really to capture?
                final String capture = capture(json, start, i);

                if (capture.length() > 0) {
                    if (lhs) {
                        mvel.append(var);
                        mvel.append('.');
                        lhs = false;
                    }

                    mvel.append(capture);
                }

                start = i;
            } else if ('[' == c) {
                inList = true;
            } else if (']' == c) {
                inList = false;
            }


        }

        // we must push var onto the stack so it is the last thing returned.
        mvel.append(var);

        return (T) MVEL.eval(mvel.toString(), new HashMap<String, Object>());

    }

    public <T> T decode(Class<T> type, String characters) {
        return decode(type, characters.toCharArray());
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

            return buffer.toString().trim();
        }

        return "";
    }
}


