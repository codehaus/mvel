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
package org.mvel2.optimizers.impl.refl.nodes;

import org.mvel2.CompileException;
import static org.mvel2.MVEL.getProperty;
import org.mvel2.compiler.AccessorNode;
import org.mvel2.integration.VariableResolverFactory;

import java.lang.reflect.Method;

public class GetterAccessor implements AccessorNode {
    private AccessorNode nextNode;
    private final Method method;

    public static final Object[] EMPTY = new Object[0];

    public Object getValue(Object ctx, Object elCtx, VariableResolverFactory vars) {
        try {
            if (nextNode != null) {
                return nextNode.getValue(method.invoke(ctx, EMPTY), elCtx, vars);
            }
            else {
                return method.invoke(ctx, EMPTY);
            }
        }
        catch (IllegalArgumentException e) {
            /**
             * HACK: Try to access this another way.
             */

            if (nextNode != null) {
                return nextNode.getValue(getProperty(method.getName() + "()", ctx), elCtx, vars);
            }
            else {
                return getProperty(method.getName() + "()", ctx);
            }
        }
        catch (Exception e) {
            throw new CompileException("cannot invoke getter: " + method.getName()
                    + " [declr.class: " + method.getDeclaringClass().getName() + "; act.class: "
                    + (ctx != null ? ctx.getClass().getName() : "null") + "]", e);
        }
    }

    public GetterAccessor(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public AccessorNode setNextNode(AccessorNode nextNode) {
        return this.nextNode = nextNode;
    }

    public AccessorNode getNextNode() {
        return nextNode;
    }

    public String toString() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public Object setValue(Object ctx, Object elCtx, VariableResolverFactory vars, Object value) {
        try {
            if (nextNode != null) {
                return nextNode.setValue(method.invoke(ctx, EMPTY), elCtx, vars, value);
            }
            else {
                throw new CompileException("bad payload");
            }
        }
        catch (IllegalArgumentException e) {
            /**
             * HACK: Try to access this another way.
             */

            if (nextNode != null) {
                return nextNode.setValue(getProperty(method.getName() + "()", ctx), elCtx, vars, value);
            }
            else {
                return getProperty(method.getName() + "()", ctx);
            }
        }
        catch (CompileException e) {
            throw e;
        }
        catch (Exception e) {
            throw new CompileException("error " + method.getName() + ": " + e.getClass().getName() + ":" + e.getMessage(), e);
        }
    }

    public Class getKnownEgressType() {
        return method.getReturnType();
    }
}
