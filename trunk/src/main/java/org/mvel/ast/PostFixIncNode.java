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
package org.mvel.ast;

import org.mvel.Operator;
import org.mvel.integration.VariableResolver;
import org.mvel.integration.VariableResolverFactory;
import static org.mvel.util.ParseTools.doOperations;

/**
 * @author Christopher Brock
 */
public class PostFixIncNode extends ASTNode {
    private String name;

    public PostFixIncNode(String name) {
        this.name = name;
    }

    public PostFixIncNode(char[] expr, int fields) {
        super(expr, fields);
        name = new String(expr);
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        VariableResolver vResolver = factory.getVariableResolver(name);
        vResolver.setValue(doOperations(ctx = vResolver.getValue(), Operator.ADD, 1));
        return ctx;
    }

    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        return getReducedValueAccelerated(ctx, thisValue, factory);
    }
}
