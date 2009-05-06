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
package org.mvel.optimizers.impl.refl;

import org.mvel.CompileException;
import org.mvel.compiler.AccessorNode;
import org.mvel.integration.VariableResolverFactory;

public class IndexedVariableAccessor implements AccessorNode {
    private AccessorNode nextNode;
    private int register;


    public IndexedVariableAccessor(int register) {
        this.register = register;
    }

    public Object getValue(Object ctx, Object elCtx, VariableResolverFactory vrf) {
        if (vrf == null)
            throw new CompileException("cannot access property in indexed accessor: " + register);

        if (nextNode != null) {
            return nextNode.getValue(vrf.getIndexedVariableResolver(register).getValue(), elCtx, vrf);
        }
        else {
            return vrf.getIndexedVariableResolver(register).getValue();
        }
    }

    public AccessorNode getNextNode() {
        return nextNode;
    }

    public AccessorNode setNextNode(AccessorNode nextNode) {
        return this.nextNode = nextNode;
    }


    public Object setValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory, Object value) {
        variableFactory.getIndexedVariableResolver(register).setValue(value);
        return value;
    }
}