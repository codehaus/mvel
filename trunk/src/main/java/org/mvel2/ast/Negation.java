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
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.VariableResolverFactory;
import static org.mvel2.util.ParseTools.subCompileExpression;

public class Negation extends ASTNode {
    private ExecutableStatement stmt;

    public Negation(char[] name, int fields, ParserContext pCtx) {
        this.name = name;

        if ((fields & COMPILE_IMMEDIATE) != 0) {
            if ((this.stmt = (ExecutableStatement) subCompileExpression(name, pCtx)).getKnownEgressType() != null
                    && (!stmt.getKnownEgressType().isAssignableFrom(Boolean.class))) {
                throw new CompileException("negation operator cannot be applied to non-boolean type");
            }
        }
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        return !((Boolean) stmt.getValue(ctx, thisValue, factory));
    }

    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        try {
            return !((Boolean) MVEL.eval(name, ctx, factory));
        }
        catch (NullPointerException e) {
            throw new CompileException("negation operator applied to a null value");
        }
        catch (ClassCastException e) {
            throw new CompileException("negation operator applied to non-boolean expression");
        }
    }

    public Class getEgressType() {
        return Boolean.class;
    }

}
