package org.mvbus.encode.engines.json;

import org.mvel2.util.ParseTools;
import org.mvel2.util.StringAppender;
import org.mvel2.util.ReflectionUtil;

import java.util.Map;
import java.util.Stack;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class JsonTransliterator<T> {
    private final Stack<Class<?>> lexicalScopes = new Stack<Class<?>>();
    private final RewriteBridge bridge;

    // state variables for parsing
    private boolean lhs = true;

    private boolean inMap = false;
    private boolean inList = false;

    private int start = 0;
    private String lastIdent;
    private static final String NO_CAPTURE = "";

    JsonTransliterator(Class<T> type, RewriteBridge bridge) {
        this.bridge = bridge;

        this.lexicalScopes.push(type);
    }

    RewriteBridge parse(char[] json) {
        // Nothing fancy, just transliterate to MVEL and then eval this.
        for (int i = 0; i < json.length; i++) {
            char c = json[i];

            if ('{' == c) {
                // push the newly resolve type on to the stack
                Class<?> newScope = resolvePropertyType(lexicalScopes.peek());
                lexicalScopes.push(newScope);

                // If we're dealing with a map, then treat it slightly special
                if (Map.class.isAssignableFrom(newScope)) {

                    bridge.beginMap();
                    inMap = true; //we output slightly different symbols for maps.
                } else {

                    // First new up the class we're interested in.
                    bridge.beginType(newScope);
                }

                // move cursor forward.
                start = i + 1;

                //pretty..

            } else if (':' == c) {
                // time to print an =

                if (lhs) {
                    lhs = false;
                }
                final String capture = capture(json, start, i);

                // NOTE: Fast comparison for base case return code. This is not an error.
                if (!NO_CAPTURE.equals(capture)) {
                    bridge.valueLhs(capture, inMap);
                }

                start = i + 1;
            } else if (',' == c) {

                bridge.valueRhs(capture(json, start, i), true, inList);

                // We don't want to toggle the LHS state if inside a list.
                if (!inList)
                    lhs = true;

                start = i + 1;
            } else if (ParseTools.isWhitespace(c)) {

                // Is there anything really to capture?
                final String capture = capture(json, start, i);

                // Fast base case comparison, Not an error!
                if (!NO_CAPTURE.equals(capture)) {
                    if (lhs) {
                        lhs = false;
                        bridge.valueLhs(capture, inMap);
                    } else
                        bridge.valueRhs(capture, false, inList);

                }

                start = i;
            } else if ('[' == c) {
                inList = true;
                bridge.beginList();
                start = i + 1;

            } else if (']' == c) {
                bridge.endList();
                inList = false;
                start = i + 1;

            } else if ('}' == c) {

                bridge.endType(capture(json, start, i), inMap);

                if (inMap)
                    inMap = false;

                // lexical descend.
                lexicalScopes.pop();

                start = i + 1;
            }
        }

        return bridge;
    }

    // Determines the type of the property being written from the given Java type target.
    private Class<?> resolvePropertyType(Class<?> type) {

        // We're still at the root object.
        if (null == lastIdent || Map.class.isAssignableFrom(type))
            return type;

        return ParseTools.getBestCandidate(new Class[]{ Object.class },
                ReflectionUtil.getSetter(lastIdent), type,
                type.getMethods(), false)

                .getParameterTypes()[0];
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

        return NO_CAPTURE;
    }
}
