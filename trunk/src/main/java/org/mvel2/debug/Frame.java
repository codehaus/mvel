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

package org.mvel2.debug;

import org.mvel2.ParserContext;
import org.mvel2.ast.LineLabel;
import org.mvel2.integration.VariableResolverFactory;

public class Frame {
    private String sourceName;
    private int lineNumber;

    private VariableResolverFactory factory;
    private ParserContext parserContext;

    public Frame(LineLabel label, VariableResolverFactory factory, ParserContext pCtx) {
        this.sourceName = label.getSourceFile();
        this.lineNumber = label.getLineNumber();
        this.factory = factory;
        this.parserContext = pCtx;
    }

    public Frame(String sourceName, int lineNumber, VariableResolverFactory factory, ParserContext pCtx) {
        this.sourceName = sourceName;
        this.lineNumber = lineNumber;
        this.factory = factory;
        this.parserContext = pCtx;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public VariableResolverFactory getFactory() {
        return factory;
    }

    public void setFactory(VariableResolverFactory factory) {
        this.factory = factory;
    }

    public ParserContext getParserContext() {
        return parserContext;
    }

    public void setParserContext(ParserContext parserContext) {
        this.parserContext = parserContext;
    }
}
