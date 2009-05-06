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
package org.mvel2.ast;

import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import static org.mvel2.util.CompilerTools.expectType;
import static org.mvel2.util.ParseTools.subCompileExpression;

import java.util.HashMap;

/**
 * @author Christopher Brock
 */
public class DoUntilNode extends BlockNode {
    protected String item;
    protected ExecutableStatement condition;
    protected ExecutableStatement compiledBlock;

    public DoUntilNode(char[] condition, char[] block) {
        expectType(this.condition = (ExecutableStatement) subCompileExpression(this.name = condition),
                Boolean.class, ((fields & COMPILE_IMMEDIATE) != 0));

        this.compiledBlock = (ExecutableStatement) subCompileExpression(this.block = block);
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        VariableResolverFactory lc = new MapVariableResolverFactory(new HashMap(0), factory);

        do {
            compiledBlock.getValue(ctx, thisValue, lc);
        }
        while (!(Boolean) condition.getValue(ctx, thisValue, lc));

        return null;
    }

    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        VariableResolverFactory lc = new MapVariableResolverFactory(new HashMap(0), factory);

        do {
            compiledBlock.getValue(ctx, thisValue, lc);
        }
        while (!(Boolean) condition.getValue(ctx, thisValue, lc));

        return null;
    }

}