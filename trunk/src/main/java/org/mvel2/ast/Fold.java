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

package org.mvel2.ast;

import org.mvel2.CompileException;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.DefaultLocalVariableResolverFactory;
import org.mvel2.integration.impl.ItemResolverFactory;
import org.mvel2.util.CompilerTools;
import org.mvel2.util.FastList;
import org.mvel2.util.ParseTools;
import static org.mvel2.util.CompilerTools.expectType;
import static org.mvel2.util.ParseTools.*;

import java.util.Collection;
import java.util.List;

public class Fold extends ASTNode {
    private ExecutableStatement subEx;
    private ExecutableStatement dataEx;
    private ExecutableStatement constraintEx;

    public Fold(char[] name, int fields) {
        this.name = name;
        int cursor = 0;
        for (; cursor < name.length; cursor++) {
            if (isWhitespace(name[cursor])) {
                while (cursor < name.length && isWhitespace(name[cursor])) cursor++;

                if (name[cursor] == 'i' && name[cursor + 1] == 'n' && isJunct(name[cursor + 2])) {
                    break;
                }
            }
        }

        subEx = (ExecutableStatement) subCompileExpression(subset(name, 0, cursor - 1));
        int start = cursor += 2; // skip 'in'

        for (; cursor < name.length; cursor++) {
            if (isWhitespace(name[cursor])) {
                while (cursor < name.length && isWhitespace(name[cursor])) cursor++;

                if (name[cursor] == 'i' && name[cursor + 1] == 'f' && isJunct(name[cursor + 2])) {
                    int s = cursor + 2;
                    constraintEx = (ExecutableStatement) subCompileExpression(subset(name, s, name.length - s));
                    break;
                }
            }
        }

        expectType(dataEx = (ExecutableStatement) subCompileExpression(subset(name, start, cursor - start)),
                Collection.class, ((fields & COMPILE_IMMEDIATE) != 0));
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        ItemResolverFactory.ItemResolver itemR = new ItemResolverFactory.ItemResolver("$");
        ItemResolverFactory itemFactory = new ItemResolverFactory(itemR, new DefaultLocalVariableResolverFactory(factory));

        List list;

        if (constraintEx != null) {
            Collection col = ((Collection) dataEx.getValue(ctx, thisValue, factory));
            list = new FastList(col.size());
            for (Object o : col) {
                itemR.value = o;
                if ((Boolean) constraintEx.getValue(ctx, thisValue, itemFactory)) {
                    list.add(subEx.getValue(o, thisValue, itemFactory));
                }
            }
        }
        else {
            Collection col = ((Collection) dataEx.getValue(ctx, thisValue, factory));
            list = new FastList(col.size());
            for (Object o : col) {
                list.add(subEx.getValue(itemR.value = o, thisValue, itemFactory));
            }
        }

        return list;
    }

    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        ItemResolverFactory.ItemResolver itemR = new ItemResolverFactory.ItemResolver("$");
        ItemResolverFactory itemFactory = new ItemResolverFactory(itemR, new DefaultLocalVariableResolverFactory(factory));

        List list;

        if (constraintEx != null) {
            Object x = dataEx.getValue(ctx, thisValue, factory);

            if (!(x instanceof Collection))
                throw new CompileException("was expecting type: Collection; but found type: "
                        + (x == null ? "null" : x.getClass().getName()));

            list = new FastList(((Collection) x).size());
            for (Object o : (Collection) x) {
                itemR.value = o;
                if ((Boolean) constraintEx.getValue(ctx, thisValue, itemFactory)) {
                    list.add(subEx.getValue(o, thisValue, itemFactory));
                }
            }
        }
        else {
            Object x = dataEx.getValue(ctx, thisValue, factory);

            if (!(x instanceof Collection))
                throw new CompileException("was expecting type: Collection; but found type: "
                        + (x == null ? "null" : x.getClass().getName()));

            list = new FastList(((Collection) x).size());
            for (Object o : (Collection) x) {
                list.add(subEx.getValue(itemR.value = o, thisValue, itemFactory));
            }
        }

        return list;
    }

    public Class getEgressType() {
        return Collection.class;
    }
}
