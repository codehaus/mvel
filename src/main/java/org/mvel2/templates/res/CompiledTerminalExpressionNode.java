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

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.templates.util.TemplateOutputStream;

import java.io.Serializable;

import static org.mvel2.util.ParseTools.subset;

public class CompiledTerminalExpressionNode extends TerminalExpressionNode {
    private Serializable ce;

    public CompiledTerminalExpressionNode() {
    }

    public CompiledTerminalExpressionNode(Node node) {
        this.begin = node.begin;
        this.name = node.name;
        ce = MVEL.compileExpression(this.contents = node.contents);
    }

    public CompiledTerminalExpressionNode(int begin, String name, char[] template, int start, int end) {
        this.begin = begin;
        this.name = name;
        ce = MVEL.compileExpression(this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1));
    }

    public CompiledTerminalExpressionNode(int begin, String name, char[] template, int start, int end, Node next) {
        this.name = name;
        this.begin = begin;
        ce = MVEL.compileExpression(this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1));
        this.next = next;
    }

    public Object eval(TemplateRuntime runtime, TemplateOutputStream appender, Object ctx, VariableResolverFactory factory) {
        return MVEL.executeExpression(ce, ctx, factory);
    }

    public boolean demarcate(Node terminatingNode, char[] template) {
        return false;
    }
}