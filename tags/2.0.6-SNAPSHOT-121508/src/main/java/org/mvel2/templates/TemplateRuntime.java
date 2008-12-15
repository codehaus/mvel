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

package org.mvel2.templates;

import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import static org.mvel2.templates.TemplateCompiler.compileTemplate;
import org.mvel2.templates.res.Node;
import org.mvel2.templates.util.TemplateTools;
import org.mvel2.util.StringAppender;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * This is the root of the template runtime, and contains various utility methods for executing templates.
 */
public class TemplateRuntime {
    private char[] template;
    private TemplateRegistry namedTemplateRegistry;
    private Node rootNode;

    public TemplateRuntime(char[] template, TemplateRegistry namedTemplateRegistry, Node rootNode) {
        this.template = template;
        this.namedTemplateRegistry = namedTemplateRegistry;
        this.rootNode = rootNode;
    }

    public static Object eval(File file, Object ctx, VariableResolverFactory vars, TemplateRegistry registry) {
        return execute(compileTemplate(TemplateTools.readInFile(file)), ctx, vars, registry);
    }

    public static Object eval(InputStream instream, Object ctx, VariableResolverFactory vars, TemplateRegistry registry) {
        return execute(compileTemplate(TemplateTools.readStream(instream)), ctx, vars, registry);
    }

    public static Object eval(String template, Map vars) {
        return execute(compileTemplate(template), null, new MapVariableResolverFactory(vars));
    }

    public static Object eval(String template, Object ctx) {
        return execute(compileTemplate(template), ctx);
    }

    public static Object eval(String template, Object ctx, Map vars) {
        return execute(compileTemplate(template), ctx, new MapVariableResolverFactory(vars));
    }

    public static Object eval(String template, Object ctx, VariableResolverFactory vars) {
        return execute(compileTemplate(template), ctx, vars);
    }

    public static Object eval(String template, Map vars, TemplateRegistry registry) {
        return execute(compileTemplate(template), null, new MapVariableResolverFactory(vars), registry);
    }

    public static Object eval(String template, Object ctx, Map vars, TemplateRegistry registry) {
        return execute(compileTemplate(template), ctx, new MapVariableResolverFactory(vars), registry);
    }

    public static Object eval(String template, Object ctx, VariableResolverFactory vars, TemplateRegistry registry) {
        return execute(compileTemplate(template), ctx, vars, registry);
    }


    public static Object execute(CompiledTemplate compiled) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), null, null, null);
    }

    public static Object execute(CompiledTemplate compiled, Object context) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context, null, null);
    }

    public static Object execute(CompiledTemplate compiled, Map vars) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), null, new MapVariableResolverFactory(vars), null);
    }

    public static Object execute(CompiledTemplate compiled, Object context, Map vars) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context, new MapVariableResolverFactory(vars), null);
    }

    public static Object execute(CompiledTemplate compiled, Object context, TemplateRegistry registry) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context, null, registry);
    }

    public static Object execute(CompiledTemplate compiled, Object context, Map vars, TemplateRegistry registry) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context, new MapVariableResolverFactory(vars), registry);
    }

    public static Object execute(CompiledTemplate compiled, Object context, VariableResolverFactory factory) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context, factory, null);
    }

    public static Object execute(CompiledTemplate compiled, Object context, VariableResolverFactory factory, TemplateRegistry registry) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context, factory, registry);
    }


    public static Object execute(Node root, char[] template,
                                 StringAppender appender, Object context,
                                 VariableResolverFactory factory, TemplateRegistry registry) {
        return new TemplateRuntime(template, registry, root).execute(appender, context, factory);
    }

    public Object execute(StringAppender appender, Object context, VariableResolverFactory factory) {
        return rootNode.eval(this, appender, context, factory);
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public char[] getTemplate() {
        return template;
    }

    public void setTemplate(char[] template) {
        this.template = template;
    }

    public TemplateRegistry getNamedTemplateRegistry() {
        return namedTemplateRegistry;
    }

    public void setNamedTemplateRegistry(TemplateRegistry namedTemplateRegistry) {
        this.namedTemplateRegistry = namedTemplateRegistry;
    }
}
