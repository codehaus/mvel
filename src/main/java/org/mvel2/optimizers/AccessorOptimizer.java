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
package org.mvel2.optimizers;

import org.mvel2.ParserContext;
import org.mvel2.compiler.Accessor;
import org.mvel2.integration.VariableResolverFactory;

public interface AccessorOptimizer {
    public void init();

    public Accessor optimizeAccessor(ParserContext pCtx, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory, boolean rootThisRef);

    public Accessor optimizeSetAccessor(ParserContext pCtx, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory, boolean rootThisRef, Object value);

    public Accessor optimizeCollection(Object collectionGraph, Class type, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory);

    public Accessor optimizeObjectCreation(ParserContext pCtx, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory);

    public Object getResultOptPass();

    public Class getEgressType();

    public boolean isLiteralOnly();
}
