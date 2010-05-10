/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
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
 */

package org.mvel2.templates.res;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.templates.TemplateRuntimeError;
import org.mvel2.templates.util.ArrayIterator;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.util.ParseTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CompiledForEachNode extends Node {
    public Node nestedNode;
    private Serializable[] ce;

    private String[] item;
    private String[] expression;

    private char[] sepExpr;
    private Serializable cSepExpr;

    public CompiledForEachNode() {
    }

    public CompiledForEachNode(int begin, String name, char[] template, int start, int end) {
        super(begin, name, template, start, end);
        configure();
    }

    public Node getNestedNode() {
        return nestedNode;
    }

    public void setNestedNode(Node nestedNode) {
        this.nestedNode = nestedNode;
    }

    public boolean demarcate(Node terminatingnode, char[] template) {
        nestedNode = next;
        next = terminus;

        sepExpr = terminatingnode.getContents();
        if (sepExpr.length == 0) {
            sepExpr = null;
        }
        else {
            cSepExpr = MVEL.compileExpression(sepExpr);
        }

        return false;
    }

    public Object eval(TemplateRuntime runtime, TemplateOutputStream appender, Object ctx, VariableResolverFactory factory) {
        Iterator[] iters = new Iterator[item.length];

        Object o;
        for (int i = 0; i < iters.length; i++) {
            if ((o = MVEL.executeExpression(ce[i], ctx, factory)) instanceof Iterable) {
                iters[i] = ((Iterable) o).iterator();
            }
            else if (o instanceof Object[]) {
                iters[i] = new ArrayIterator((Object[]) o);
            }
            else {
                throw new TemplateRuntimeError("cannot iterate object type: " + o.getClass().getName());
            }
        }

        Map<String, Object> locals = new HashMap<String, Object>();
        MapVariableResolverFactory localFactory = new MapVariableResolverFactory(locals, factory);

        int iterate = iters.length;

        while (true) {
            for (int i = 0; i < iters.length; i++) {
                if (!iters[i].hasNext()) {
                    iterate--;
                    locals.put(item[i], "");
                }
                else {
                    locals.put(item[i], iters[i].next());
                }
            }
            if (iterate != 0) {
                nestedNode.eval(runtime, appender, ctx, localFactory);

                if (sepExpr != null) {
                    for (Iterator it : iters) {
                        if (it.hasNext()) {
                            appender.append(String.valueOf(MVEL.executeExpression(cSepExpr, ctx, factory)));
                            break;
                        }
                    }
                }
            }
            else break;
        }

        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    private void configure() {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> expr = new ArrayList<String>();

        int start = 0;
        for (int i = 0; i < contents.length; i++) {
            switch (contents[i]) {
                case ':':
                    items.add(ParseTools.createStringTrimmed(contents, start, i - start));
                    start = i + 1;
                    break;
                case ',':
                    if (expr.size() != (items.size() - 1)) {
                        throw new CompileException("unexpected character ',' in foreach tag", cStart + i);
                    }
                    expr.add(ParseTools.createStringTrimmed(contents, start, i - start));
                    start = i + 1;
                    break;
            }
        }

        if (start < contents.length) {
            if (expr.size() != (items.size() - 1)) {
                throw new CompileException("expected character ':' in foreach tag", cEnd);
            }
            expr.add(ParseTools.createStringTrimmed(contents, start, contents.length - start));
        }

        item = new String[items.size()];
        int i = 0;
        for (String s : items) item[i++] = s;

        ce = new Serializable[(expression = new String[expr.size()]).length];
        i = 0;
        for (String s : expr) {
            ce[i] = MVEL.compileExpression(expression[i++] = s);
        }
    }
}