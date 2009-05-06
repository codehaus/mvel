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
package org.mvel2.optimizers.impl.refl.collection;

import org.mvel2.CompileException;
import org.mvel2.ParserContext;
import static org.mvel2.DataConversion.canConvert;
import static org.mvel2.DataConversion.convert;
import org.mvel2.compiler.Accessor;
import org.mvel2.compiler.ExecutableLiteral;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.util.ParseTools;
import static org.mvel2.util.ParseTools.getSubComponentType;

/**
 * @author Christopher Brock
 */
public class ExprValueAccessor implements Accessor {
    public ExecutableStatement stmt;

    public ExprValueAccessor(String ex) {
        stmt = (ExecutableStatement) ParseTools.subCompileExpression(ex.toCharArray());
    }

    public ExprValueAccessor(String ex, Class expectedType, boolean strongType) {
        stmt = (ExecutableStatement) ParseTools.subCompileExpression(ex.toCharArray());

        //if (expectedType.isArray()) {
        Class tt = getSubComponentType(expectedType);
        Class et = stmt.getKnownEgressType();
        if (stmt.getKnownEgressType() != null && !tt.isAssignableFrom(et)) {
            if (!strongType && (stmt instanceof ExecutableLiteral) && canConvert(et, tt)) {
                try {
                    stmt = new ExecutableLiteral(convert(stmt.getValue(null, null), tt));
                    return;
                }
                catch (IllegalArgumentException e) {
                    // fall through;
                }
            }
            throw new CompileException("was expecting type: " + tt + "; but found type: " + (et == null ? "null" : et.getName()));
        }
    }


    public Object getValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory) {
        return stmt.getValue(elCtx, variableFactory);
    }

    public Object setValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory, Object value) {
        // not implemented
        return null;
    }

    public ExecutableStatement getStmt() {
        return stmt;
    }

    public void setStmt(ExecutableStatement stmt) {
        this.stmt = stmt;
    }

    public Class getKnownEgressType() {
        return stmt.getKnownEgressType();
    }
}
