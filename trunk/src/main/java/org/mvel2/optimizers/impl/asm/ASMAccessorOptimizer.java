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
package org.mvel2.optimizers.impl.asm;

import org.mvel2.*;
import static org.mvel2.DataConversion.canConvert;
import static org.mvel2.DataConversion.convert;
import static org.mvel2.MVEL.eval;
import static org.mvel2.MVEL.isAdvancedDebugging;
import org.mvel2.asm.ClassWriter;
import org.mvel2.asm.Label;
import org.mvel2.asm.MethodVisitor;
import org.mvel2.asm.Opcodes;
import static org.mvel2.asm.Opcodes.*;
import static org.mvel2.asm.Type.*;
import org.mvel2.ast.Function;
import org.mvel2.ast.TypeDescriptor;
import static org.mvel2.ast.TypeDescriptor.getClassReference;
import org.mvel2.compiler.Accessor;
import org.mvel2.compiler.AccessorNode;
import org.mvel2.compiler.ExecutableLiteral;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.PropertyHandler;
import static org.mvel2.integration.PropertyHandlerFactory.getPropertyHandler;
import static org.mvel2.integration.PropertyHandlerFactory.hasPropertyHandler;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.optimizers.AbstractOptimizer;
import org.mvel2.optimizers.AccessorOptimizer;
import org.mvel2.optimizers.OptimizationNotSupported;
import org.mvel2.optimizers.impl.refl.Union;
import static org.mvel2.util.ArrayTools.findFirst;
import org.mvel2.util.*;
import static org.mvel2.util.ParseTools.*;
import static org.mvel2.util.PropertyTools.getFieldOrAccessor;
import static org.mvel2.util.PropertyTools.getFieldOrWriteAccessor;

import java.io.FileWriter;
import java.io.IOException;
import static java.lang.String.valueOf;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import java.lang.reflect.*;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.STATIC;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the MVEL Just-in-Time (JIT) compiler for Property Accessors using the ASM bytecode
 * engineering library.
 * <p/>
 */
@SuppressWarnings({"TypeParameterExplicitlyExtendsObject", "unchecked", "UnusedDeclaration"})
public class ASMAccessorOptimizer extends AbstractOptimizer implements AccessorOptimizer {
    private static final String MAP_IMPL = "java/util/HashMap";

    private static String LIST_IMPL;
    private static String NAMESPACE;
    private static final int OPCODES_VERSION;

    static {
        final String javaVersion = getProperty("java.version");
        if (javaVersion.startsWith("1.4"))
            OPCODES_VERSION = Opcodes.V1_4;
        else if (javaVersion.startsWith("1.5"))
            OPCODES_VERSION = Opcodes.V1_5;
        else if (javaVersion.startsWith("1.6") || javaVersion.startsWith("1.7"))
            OPCODES_VERSION = Opcodes.V1_6;
        else
            OPCODES_VERSION = Opcodes.V1_2;


        String defaultNameSapce = getProperty("mvel2.namespace");
        if (defaultNameSapce == null) NAMESPACE = "org/mvel2/";
        else NAMESPACE = defaultNameSapce;

        String jitListImpl = getProperty("mvel2.jit.list_impl");
        if (jitListImpl == null) LIST_IMPL = NAMESPACE + "util/FastList";
        else LIST_IMPL = jitListImpl;
    }

    private Object ctx;
    private Object thisRef;

    private VariableResolverFactory variableFactory;

    private static final Object[] EMPTYARG = new Object[0];
    private static final Class[] EMPTYCLS = new Class[0];

    private boolean first = true;
    private boolean noinit = false;
    private boolean deferFinish = false;
    private boolean literal = false;

    private String className;
    private ClassWriter cw;
    private MethodVisitor mv;

    private Object val;
    private int stacksize = 1;
    private int maxlocals = 1;
    private long time;

    private ArrayList<ExecutableStatement> compiledInputs;

    private Class returnType;

    @SuppressWarnings({"StringBufferField"})
    private StringAppender buildLog;

    public ASMAccessorOptimizer() {
        //do this to confirm we're running the correct version
        //otherwise should create a verification error in VM
        new ClassWriter(ClassWriter.COMPUTE_MAXS);
    }

    /**
     * Does all the boilerplate for initiating the JIT.
     */
    private void _initJIT() {
        if (isAdvancedDebugging()) {
            buildLog = new StringAppender();
        }

        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);

        synchronized (Runtime.getRuntime()) {
            cw.visit(OPCODES_VERSION, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className = "ASMAccessorImpl_"
                    + valueOf(cw.hashCode()).replaceAll("\\-", "_") + (System.currentTimeMillis() / 10) +
                    ((int) Math.random() * 100),
                    null, "java/lang/Object", new String[]{NAMESPACE + "compiler/Accessor"});
        }

        MethodVisitor m = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V");
        m.visitInsn(RETURN);

        m.visitMaxs(1, 1);
        m.visitEnd();

        (mv = cw.visitMethod(ACC_PUBLIC, "getValue",
                "(Ljava/lang/Object;Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;)Ljava/lang/Object;", null, null)).visitCode();
    }


    private void _initJIT2() {
        if (isAdvancedDebugging()) {
            buildLog = new StringAppender();
        }

        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);

        synchronized (Runtime.getRuntime()) {
            cw.visit(OPCODES_VERSION, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className = "ASMAccessorImpl_"
                    + valueOf(cw.hashCode()).replaceAll("\\-", "_") + (System.currentTimeMillis() / 10) +
                    ((int) Math.random() * 100),
                    null, "java/lang/Object", new String[]{NAMESPACE + "compiler/Accessor"});
        }

        MethodVisitor m = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V");
        m.visitInsn(RETURN);

        m.visitMaxs(1, 1);
        m.visitEnd();

        (mv = cw.visitMethod(ACC_PUBLIC, "setValue",
                "(Ljava/lang/Object;Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;Ljava/lang/Object;)Ljava/lang/Object;", null, null)).visitCode();

    }

    public Accessor optimizeAccessor(ParserContext pCtx, char[] property, Object staticContext, Object thisRef, VariableResolverFactory factory, boolean root) {
        time = System.currentTimeMillis();

        compiledInputs = new ArrayList<ExecutableStatement>();

        start = cursor = 0;

        this.first = true;
        this.val = null;

        this.length = property.length;
        this.expr = property;
        this.ctx = staticContext;
        this.thisRef = thisRef;
        this.variableFactory = factory;

        if (!noinit) _initJIT();

        //     this.pCtx = pCtx;
        return compileAccessor();
    }

    public Accessor optimizeSetAccessor(ParserContext pCtx, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory, boolean rootThisRef, Object value) {
        this.start = this.cursor = 0;
        this.first = true;

        compiledInputs = new ArrayList<ExecutableStatement>();

        this.length = (this.expr = property).length;
        this.ctx = ctx;
        this.thisRef = thisRef;
        this.variableFactory = factory;

        char[] root = null;

        int split = findLastUnion();

        if (split != -1) {
            root = subset(property, 0, split++);
            property = subset(property, split, property.length - split);
        }

        AccessorNode rootAccessor = null;

        _initJIT2();

        if (root != null) {
            this.length = (this.expr = root).length;

            // run the compiler but don't finish building.
            deferFinish = true;
            noinit = true;

            compileAccessor();
            ctx = this.val;
        }
        else {
            assert debug("ALOAD 1");
            mv.visitVarInsn(ALOAD, 1);
        }

        try {
            this.length = (this.expr = property).length;
            this.cursor = this.start = 0;

            whiteSpaceSkip();

            if (collection) {
                int start = cursor;
                whiteSpaceSkip();

                if (cursor == length)
                    throw new PropertyAccessException("unterminated '['");

                if (scanTo(']'))
                    throw new PropertyAccessException("unterminated '['");

                String ex = new String(property, start, cursor - start);

                assert debug("CHECKCAST " + ctx.getClass().getName());
                mv.visitTypeInsn(CHECKCAST, getInternalName(ctx.getClass()));

                if (ctx instanceof Map) {
                    //noinspection unchecked
                    ((Map) ctx).put(eval(ex, ctx, variableFactory), value);

                    writeLiteralOrSubexpression(subCompileExpression(ex.toCharArray()));

                    assert debug("ALOAD 4");
                    mv.visitVarInsn(ALOAD, 4);

                    assert debug("DUP_X1");
                    mv.visitInsn(DUP_X1);

                    assert debug("INVOKEINTERFACE Map.put");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

                    assert debug("POP");
                    mv.visitInsn(POP);
                }
                else if (ctx instanceof List) {
                    //noinspection unchecked
                    ((List) ctx).set(eval(ex, ctx, variableFactory, Integer.class), value);

                    writeLiteralOrSubexpression(subCompileExpression(ex.toCharArray()));
                    unwrapPrimitive(int.class);

                    assert debug("ALOAD 4");
                    mv.visitVarInsn(ALOAD, 4);

                    assert debug("DUP_X1");
                    mv.visitInsn(DUP_X1);

                    assert debug("INVOKEINTERFACE List.set");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;");

                    assert debug("POP");
                    mv.visitInsn(POP);

                }
                else if (ctx.getClass().isArray()) {
                    Class type = getBaseComponentType(ctx.getClass());

                    Object idx = eval(ex, ctx, variableFactory);

                    writeLiteralOrSubexpression(subCompileExpression(ex.toCharArray()));
                    if (!(idx instanceof Integer)) {
                        dataConversion(Integer.class);
                        idx = DataConversion.convert(idx, Integer.class);
                    }

                    unwrapPrimitive(int.class);

                    assert debug("ALOAD 4");
                    mv.visitVarInsn(ALOAD, 4);

                    assert debug("CHECKCAST " + getInternalName(type));
                    mv.visitTypeInsn(CHECKCAST, getInternalName(type));

                    assert debug("DUP_X2");
                    mv.visitInsn(DUP_X2);

                    if (!type.equals(value.getClass())) dataConversion(type);
                    if (type.isPrimitive()) wrapPrimitive(type);

                    arrayStore(type);

                    //noinspection unchecked
                    Array.set(ctx, (Integer) idx, convert(value, getBaseComponentType(ctx.getClass())));
                }
                else {
                    throw new PropertyAccessException("cannot bind to collection property: " + new String(property) + ": not a recognized collection type: " + ctx.getClass());
                }

                deferFinish = false;
                noinit = false;

                _finishJIT();

                try {
                    deferFinish = false;
                    return _initializeAccessor();
                }
                catch (Exception e) {
                    throw new CompileException("could not generate accessor", e);
                }
            }

            String tk = new String(property);
            Member member = getFieldOrWriteAccessor(ctx.getClass(), tk);

            if (member instanceof Field) {
                assert debug("CHECKCAST " + ctx.getClass().getName());
                mv.visitTypeInsn(CHECKCAST, getInternalName(ctx.getClass()));


                Field fld = (Field) member;

                assert debug("ALOAD 4");
                mv.visitVarInsn(ALOAD, 4);

                assert debug("CHECKCAST " + fld.getType().getName());
                mv.visitTypeInsn(CHECKCAST, getInternalName(fld.getType()));

                assert debug("DUP_X1");
                mv.visitInsn(DUP_X1);

                if (value != null && !fld.getType().isAssignableFrom(value.getClass())) {
                    if (!canConvert(fld.getType(), value.getClass())) {
                        throw new ConversionException("cannot convert type: "
                                + value.getClass() + ": to " + fld.getType());
                    }

                    dataConversion(fld.getType());
                    fld.set(ctx, convert(value, fld.getType()));
                }
                else {
                    fld.set(ctx, value);
                }

                assert debug("PUTFIELD " + getInternalName(fld.getDeclaringClass()) + "." + tk);
                mv.visitFieldInsn(PUTFIELD, getInternalName(fld.getDeclaringClass()), tk, getDescriptor(fld.getType()));

            }
            else if (member != null) {
                assert debug("CHECKCAST " + getInternalName(ctx.getClass()));
                mv.visitTypeInsn(CHECKCAST, getInternalName(ctx.getClass()));

                Method meth = (Method) member;

                assert debug("ALOAD 4");
                mv.visitVarInsn(ALOAD, 4);

                Class targetType = meth.getParameterTypes()[0];

                assert debug("DUP_X1");
                mv.visitInsn(DUP_X1);

                if (value != null && !targetType.isAssignableFrom(value.getClass())) {
                    if (!canConvert(targetType, value.getClass())) {
                        throw new ConversionException("cannot convert type: "
                                + value.getClass() + ": to " + meth.getParameterTypes()[0]);
                    }

                    dataConversion(getWrapperClass(targetType));
                    if (targetType.isPrimitive()) unwrapPrimitive(targetType);

                    meth.invoke(ctx, convert(value, meth.getParameterTypes()[0]));
                }
                else {
                    assert debug("CHECKCAST " + getInternalName(targetType));
                    mv.visitTypeInsn(CHECKCAST, getInternalName(targetType));

                    meth.invoke(ctx, value);
                }

                assert debug("INVOKEVIRTUAL " + getInternalName(meth.getDeclaringClass()) + "." + meth.getName());
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(meth.getDeclaringClass()), meth.getName(),
                        getMethodDescriptor(meth));

            }
            else if (ctx instanceof Map) {
                assert debug("CHECKCAST " + getInternalName(ctx.getClass()));
                mv.visitTypeInsn(CHECKCAST, getInternalName(ctx.getClass()));

                assert debug("LDC '" + tk + "'");
                mv.visitLdcInsn(tk);

                assert debug("ALOAD 4");
                mv.visitVarInsn(ALOAD, 4);

                assert debug("DUP_X2");
                mv.visitInsn(DUP_X2);

                assert debug("INVOKEVIRTUAL java/util/HashMap.put");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

                assert debug("POP");
                mv.visitInsn(POP);

                //noinspection unchecked
                ((Map) ctx).put(tk, value);
            }
            else {
                throw new PropertyAccessException("could not access property (" + tk + ") in: " + ctx.getClass().getName());
            }
        }
        catch (InvocationTargetException e) {
            throw new PropertyAccessException("could not access property", e);
        }
        catch (IllegalAccessException e) {
            throw new PropertyAccessException("could not access property", e);
        }

        try {
            deferFinish = false;
            noinit = false;

            _finishJIT();
            return _initializeAccessor();
        }
        catch (Exception e) {

            throw new CompileException("could not generate accessor", e);
        }
    }

    private void _finishJIT() {
        if (deferFinish) return;

        if (returnType != null && returnType.isPrimitive()) {
            //noinspection unchecked
            wrapPrimitive(returnType);
        }

        if (returnType == void.class) {
            assert debug("ACONST_NULL");
            mv.visitInsn(ACONST_NULL);
        }

        assert debug("ARETURN");
        mv.visitInsn(ARETURN);

        assert debug("\n{METHOD STATS (maxstack=" + stacksize + ")}\n");

        mv.visitMaxs(stacksize, maxlocals);
        mv.visitEnd();

        buildInputs();

        cw.visitEnd();

        dumpAdvancedDebugging(); // dump advanced debugging if necessary
    }

    private Accessor _initializeAccessor() throws Exception {
        if (deferFinish) return null;

        /**
         * Hot load the class we just generated.
         */
        Class cls = loadClass(className, cw.toByteArray());

        assert debug("[MVEL JIT Completed Optimization <<" + (expr != null ? new String(expr) : "") + ">>]::" + cls + " (time: " + (System.currentTimeMillis() - time) + "ms)");

        Object o;

        try {
            if (compiledInputs.size() == 0) {
                o = cls.newInstance();
            }
            else {
                Class[] parms = new Class[compiledInputs.size()];
                for (int i = 0; i < compiledInputs.size(); i++) {
                    parms[i] = ExecutableStatement.class;
                }
                o = cls.getConstructor(parms).newInstance(compiledInputs.toArray(new ExecutableStatement[compiledInputs.size()]));
            }
        }
        catch (VerifyError e) {
            System.out.println("**** COMPILER BUG! REPORT THIS IMMEDIATELY AT http://jira.codehaus.org/browse/mvel2");
            System.out.println("Expression: " + (expr == null ? null : new String(expr)));
            throw e;
        }

        return (Accessor) o;
    }

    private Accessor compileAccessor() {
        assert debug("<<INITIATE COMPILE>>");

        Object curr = ctx;

        try {
            while (cursor < length) {
                switch (nextSubToken()) {
                    case BEAN:
                        curr = getBeanProperty(curr, capture());
                        break;
                    case METH:
                        curr = getMethod(curr, capture());
                        break;
                    case COL:
                        curr = getCollectionProperty(curr, capture());
                        break;
                    case WITH:
                        curr = getWithProperty(curr);
                        break;
                }

                // check to see if a null safety is enabled on this property.
                if (fields == -1) {
                    if (curr == null) {
                        break;
                    }
                    else {
                        fields = 0;
                    }
                }

                first = false;
            }

            val = curr;

            _finishJIT();

            return _initializeAccessor();
        }
        catch (InvocationTargetException e) {
            throw new PropertyAccessException(new String(expr), e);
        }
        catch (IllegalAccessException e) {
            throw new PropertyAccessException(new String(expr), e);
        }
        catch (IndexOutOfBoundsException e) {
            throw new PropertyAccessException(new String(expr), e);
        }
        catch (PropertyAccessException e) {
            throw new CompileException(e.getMessage(), e);
        }
        catch (CompileException e) {
            throw e;
        }
        catch (NullPointerException e) {
            throw new PropertyAccessException(new String(expr), e);
        }
        catch (OptimizationNotSupported e) {
            throw e;
        }
        catch (Exception e) {
            throw new CompileException(e.getMessage(), e);
        }
    }

    private Object getWithProperty(Object ctx) {
        assert debug("\n  ** ENTER -> {with}");

        if (first) {
            assert debug("ALOAD 1");
            mv.visitVarInsn(ALOAD, 1);
            first = false;
        }

        String root = new String(expr, 0, cursor - 1).trim();

        int start = cursor + 1;
        int[] res = balancedCaptureWithLineAccounting(expr, cursor, '{');
        cursor = res[0];
        (pCtx = getParserContext()).incrementLineCount(res[1]);

        WithStatementPair[] pvp = parseWithExpressions(root, subset(expr, start, cursor++ - start));

        for (WithStatementPair aPvp : pvp) {
            assert debug("DUP");
            mv.visitInsn(DUP);
            if (aPvp.getParm() == null) {
                // Execute this interpretively now.
                MVEL.eval(aPvp.getValue(), ctx, variableFactory);

                addSubstatement((ExecutableStatement) subCompileExpression(aPvp.getValue().toCharArray()));
            }
            else {
                // Execute interpretively.
                MVEL.setProperty(ctx, aPvp.getParm(), MVEL.eval(aPvp.getValue(), ctx, variableFactory));

                compiledInputs.add(((ExecutableStatement) MVEL.compileSetExpression(aPvp.getParm(), pCtx)));

                assert debug("ALOAD 0");
                mv.visitVarInsn(ALOAD, 0);

                assert debug("GETFIELD p" + (compiledInputs.size() - 1));
                mv.visitFieldInsn(GETFIELD, className, "p" + (compiledInputs.size() - 1), "L" + NAMESPACE + "compiler/ExecutableStatement;");

                assert debug("ALOAD 1");
                mv.visitVarInsn(ALOAD, 1);

                assert debug("ALOAD 2");
                mv.visitVarInsn(ALOAD, 2);

                assert debug("ALOAD 3");
                mv.visitVarInsn(ALOAD, 3);

                addSubstatement((ExecutableStatement) subCompileExpression(aPvp.getValue().toCharArray()));


                assert debug("INVOKEINTERFACE Accessor.setValue");
                mv.visitMethodInsn(INVOKEINTERFACE, "org/mvel2/compiler/ExecutableStatement",
                        "setValue",
                        "(Ljava/lang/Object;Ljava/lang/Object;L"
                                + NAMESPACE + "integration/VariableResolverFactory;Ljava/lang/Object;)Ljava/lang/Object;");

                assert debug("POP");
                mv.visitInsn(POP);
            }
        }

        return ctx;

    }

    private Object getBeanProperty(Object ctx, String property)
            throws IllegalAccessException, InvocationTargetException {

        assert debug("\n  **  ENTER -> {bean: " + property + "; ctx=" + ctx + "}");

        if (returnType != null && returnType.isPrimitive()) {
            //noinspection unchecked
            wrapPrimitive(returnType);
        }

        Class cls = (ctx instanceof Class ? ((Class) ctx) : ctx != null ? ctx.getClass() : null);

        if (hasPropertyHandler(cls)) {
            PropertyHandler prop = getPropertyHandler(cls);
            if (prop instanceof ProducesBytecode) {
                ((ProducesBytecode) prop).produceBytecodeGet(mv, property, variableFactory);
                return prop.getProperty(property, ctx, variableFactory);
            }
            else {
                throw new RuntimeException("unable to compile: custom accessor does not support producing bytecode: "
                        + prop.getClass().getName());
            }
        }

        Member member = cls != null ? getFieldOrAccessor(cls, property) : null;

        if (first) {
            if ("this".equals(property)) {
                assert debug("ALOAD 2");
                mv.visitVarInsn(ALOAD, 2);
                return thisRef;
            }
            else if (variableFactory != null && variableFactory.isResolveable(property)) {
                if (variableFactory.isIndexedFactory() && variableFactory.isTarget(property)) {
                    int idx;
                    try {
                        loadVariableByIndex(idx = variableFactory.variableIndexOf(property));
                    }
                    catch (Exception e) {
                        throw new OptimizationFailure(property);
                    }

                    return variableFactory.getIndexedVariableResolver(idx).getValue();
                }
                else {
                    try {
                        loadVariableByName(property);
                    }
                    catch (Exception e) {
                        throw new OptimizationFailure("critical error in JIT", e);
                    }

                    return variableFactory.getVariableResolver(property).getValue();
                }
            }
            else {
                assert debug("ALOAD 1");
                mv.visitVarInsn(ALOAD, 1);
            }
        }

        if (member instanceof Field) {
            Object o = ((Field) member).get(ctx);

            if (first) {
                assert debug("ALOAD 1 (A)");
                mv.visitVarInsn(ALOAD, 1);
            }

            if (((member.getModifiers() & (STATIC | FINAL)) != 0)) {
                // Check if the static field reference is a constant and a primitive.
                if ((member.getModifiers() & FINAL) != 0 && (o instanceof String || o.getClass().isPrimitive())) {
                    o = ((Field) member).get(null);
                    assert debug("LDC " + valueOf(o));
                    mv.visitLdcInsn(o);
                    wrapPrimitive(o.getClass());
                    return o;
                }
                else {
                    assert debug("GETSTATIC " + getDescriptor(member.getDeclaringClass()) + "."
                            + member.getName() + "::" + getDescriptor(((Field) member).getType()));

                    mv.visitFieldInsn(GETSTATIC, getInternalName(member.getDeclaringClass()),
                            member.getName(), getDescriptor(returnType = ((Field) member).getType()));
                }
            }
            else {
                assert debug("CHECKCAST " + getInternalName(cls));
                mv.visitTypeInsn(CHECKCAST, getInternalName(cls));

                assert debug("GETFIELD " + property + ":" + getDescriptor(((Field) member).getType()));
                mv.visitFieldInsn(GETFIELD, getInternalName(cls), property, getDescriptor(returnType = ((Field) member).getType()));
            }

            returnType = ((Field) member).getType();

            return o;
        }
        else if (member != null) {
            Object o;

            if (first) {
                assert debug("ALOAD 1 (B)");
                mv.visitVarInsn(ALOAD, 1);
            }

            try {
                o = ((Method) member).invoke(ctx, EMPTYARG);

                if (returnType != member.getDeclaringClass()) {
                    assert debug("CHECKCAST " + getInternalName(member.getDeclaringClass()));
                    mv.visitTypeInsn(CHECKCAST, getInternalName(member.getDeclaringClass()));
                }

                returnType = ((Method) member).getReturnType();

                assert debug("INVOKEVIRTUAL " + member.getName() + ":" + returnType);
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(member.getDeclaringClass()), member.getName(),
                        getMethodDescriptor((Method) member));
            }
            catch (IllegalAccessException e) {
                Method iFaceMeth = determineActualTargetMethod((Method) member);
                if (iFaceMeth == null)
                    throw new PropertyAccessException("could not access field: " + cls.getName() + "." + property);

                assert debug("CHECKCAST " + getInternalName(iFaceMeth.getDeclaringClass()));
                mv.visitTypeInsn(CHECKCAST, getInternalName(iFaceMeth.getDeclaringClass()));

                returnType = iFaceMeth.getReturnType();

                assert debug("INVOKEINTERFACE " + member.getName() + ":" + returnType);
                mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(iFaceMeth.getDeclaringClass()), member.getName(),
                        getMethodDescriptor((Method) member));

                o = iFaceMeth.invoke(ctx, EMPTYARG);
            }
            return o;

        }
        else if (ctx instanceof Map && ((Map) ctx).containsKey(property)) {
            assert debug("CHECKCAST java/util/Map");
            mv.visitTypeInsn(CHECKCAST, "java/util/Map");

            assert debug("LDC: \"" + property + "\"");
            mv.visitLdcInsn(property);

            assert debug("INVOKEINTERFACE: get");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            return ((Map) ctx).get(property);
        }
        else if (first && "this".equals(property)) {
            assert debug("ALOAD 2");
            mv.visitVarInsn(ALOAD, 2); // load the thisRef value.

            return this.thisRef;
        }
        else if ("length".equals(property) && ctx.getClass().isArray()) {
            anyArrayCheck(ctx.getClass());

            assert debug("ARRAYLENGTH");
            mv.visitInsn(ARRAYLENGTH);

            wrapPrimitive(int.class);
            return getLength(ctx);
        }
        else if (LITERALS.containsKey(property)) {
            Object lit = LITERALS.get(property);

            if (lit instanceof Class) {
                ldcClassConstant((Class) lit);
            }

            return lit;
        }
        else if (ctx == null) {
            throw new NullPointerException("parent field of '" + property + "' is null in: " + new String(expr));
        }
        else {
            Object ts = tryStaticAccess();

            if (ts != null) {
                if (ts instanceof Class) {
                    ldcClassConstant((Class) ts);
                    return ts;
                }
                else if (ts instanceof Method) {
                    writeFunctionPointerStub(((Method) ts).getDeclaringClass(), (Method) ts);
                    return ts;
                }
                else {
                    Field f = (Field) ts;

                    if ((f.getModifiers() & FINAL) != 0) {
                        Object finalVal = f.get(null);
                        assert debug("LDC " + valueOf(finalVal));
                        mv.visitLdcInsn(finalVal);
                        wrapPrimitive(finalVal.getClass());
                        return finalVal;
                    }
                    else {
                        assert debug("GETSTATIC " + getInternalName(f.getDeclaringClass()) + "."
                                + ((Field) ts).getName() + "::" + getDescriptor(f.getType()));

                        mv.visitFieldInsn(GETSTATIC, getInternalName(f.getDeclaringClass()),
                                f.getName(), getDescriptor(returnType = f.getType()));


                        return f.get(null);
                    }
                }
            }
            else if (ctx instanceof Class) {
                /**
                 * This is our ugly support for function pointers.  This works but needs to be re-thought out at some
                 * point.
                 */
                Class c = (Class) ctx;
                for (Method m : c.getMethods()) {
                    if (property.equals(m.getName())) {
                        if (MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL) {
                            assert debug("POP");
                            mv.visitInsn(POP);
                            assert debug("INVOKESTATIC " + m.getName());
                            mv.visitMethodInsn(INVOKESTATIC, getInternalName(m.getDeclaringClass()), m.getName(), getMethodDescriptor(m));

                            returnType = m.getReturnType();

                            return m.invoke(null, EMPTY_OBJ_ARR);
                        }
                        else {
                            writeFunctionPointerStub(c, m);
                            return m;
                        }
                    }
                }
            }
            else if (MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL) {
                return getMethod(ctx, property);
            }

            throw new PropertyAccessException(property);
        }
    }


    private void writeFunctionPointerStub(Class c, Method m) {
        ldcClassConstant(c);

        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethods", "()[Ljava/lang/reflect/Method;");
        mv.visitVarInsn(ASTORE, 7);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 5);
        mv.visitVarInsn(ALOAD, 7);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, 6);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 7);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, 4);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLdcInsn(m.getName());
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        Label l4 = new Label();
        mv.visitJumpInsn(IFEQ, l4);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l4);
        mv.visitIincInsn(5, 1);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitVarInsn(ILOAD, 6);
        mv.visitJumpInsn(IF_ICMPLT, l2);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);

        deferFinish = true;
    }


    private Object getCollectionProperty(Object ctx, String prop)
            throws IllegalAccessException, InvocationTargetException {
        if (prop.length() > 0) ctx = getBeanProperty(ctx, prop);

        assert debug("\n  **  ENTER -> {collections: " + prop + "; ctx=" + ctx + "}");

        int start = ++cursor;

        whiteSpaceSkip();

        if (cursor == length)
            throw new CompileException("unterminated '['");

        if (scanTo(']'))
            throw new CompileException("unterminated '['");

        String tk = new String(expr, start, cursor - start);

        assert debug("{collection token:<<" + tk + ">>}");

        ExecutableStatement compiled = (ExecutableStatement) subCompileExpression(tk.toCharArray());
        Object item = compiled.getValue(ctx, variableFactory);

        ++cursor;

        if (ctx instanceof Map) {
            assert debug("CHECKCAST java/util/Map");
            mv.visitTypeInsn(CHECKCAST, "java/util/Map");

            Class c = writeLiteralOrSubexpression(compiled);
            if (c != null && c.isPrimitive()) {
                wrapPrimitive(c);
            }

            assert debug("INVOKEINTERFACE: get");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");

            return ((Map) ctx).get(item);
        }
        else if (ctx instanceof List) {
            assert debug("CHECKCAST java/util/List");
            mv.visitTypeInsn(CHECKCAST, "java/util/List");

            writeLiteralOrSubexpression(compiled, int.class);

            assert debug("INVOKEINTERFACE: java/util/List.get");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;");

            return ((List) ctx).get(convert(item, Integer.class));

        }
        else if (ctx.getClass().isArray()) {
            assert debug("CHECKCAST " + getDescriptor(ctx.getClass()));
            mv.visitTypeInsn(CHECKCAST, getDescriptor(ctx.getClass()));

            writeLiteralOrSubexpression(compiled, int.class, item.getClass());

            Class cls = getBaseComponentType(ctx.getClass());
            if (cls.isPrimitive()) {
                if (cls == int.class) {
                    assert debug("IALOAD");
                    mv.visitInsn(IALOAD);
                }
                else if (cls == char.class) {
                    assert debug("CALOAD");
                    mv.visitInsn(CALOAD);
                }
                else if (cls == boolean.class) {
                    assert debug("BALOAD");
                    mv.visitInsn(BALOAD);
                }
                else if (cls == double.class) {
                    assert debug("DALOAD");
                    mv.visitInsn(DALOAD);
                }
                else if (cls == float.class) {
                    assert debug("FALOAD");
                    mv.visitInsn(FALOAD);
                }
                else if (cls == short.class) {
                    assert debug("SALOAD");
                    mv.visitInsn(SALOAD);
                }
                else if (cls == long.class) {
                    assert debug("LALOAD");
                    mv.visitInsn(LALOAD);
                }
                else if (cls == byte.class) {
                    assert debug("BALOAD");
                    mv.visitInsn(BALOAD);
                }

                wrapPrimitive(cls);
            }
            else {
                assert debug("AALOAD");
                mv.visitInsn(AALOAD);
            }

            return Array.get(ctx, convert(item, Integer.class));
        }
        else if (ctx instanceof CharSequence) {
            assert debug("CHECKCAST java/lang/CharSequence");
            mv.visitTypeInsn(CHECKCAST, "java/lang/CharSequence");

            if (item instanceof Integer) {
                intPush((Integer) item);

                assert debug("INVOKEINTERFACE java/lang/CharSequence.charAt");
                mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/CharSequence", "charAt", "(I)C");

                wrapPrimitive(char.class);

                return ((CharSequence) ctx).charAt((Integer) item);
            }
            else {
                writeLiteralOrSubexpression(compiled, Integer.class);
                unwrapPrimitive(int.class);

                assert debug("INVOKEINTERFACE java/lang/CharSequence.charAt");
                mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/CharSequence", "charAt", "(I)C");

                wrapPrimitive(char.class);

                return ((CharSequence) ctx).charAt(convert(item, Integer.class));
            }
        }
        else {
            TypeDescriptor tDescr = new TypeDescriptor(expr, 0);
            if (tDescr.isArray()) {
                try {
                    Class cls = getClassReference((Class) ctx, tDescr, variableFactory);
                    //   rootNode = new StaticReferenceAccessor(cls);
                    ldcClassConstant(cls);
                    return cls;
                }
                catch (Exception e) {
                    //fall through
                }
            }

            throw new CompileException("illegal use of []: unknown type: " + (ctx == null ? null : ctx.getClass().getName()));
        }
    }

    @SuppressWarnings({"unchecked"})
    private Object getMethod(Object ctx, String name)
            throws IllegalAccessException, InvocationTargetException {
        assert debug("\n  **  {method: " + name + "}");

        int st = cursor;
        String tk = cursor != length && ((cursor = balancedCapture(expr, cursor, '(')) - st) > 1 ?
                new String(expr, st + 1, cursor - st - 1) : "";
        cursor++;

        Object[] preConvArgs;
        Object[] args;
        ExecutableStatement[] es;

        if (tk.length() == 0) {
            args = preConvArgs = EMPTYARG;
            es = null;
        }
        else {
            String[] subtokens = parseParameterList(tk.toCharArray(), 0, -1);

            es = new ExecutableStatement[subtokens.length];
            args = new Object[subtokens.length];
            preConvArgs = new Object[es.length];

            for (int i = 0; i < subtokens.length; i++) {
                assert debug("subtoken[" + i + "] { " + subtokens[i] + " }");
                preConvArgs[i] = args[i] = (es[i] = (ExecutableStatement) subCompileExpression(subtokens[i].toCharArray())).getValue(this.ctx, this.thisRef, variableFactory);
            }
        }

        if (first && variableFactory != null && variableFactory.isResolveable(name)) {
            Object ptr = variableFactory.getVariableResolver(name).getValue();

            if (ptr instanceof Method) {
                ctx = ((Method) ptr).getDeclaringClass();
                name = ((Method) ptr).getName();
            }
            else if (ptr instanceof MethodStub) {
                ctx = ((MethodStub) ptr).getClassReference();
                name = ((MethodStub) ptr).getMethodName();
            }
            else if (ptr instanceof Function) {

                if (es != null && es.length != 0) {
                    compiledInputs.addAll(Arrays.asList(es));

                    intPush(es.length);

                    assert debug("ANEWARRAY [" + es.length + "]");
                    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

                    assert debug("ASTORE 4");
                    mv.visitVarInsn(ASTORE, 4);

                    for (int i = 0; i < es.length; i++) {
                        assert debug("ALOAD 4");
                        mv.visitVarInsn(ALOAD, 4);
                        intPush(i);
                        loadField(i);

                        assert debug("ALOAD 1");
                        mv.visitVarInsn(ALOAD, 1);

                        assert debug("ALOAD 3");
                        mv.visitIntInsn(ALOAD, 3);

                        assert debug("INVOKEINTERFACE ExecutableStatement.getValue");
                        mv.visitMethodInsn(INVOKEINTERFACE, NAMESPACE + "compiler/ExecutableStatement", "getValue",
                                "(Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;)Ljava/lang/Object;");

                        assert debug("AASTORE");
                        mv.visitInsn(AASTORE);
                    }
                }
                else {
                    assert debug("ACONST_NULL");
                    mv.visitInsn(ACONST_NULL);

                    assert debug("CHECKCAST java/lang/Object");
                    mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");

                    assert debug("ASTORE 4");
                    mv.visitVarInsn(ASTORE, 4);
                }

                if (variableFactory.isIndexedFactory() && variableFactory.isTarget(name)) {
                    loadVariableByIndex(variableFactory.variableIndexOf(name));
                }
                else {
                    loadVariableByName(name);
                }

                checkcast(Function.class);

                assert debug("ALOAD 1");
                mv.visitVarInsn(ALOAD, 1);

                assert debug("ALOAD 2");
                mv.visitVarInsn(ALOAD, 2);

                assert debug("ALOAD 3");
                mv.visitVarInsn(ALOAD, 3);

                assert debug("ALOAD 4");
                mv.visitVarInsn(ALOAD, 4);

                assert debug("INVOKEVIRTUAL Function.call");
                mv.visitMethodInsn(INVOKEVIRTUAL,
                        getInternalName(Function.class),
                        "call",
                        "(Ljava/lang/Object;Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;[Ljava/lang/Object;)Ljava/lang/Object;");

                Object[] parm = null;

                if (es != null) {
                    parm = new Object[es.length];
                    for (int i = 0; i < es.length; i++) {
                        parm[i] = es[i].getValue(ctx, thisRef, variableFactory);
                    }
                }

                return ((Function) ptr).call(ctx, thisRef, variableFactory, parm);
            }
            else {
                throw new OptimizationFailure("attempt to optimize a method call for a reference that does not point to a method: "
                        + name + " (reference is type: " + (ctx != null ? ctx.getClass().getName() : null) + ")");
            }

            first = false;
        }
        else if (returnType != null && returnType.isPrimitive()) {
            //noinspection unchecked
            wrapPrimitive(returnType);
        }


        int inputsOffset = compiledInputs.size();

        if (es != null) {
            for (ExecutableStatement e : es) {
                if (e instanceof ExecutableLiteral) {
                    continue;
                }

                compiledInputs.add(e);
            }
        }

        if (first) {
            assert debug("ALOAD 1 (D) ");
            mv.visitVarInsn(ALOAD, 1);
        }

        /**
         * If the target object is an instance of java.lang.Class itself then do not
         * adjust the Class scope target.
         */
        Class cls = ctx instanceof Class ? (Class) ctx : ctx.getClass();

        Method m;
        Class[] parameterTypes = null;

        /**
         * Try to find an instance method from the class target.
         */
        if ((m = getBestCandidate(args, name, cls, cls.getMethods(), false)) != null) {
            parameterTypes = m.getParameterTypes();
        }

        if (m == null) {
            /**
             * If we didn't find anything, maybe we're looking for the actual java.lang.Class methods.
             */
            if ((m = getBestCandidate(args, name, cls, cls.getClass().getDeclaredMethods(), false)) != null) {
                parameterTypes = m.getParameterTypes();
            }
        }

        if (m == null) {
            StringAppender errorBuild = new StringAppender();

            if (parameterTypes != null) {
                for (int i = 0; i < args.length; i++) {
                    errorBuild.append(parameterTypes[i] != null ? parameterTypes[i].getClass().getName() : null);
                    if (i < args.length - 1) errorBuild.append(", ");
                }
            }

            if ("size".equals(name) && args.length == 0 && cls.isArray()) {
                anyArrayCheck(cls);

                assert debug("ARRAYLENGTH");
                mv.visitInsn(ARRAYLENGTH);

                wrapPrimitive(int.class);
                return getLength(ctx);
            }

            throw new CompileException("unable to resolve method: " + cls.getName() + "." + name + "(" + errorBuild.toString() + ") [arglength=" + args.length + "]");
        }
        else {
            m = getWidenedTarget(m);

            if (es != null) {
                ExecutableStatement cExpr;
                for (int i = 0; i < es.length; i++) {
                    if ((cExpr = es[i]).getKnownIngressType() == null) {
                        cExpr.setKnownIngressType(parameterTypes[i]);
                        cExpr.computeTypeConversionRule();
                    }
                    if (!cExpr.isConvertableIngressEgress()) {
                        args[i] = convert(args[i], parameterTypes[i]);
                    }
                }
            }
            else {
                /**
                 * Coerce any types if required.
                 */
                for (int i = 0; i < args.length; i++) {
                    args[i] = convert(args[i], parameterTypes[i]);
                }
            }

            if (m.getParameterTypes().length == 0) {
                if ((m.getModifiers() & STATIC) != 0) {
                    assert debug("INVOKESTATIC " + m.getName());
                    mv.visitMethodInsn(INVOKESTATIC, getInternalName(m.getDeclaringClass()), m.getName(), getMethodDescriptor(m));
                }
                else {
                    assert debug("CHECKCAST " + getInternalName(m.getDeclaringClass()));
                    mv.visitTypeInsn(CHECKCAST, getInternalName(m.getDeclaringClass()));

                    if (m.getDeclaringClass().isInterface()) {
                        assert debug("INVOKEINTERFACE " + m.getName());
                        mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(m.getDeclaringClass()), m.getName(),
                                getMethodDescriptor(m));

                    }
                    else {
                        assert debug("INVOKEVIRTUAL " + m.getName());
                        mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(m.getDeclaringClass()), m.getName(),
                                getMethodDescriptor(m));
                    }
                }

                returnType = m.getReturnType();

                stacksize++;
            }
            else {
                if ((m.getModifiers() & STATIC) == 0) {
                    assert debug("CHECKCAST " + getInternalName(cls));
                    mv.visitTypeInsn(CHECKCAST, getInternalName(cls));
                }

                for (int i = 0; i < es.length; i++) {
                    if (es[i] instanceof ExecutableLiteral) {
                        ExecutableLiteral literal = (ExecutableLiteral) es[i];

                        if (literal.getLiteral() == null) {
                            assert debug("ICONST_NULL");
                            mv.visitInsn(ACONST_NULL);
                            continue;
                        }
                        else if (parameterTypes[i] == int.class && literal.intOptimized()) {
                            intPush(literal.getInteger32());
                            continue;
                        }
                        else if (parameterTypes[i] == int.class && preConvArgs[i] instanceof Integer) {
                            intPush((Integer) preConvArgs[i]);
                            continue;
                        }
                        else if (parameterTypes[i] == boolean.class) {
                            boolean bool = DataConversion.convert(literal.getLiteral(), Boolean.class);
                            assert debug(bool ? "ICONST_1" : "ICONST_0");
                            mv.visitInsn(bool ? ICONST_1 : ICONST_0);
                            continue;
                        }
                        else {
                            Object lit = literal.getLiteral();

                            if (parameterTypes[i] == Object.class) {
                                if (isPrimitiveWrapper(lit.getClass())) {
                                    if (lit.getClass() == Integer.class) {
                                        intPush((Integer) lit);
                                    }
                                    else {
                                        assert debug("LDC " + lit);
                                        mv.visitLdcInsn(lit);
                                    }

                                    wrapPrimitive(lit.getClass());
                                }
                                else if (lit instanceof String) {
                                    mv.visitLdcInsn(lit);
                                    checkcast(Object.class);
                                }
                                continue;
                            }
                            else if (canConvert(parameterTypes[i], lit.getClass())) {
                                assert debug("LDC " + lit + " (" + lit.getClass().getName() + ")");
                                mv.visitLdcInsn(convert(lit, parameterTypes[i]));

                                if (isPrimitiveWrapper(parameterTypes[i])) {
                                    wrapPrimitive(lit.getClass());
                                }

                                continue;
                            }
                        }
                    }

                    assert debug("ALOAD 0");
                    mv.visitVarInsn(ALOAD, 0);

                    assert debug("GETFIELD p" + inputsOffset);
                    mv.visitFieldInsn(GETFIELD, className, "p" + inputsOffset, "L" + NAMESPACE + "compiler/ExecutableStatement;");

                    inputsOffset++;

                    assert debug("ALOAD 2");
                    mv.visitVarInsn(ALOAD, 2);

                    assert debug("ALOAD 3");
                    mv.visitVarInsn(ALOAD, 3);

                    assert debug("INVOKEINTERFACE ExecutableStatement.getValue");
                    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(ExecutableStatement.class), "getValue",
                            "(Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;)Ljava/lang/Object;");

                    if (parameterTypes[i].isPrimitive()) {
                        if (preConvArgs[i] == null ||
                                (parameterTypes[i] != String.class &&
                                        !parameterTypes[i].isAssignableFrom(preConvArgs[i].getClass()))) {

                            ldcClassConstant(getWrapperClass(parameterTypes[i]));

                            assert debug("INVOKESTATIC DataConversion.convert");
                            mv.visitMethodInsn(INVOKESTATIC, NAMESPACE + "DataConversion", "convert",
                                    "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
                        }

                        unwrapPrimitive(parameterTypes[i]);
                    }
                    else if (preConvArgs[i] == null ||
                            (parameterTypes[i] != String.class &&
                                    !parameterTypes[i].isAssignableFrom(preConvArgs[i].getClass()))) {

                        ldcClassConstant(parameterTypes[i]);

                        assert debug("INVOKESTATIC DataConversion.convert");
                        mv.visitMethodInsn(INVOKESTATIC, NAMESPACE + "DataConversion", "convert",
                                "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");

                        assert debug("CHECKCAST " + getInternalName(parameterTypes[i]));
                        mv.visitTypeInsn(CHECKCAST, getInternalName(parameterTypes[i]));
                    }
                    else if (parameterTypes[i] == String.class) {
                        assert debug("<<<DYNAMIC TYPE OPTIMIZATION STRING>>");
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
                    }
                    else {
                        assert debug("<<<DYNAMIC TYPING BYPASS>>>");
                        assert debug("<<<OPT. JUSTIFICATION " + parameterTypes[i] + "=" + preConvArgs[i].getClass() + ">>>");

                        assert debug("CHECKCAST " + getInternalName(parameterTypes[i]));
                        mv.visitTypeInsn(CHECKCAST, getInternalName(parameterTypes[i]));
                    }
                }

                if ((m.getModifiers() & STATIC) != 0) {
                    assert debug("INVOKESTATIC: " + m.getName());
                    mv.visitMethodInsn(INVOKESTATIC, getInternalName(m.getDeclaringClass()), m.getName(), getMethodDescriptor(m));
                }
                else {
                    if (m.getDeclaringClass() != cls && m.getDeclaringClass().isInterface()) {
                        assert debug("INVOKEINTERFACE: " + getInternalName(m.getDeclaringClass()) + "." + m.getName());
                        mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(m.getDeclaringClass()), m.getName(),
                                getMethodDescriptor(m));
                    }
                    else {
                        assert debug("INVOKEVIRTUAL: " + getInternalName(cls) + "." + m.getName());
                        mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(cls), m.getName(),
                                getMethodDescriptor(m));
                    }
                }

                returnType = m.getReturnType();

                stacksize++;
            }

            return m.invoke(ctx, args);
        }
    }

    private void dataConversion(Class target) {
        if (target.equals(Object.class)) return;

        ldcClassConstant(target);
        assert debug("INVOKESTATIC " + NAMESPACE + "DataConversion.convert");
        mv.visitMethodInsn(INVOKESTATIC, NAMESPACE + "DataConversion", "convert", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
    }

    private static MVELClassLoader classLoader;

    public static void setMVELClassLoader(MVELClassLoader cl) {
        classLoader = cl;
    }

    public static MVELClassLoader getMVELClassLoader() {
        return classLoader;
    }

    public void init() {
        try {
            classLoader = new JITClassLoader(currentThread().getContextClassLoader());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private java.lang.Class loadClass(String className, byte[] b) throws Exception {
        /**
         * This must be synchronized.  Two classes cannot be simultaneously deployed in the JVM.
         */

        return classLoader.defineClassX(className, b, 0, b.length);
    }

    private boolean debug(String instruction) {
        if (buildLog != null) {
            buildLog.append(instruction).append("\n");
        }
        return true;
    }

    @SuppressWarnings({"SameReturnValue"})
    public String getName() {
        return "ASM";
    }

    public Object getResultOptPass() {
        return val;
    }

    private Class getWrapperClass(Class cls) {
        if (cls == boolean.class) {
            return Boolean.class;
        }
        else if (cls == int.class) {
            return Integer.class;
        }
        else if (cls == float.class) {
            return Float.class;
        }
        else if (cls == double.class) {
            return Double.class;
        }
        else if (cls == short.class) {
            return Short.class;
        }
        else if (cls == long.class) {
            return Long.class;
        }
        else if (cls == byte.class) {
            return Byte.class;
        }
        else if (cls == char.class) {
            return Character.class;
        }

        return null;
    }

    private void unwrapPrimitive(Class cls) {
        if (cls == boolean.class) {
            assert debug("CHECKCAST java/lang/Boolean");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            assert debug("INVOKEVIRTUAL java/lang/Boolean.booleanValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        }
        else if (cls == int.class) {
            assert debug("CHECKCAST java/lang/Integer");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            assert debug("INVOKEVIRTUAL java/lang/Integer.intValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
        }
        else if (cls == float.class) {
            assert debug("CHECKCAST java/lang/Float");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
            assert debug("INVOKEVIRTUAL java/lang/Float.floatValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
        }
        else if (cls == double.class) {
            assert debug("CHECKCAST java/lang/Double");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            assert debug("INVOKEVIRTUAL java/lang/Double.doubleValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
        }
        else if (cls == short.class) {
            assert debug("CHECKCAST java/lang/Short");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
            assert debug("INVOKEVIRTUAL java/lang/Short.shortValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
        }
        else if (cls == long.class) {
            assert debug("CHECKCAST java/lang/Long");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
            assert debug("INVOKEVIRTUAL java/lang/Long.longValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
        }
        else if (cls == byte.class) {
            assert debug("CHECKCAST java/lang/Byte");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            assert debug("INVOKEVIRTUAL java/lang/Byte.byteValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
        }
        else if (cls == char.class) {
            assert debug("CHECKCAST java/lang/Character");
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            assert debug("INVOKEVIRTUAL java/lang/Character.charValue");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
        }
    }


    private void wrapPrimitive(Class<? extends Object> cls) {
        if (OPCODES_VERSION == Opcodes.V1_4) {
            /**
             * JAVA 1.4 SUCKS!  DIE 1.4 DIE!!!
             */

            debug("** Using 1.4 Bytecode **");

            if (cls == boolean.class || cls == Boolean.class) {
                debug("NEW java/lang/Boolean");
                mv.visitTypeInsn(NEW, "java/lang/Boolean");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Boolan.<init>::(Z)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");

                returnType = Boolean.class;
            }
            else if (cls == int.class || cls == Integer.class) {
                debug("NEW java/lang/Integer");
                mv.visitTypeInsn(NEW, "java/lang/Integer");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Integer.<init>::(I)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V");

                returnType = Integer.class;
            }
            else if (cls == float.class || cls == Float.class) {
                debug("NEW java/lang/Float");
                mv.visitTypeInsn(NEW, "java/lang/Float");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Float.<init>::(F)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Float", "<init>", "(F)V");

                returnType = Float.class;
            }
            else if (cls == double.class || cls == Double.class) {
                debug("NEW java/lang/Double");
                mv.visitTypeInsn(NEW, "java/lang/Double");

                debug("DUP X2");
                mv.visitInsn(DUP_X2);

                debug("DUP X2");
                mv.visitInsn(DUP_X2);

                debug("POP");
                mv.visitInsn(POP);

                debug("INVOKESPECIAL java/lang/Double.<init>::(D)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Double", "<init>", "(D)V");

                returnType = Double.class;
            }
            else if (cls == short.class || cls == Short.class) {
                debug("NEW java/lang/Short");
                mv.visitTypeInsn(NEW, "java/lang/Short");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Short.<init>::(S)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Short", "<init>", "(S)V");

                returnType = Short.class;
            }
            else if (cls == long.class || cls == Long.class) {
                debug("NEW java/lang/Long");
                mv.visitTypeInsn(NEW, "java/lang/Long");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Long.<init>::(L)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Float", "<init>", "(L)V");

                returnType = Long.class;
            }
            else if (cls == byte.class || cls == Byte.class) {
                debug("NEW java/lang/Byte");
                mv.visitTypeInsn(NEW, "java/lang/Byte");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Byte.<init>::(B)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Byte", "<init>", "(B)V");

                returnType = Byte.class;
            }
            else if (cls == char.class || cls == Character.class) {
                debug("NEW java/lang/Character");
                mv.visitTypeInsn(NEW, "java/lang/Character");

                debug("DUP X1");
                mv.visitInsn(DUP_X1);

                debug("SWAP");
                mv.visitInsn(SWAP);

                debug("INVOKESPECIAL java/lang/Character.<init>::(C)V");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Character", "<init>", "(C)V");

                returnType = Character.class;
            }
        }
        else {
            if (cls == boolean.class || cls == Boolean.class) {
                debug("INVOKESTATIC java/lang/Boolean.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                returnType = Boolean.class;
            }
            else if (cls == int.class || cls == Integer.class) {
                debug("INVOKESTATIC java/lang/Integer.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                returnType = Integer.class;
            }
            else if (cls == float.class || cls == Float.class) {
                debug("INVOKESTATIC java/lang/Float.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                returnType = Float.class;
            }
            else if (cls == double.class || cls == Double.class) {
                debug("INVOKESTATIC java/lang/Double.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                returnType = Double.class;
            }
            else if (cls == short.class || cls == Short.class) {
                debug("INVOKESTATIC java/lang/Short.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                returnType = Short.class;
            }
            else if (cls == long.class || cls == Long.class) {
                debug("INVOKESTATIC java/lang/Long.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                returnType = Long.class;
            }
            else if (cls == byte.class || cls == Byte.class) {
                debug("INVOKESTATIC java/lang/Byte.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                returnType = Byte.class;
            }
            else if (cls == char.class || cls == Character.class) {
                debug("INVOKESTATIC java/lang/Character.valueOf");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                returnType = Character.class;
            }
        }
    }

    private void anyArrayCheck(Class<? extends Object> cls) {
        if (cls == boolean[].class) {
            assert debug("CHECKCAST [Z");
            mv.visitTypeInsn(CHECKCAST, "[Z");
        }
        else if (cls == int[].class) {
            assert debug("CHECKCAST [I");
            mv.visitTypeInsn(CHECKCAST, "[I");
        }
        else if (cls == float[].class) {
            assert debug("CHECKCAST [F");
            mv.visitTypeInsn(CHECKCAST, "[F");
        }
        else if (cls == double[].class) {
            assert debug("CHECKCAST [D");
            mv.visitTypeInsn(CHECKCAST, "[D");
        }
        else if (cls == short[].class) {
            assert debug("CHECKCAST [S");
            mv.visitTypeInsn(CHECKCAST, "[S");
        }
        else if (cls == long[].class) {
            assert debug("CHECKCAST [J");
            mv.visitTypeInsn(CHECKCAST, "[J");
        }
        else if (cls == byte[].class) {
            assert debug("CHECKCAST [B");
            mv.visitTypeInsn(CHECKCAST, "[B");
        }
        else if (cls == char[].class) {
            assert debug("CHECKCAST [C");
            mv.visitTypeInsn(CHECKCAST, "[C");
        }
        else {
            assert debug("CHECKCAST [Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        }
    }

    private void writeOutLiteralWrapped(Object lit) {
        if (lit instanceof Integer) {
            intPush((Integer) lit);
            wrapPrimitive(int.class);
            return;
        }

        assert debug("LDC " + lit);
        if (lit instanceof String) {
            mv.visitLdcInsn(lit);
        }
        else if (lit instanceof Long) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(long.class);
        }
        else if (lit instanceof Float) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(float.class);
        }
        else if (lit instanceof Double) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(double.class);
        }
        else if (lit instanceof Short) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(short.class);
        }
        else if (lit instanceof Character) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(char.class);
        }
        else if (lit instanceof Boolean) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(boolean.class);
        }
        else if (lit instanceof Byte) {
            mv.visitLdcInsn(lit);
            wrapPrimitive(byte.class);
        }
    }

    public void arrayStore(Class cls) {
        if (cls.isPrimitive()) {
            if (cls == int.class) {
                assert debug("IASTORE");
                mv.visitInsn(IASTORE);
            }
            else if (cls == char.class) {
                assert debug("CASTORE");
                mv.visitInsn(CASTORE);
            }
            else if (cls == boolean.class) {
                assert debug("BASTORE");
                mv.visitInsn(BASTORE);
            }
            else if (cls == double.class) {
                assert debug("DASTORE");
                mv.visitInsn(DASTORE);
            }
            else if (cls == float.class) {
                assert debug("FASTORE");
                mv.visitInsn(FASTORE);
            }
            else if (cls == short.class) {
                assert debug("SASTORE");
                mv.visitInsn(SASTORE);
            }
            else if (cls == long.class) {
                assert debug("LASTORE");
                mv.visitInsn(LASTORE);
            }
            else if (cls == byte.class) {
                assert debug("BASTORE");
                mv.visitInsn(BASTORE);
            }
        }
        else {
            assert debug("AASTORE");
            mv.visitInsn(AASTORE);
        }

    }

    public void wrapRuntimeConverstion(Class toType) {
        ldcClassConstant(getWrapperClass(toType));

        assert debug("INVOKESTATIC DataConversion.convert");
        mv.visitMethodInsn(INVOKESTATIC, "" + NAMESPACE + "DataConversion", "convert",
                "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
    }


    private void addSubstatement(ExecutableStatement stmt) {
        compiledInputs.add(stmt);

        assert debug("ALOAD 0");
        mv.visitVarInsn(ALOAD, 0);

        assert debug("GETFIELD p" + (compiledInputs.size() - 1));
        mv.visitFieldInsn(GETFIELD, className, "p" + (compiledInputs.size() - 1), "L" + NAMESPACE + "compiler/ExecutableStatement;");

        assert debug("ALOAD 2");
        mv.visitVarInsn(ALOAD, 2);

        assert debug("ALOAD 3");
        mv.visitVarInsn(ALOAD, 3);

        assert debug("INVOKEINTERFACE ExecutableStatement.getValue");
        mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(ExecutableStatement.class), "getValue",
                "(Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;)Ljava/lang/Object;");
    }


    private void loadVariableByName(String name) {
        assert debug("ALOAD 3");
        mv.visitVarInsn(ALOAD, 3);

        assert debug("LDC :" + name);
        mv.visitLdcInsn(name);

        assert debug("INVOKEINTERFACE " + NAMESPACE + "integration/VariableResolverFactory.getVariableResolver");
        mv.visitMethodInsn(INVOKEINTERFACE, "" + NAMESPACE + "integration/VariableResolverFactory",
                "getVariableResolver", "(Ljava/lang/String;)L" + NAMESPACE + "integration/VariableResolver;");

        assert debug("INVOKEINTERFACE " + NAMESPACE + "integration/VariableResolver.getValue");
        mv.visitMethodInsn(INVOKEINTERFACE, "" + NAMESPACE + "integration/VariableResolver",
                "getValue", "()Ljava/lang/Object;");

        returnType = Object.class;
    }

    private void loadVariableByIndex(int pos) {
        assert debug("ALOAD 3");
        mv.visitVarInsn(ALOAD, 3);

        assert debug("PUSH IDX VAL =" + pos);
        intPush(pos);

        assert debug("INVOKEINTERFACE " + NAMESPACE + "integration/VariableResolverFactory.getIndexedVariableResolver");
        mv.visitMethodInsn(INVOKEINTERFACE, "" + NAMESPACE + "integration/VariableResolverFactory",
                "getIndexedVariableResolver", "(I)L" + NAMESPACE + "integration/VariableResolver;");

        assert debug("INVOKEINTERFACE " + NAMESPACE + "integration/VariableResolver.getValue");
        mv.visitMethodInsn(INVOKEINTERFACE, "" + NAMESPACE + "integration/VariableResolver",
                "getValue", "()Ljava/lang/Object;");

        returnType = Object.class;
    }

    private void loadField(int number) {
        assert debug("ALOAD 0");
        mv.visitVarInsn(ALOAD, 0);

        assert debug("GETFIELD p" + number);
        mv.visitFieldInsn(GETFIELD, className, "p" + number, "L" + NAMESPACE + "compiler/ExecutableStatement;");
    }

    private void ldcClassConstant(Class cls) {
        if (OPCODES_VERSION == Opcodes.V1_4) {
            assert debug("LDC \"" + cls.getName() + "\"");
            mv.visitLdcInsn(cls.getName());
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
            Label l4 = new Label();
            mv.visitJumpInsn(GOTO, l4);
            mv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
            mv.visitInsn(DUP_X1);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(l4);
        }
        else {
            assert debug("LDC " + getType(cls));
            mv.visitLdcInsn(getType(cls));
        }
    }

    private void buildInputs() {
        if (compiledInputs.size() == 0) return;

        assert debug("\n{SETTING UP MEMBERS...}\n");

        StringAppender constSig = new StringAppender("(");
        int size = compiledInputs.size();

        for (int i = 0; i < size; i++) {
            assert debug("ACC_PRIVATE p" + i);
            cw.visitField(ACC_PRIVATE, "p" + i, "L" + NAMESPACE + "compiler/ExecutableStatement;", null, null).visitEnd();

            constSig.append("L" + NAMESPACE + "compiler/ExecutableStatement;");
        }
        constSig.append(")V");

        assert debug("\n{CREATING INJECTION CONSTRUCTOR}\n");

        MethodVisitor cv = cw.visitMethod(ACC_PUBLIC, "<init>", constSig.toString(), null, null);
        cv.visitCode();
        assert debug("ALOAD 0");
        cv.visitVarInsn(ALOAD, 0);
        assert debug("INVOKESPECIAL java/lang/Object.<init>");
        cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

        for (int i = 0; i < size; i++) {
            assert debug("ALOAD 0");
            cv.visitVarInsn(ALOAD, 0);
            assert debug("ALOAD " + (i + 1));
            cv.visitVarInsn(ALOAD, i + 1);
            assert debug("PUTFIELD p" + i);
            cv.visitFieldInsn(PUTFIELD, className, "p" + i, "L" + NAMESPACE + "compiler/ExecutableStatement;");
        }

        assert debug("RETURN");
        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);
        cv.visitEnd();

        assert debug("}");
    }

    private static final int ARRAY = 0;
    private static final int LIST = 1;
    private static final int MAP = 2;
    private static final int VAL = 3;

    private int _getAccessor(Object o, Class type) {
        if (o instanceof List) {
            assert debug("NEW " + LIST_IMPL);
            mv.visitTypeInsn(NEW, LIST_IMPL);

            assert debug("DUP");
            mv.visitInsn(DUP);

            assert debug("DUP");
            mv.visitInsn(DUP);

            intPush(((List) o).size());
            assert debug("INVOKESPECIAL " + LIST_IMPL + ".<init>");
            mv.visitMethodInsn(INVOKESPECIAL, LIST_IMPL, "<init>", "(I)V");

            for (Object item : (List) o) {
                if (_getAccessor(item, type) != VAL) {
                    assert debug("POP");
                    mv.visitInsn(POP);
                }

                assert debug("INVOKEINTERFACE java/util/List.add");
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");

                assert debug("POP");
                mv.visitInsn(POP);

                assert debug("DUP");
                mv.visitInsn(DUP);
            }

            returnType = List.class;

            return LIST;
        }
        else if (o instanceof Map) {
            assert debug("NEW " + MAP_IMPL);
            mv.visitTypeInsn(NEW, MAP_IMPL);

            assert debug("DUP");
            mv.visitInsn(DUP);

            assert debug("DUP");
            mv.visitInsn(DUP);

            intPush(((Map) o).size());

            assert debug("INVOKESPECIAL " + MAP_IMPL + ".<init>");
            mv.visitMethodInsn(INVOKESPECIAL, MAP_IMPL, "<init>", "(I)V");

            for (Object item : ((Map) o).keySet()) {
                mv.visitTypeInsn(CHECKCAST, "java/util/Map");

                if (_getAccessor(item, type) != VAL) {
                    assert debug("POP");
                    mv.visitInsn(POP);
                }
                if (_getAccessor(((Map) o).get(item), type) != VAL) {
                    assert debug("POP");
                    mv.visitInsn(POP);
                }

                assert debug("INVOKEINTERFACE java/util/Map.put");
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

                assert debug("POP");
                mv.visitInsn(POP);

                assert debug("DUP");
                mv.visitInsn(DUP);
            }

            returnType = Map.class;

            return MAP;
        }
        else if (o instanceof Object[]) {
            Accessor[] a = new Accessor[((Object[]) o).length];
            int i = 0;
            int dim = 0;

            if (type != null) {
                String nm = type.getName();
                while (nm.charAt(dim) == '[') dim++;
            }
            else {
                type = Object[].class;
                dim = 1;
            }

            try {
                intPush(((Object[]) o).length);
                assert debug("ANEWARRAY " + getInternalName(getSubComponentType(type)) + " (" + ((Object[]) o).length + ")");
                mv.visitTypeInsn(ANEWARRAY, getInternalName(getSubComponentType(type)));

                Class cls = dim > 1 ? findClass(null, repeatChar('[', dim - 1) + "L" + getBaseComponentType(type).getName() + ";")
                        : type;


                assert debug("DUP");
                mv.visitInsn(DUP);

                for (Object item : (Object[]) o) {
                    intPush(i);

                    if (_getAccessor(item, cls) != VAL) {
                        assert debug("POP");
                        mv.visitInsn(POP);
                    }

                    assert debug("AASTORE (" + o.hashCode() + ")");
                    mv.visitInsn(AASTORE);

                    assert debug("DUP");
                    mv.visitInsn(DUP);

                    i++;
                }

            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("this error should never throw:" + getBaseComponentType(type).getName(), e);
            }

            return ARRAY;
        }
        else {
            if (type.isArray()) {
                writeLiteralOrSubexpression(subCompileExpression(((String) o).toCharArray()), getSubComponentType(type));
            }
            else {
                writeLiteralOrSubexpression(subCompileExpression(((String) o).toCharArray()));
            }
            return VAL;
        }
    }

    private Class writeLiteralOrSubexpression(Object stmt) {
        return writeLiteralOrSubexpression(stmt, null, null);
    }

    private Class writeLiteralOrSubexpression(Object stmt, Class desiredTarget) {
        return writeLiteralOrSubexpression(stmt, desiredTarget, null);
    }

    private Class writeLiteralOrSubexpression(Object stmt, Class desiredTarget, Class knownIngressType) {
        if (stmt instanceof ExecutableLiteral) {
            Class type = ((ExecutableLiteral) stmt).getLiteral().getClass();

            assert debug("*** type:" + type + ";desired:" + desiredTarget);

            if (type == Integer.class && desiredTarget == int.class) {
                intPush(((ExecutableLiteral) stmt).getInteger32());
                type = int.class;
            }
            else if (desiredTarget != null && type != desiredTarget) {
                assert debug("*** Converting because desiredType(" + desiredTarget.getClass() + ") is not: " + type);

                if (!DataConversion.canConvert(type, desiredTarget)) {
                    throw new CompileException("was expecting type: " + desiredTarget.getName() + "; but found type: " + type.getName());
                }
                writeOutLiteralWrapped(convert(((ExecutableLiteral) stmt).getLiteral(), desiredTarget));
            }
            else {
                writeOutLiteralWrapped(((ExecutableLiteral) stmt).getLiteral());
            }

            return type;
        }
        else {
            literal = false;

            addSubstatement((ExecutableStatement) stmt);

            Class type;
            if (knownIngressType == null) {
                type = ((ExecutableStatement) stmt).getKnownEgressType();
            }
            else {
                type = knownIngressType;
            }

            if (desiredTarget != null && type != desiredTarget) {
                //      dataConversion(desiredTarget);
                if (desiredTarget.isPrimitive()) {
                    if (type == null) throw new OptimizationFailure("cannot optimize expression: " + new String(expr) +
                            ": cannot determine ingress type for primitive output");

                    checkcast(type);
                    unwrapPrimitive(desiredTarget);
                }
            }

            return type;
        }
    }

    private void addPrintOut(String text) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(text);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    }


    public Accessor optimizeCollection(Object o, Class type, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory) {
        this.cursor = 0;
        this.returnType = type;
        if (property != null) this.length = (this.expr = property).length;
        this.compiledInputs = new ArrayList<ExecutableStatement>();

        this.ctx = ctx;
        this.thisRef = thisRef;
        this.variableFactory = factory;

        _initJIT();

        literal = true;

        _getAccessor(o, type);


        _finishJIT();

        try {
            Accessor compiledAccessor = _initializeAccessor();

            if (property != null && property.length > 0) {
                return new Union(compiledAccessor, property);
            }
            else {
                return compiledAccessor;
            }

        }
        catch (Exception e) {
            throw new OptimizationFailure("could not optimize collection", e);
        }
    }

    private void checkcast(Class cls) {
        assert debug("CHECKCAST " + getInternalName(cls));
        mv.visitTypeInsn(CHECKCAST, getInternalName(cls));
    }

    private void intPush(int index) {
        if (index < 6) {
            switch (index) {
                case 0:
                    assert debug("ICONST_0");
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    assert debug("ICONST_1");
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    assert debug("ICONST_2");
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    assert debug("ICONST_3");
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    assert debug("ICONST_4");
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    assert debug("ICONST_5");
                    mv.visitInsn(ICONST_5);
                    break;
            }
        }
        else if (index < Byte.MAX_VALUE) {
            assert debug("BIPUSH " + index);
            mv.visitIntInsn(BIPUSH, index);
        }
        else {
            assert debug("SIPUSH " + index);
            mv.visitIntInsn(SIPUSH, index);
        }
    }

    public Accessor optimizeObjectCreation(ParserContext pCtx, char[] property, Object ctx, Object thisRef, VariableResolverFactory factory) {
        _initJIT();

        compiledInputs = new ArrayList<ExecutableStatement>();
        this.length = (this.expr = property).length;
        this.ctx = ctx;
        this.thisRef = thisRef;
        this.variableFactory = factory;
        this.pCtx = pCtx;

        String[] cnsRes = captureContructorAndResidual(property);
        String[] constructorParms = parseMethodOrConstructor(cnsRes[0].toCharArray());

        try {
            if (constructorParms != null) {
                for (String constructorParm : constructorParms) {
                    compiledInputs.add((ExecutableStatement) subCompileExpression(constructorParm.toCharArray()));
                }

                Class cls = findClass(factory, new String(subset(property, 0, findFirst('(', property))));

                assert debug("NEW " + getInternalName(cls));
                mv.visitTypeInsn(NEW, getInternalName(cls));
                assert debug("DUP");
                mv.visitInsn(DUP);

                Object[] parms = new Object[constructorParms.length];

                int i = 0;
                for (ExecutableStatement es : compiledInputs) {
                    parms[i++] = es.getValue(ctx, factory);
                }

                Constructor cns = getBestConstructorCanadidate(parms, cls);

                if (cns == null)
                    throw new CompileException("unable to find constructor for: " + cls.getName());

                Class tg;
                for (i = 0; i < constructorParms.length; i++) {
                    assert debug("ALOAD 0");
                    mv.visitVarInsn(ALOAD, 0);
                    assert debug("GETFIELD p" + i);
                    mv.visitFieldInsn(GETFIELD, className, "p" + i, "L" + NAMESPACE + "compiler/ExecutableStatement;");
                    assert debug("ALOAD 2");
                    mv.visitVarInsn(ALOAD, 2);
                    assert debug("ALOAD 3");
                    mv.visitVarInsn(ALOAD, 3);
                    assert debug("INVOKEINTERFACE " + NAMESPACE + "compiler/ExecutableStatement.getValue");
                    mv.visitMethodInsn(INVOKEINTERFACE, "" + NAMESPACE + "compiler/ExecutableStatement", "getValue", "(Ljava/lang/Object;L" + NAMESPACE + "integration/VariableResolverFactory;)Ljava/lang/Object;");

                    tg = cns.getParameterTypes()[i].isPrimitive()
                            ? getWrapperClass(cns.getParameterTypes()[i]) : cns.getParameterTypes()[i];

                    if (parms[i] != null && !parms[i].getClass().isAssignableFrom(cns.getParameterTypes()[i])) {
                        ldcClassConstant(tg);

                        assert debug("INVOKESTATIC " + NAMESPACE + "DataConversion.convert");
                        mv.visitMethodInsn(INVOKESTATIC, "" + NAMESPACE + "DataConversion", "convert", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");

                        if (cns.getParameterTypes()[i].isPrimitive()) {
                            unwrapPrimitive(cns.getParameterTypes()[i]);
                        }
                        else {
                            assert debug("CHECKCAST " + getInternalName(tg));
                            mv.visitTypeInsn(CHECKCAST, getInternalName(tg));
                        }

                    }
                    else {
                        assert debug("CHECKCAST " + getInternalName(cns.getParameterTypes()[i]));
                        mv.visitTypeInsn(CHECKCAST, getInternalName(cns.getParameterTypes()[i]));
                    }

                }

                assert debug("INVOKESPECIAL " + getInternalName(cls) + ".<init> : " + getConstructorDescriptor(cns));
                mv.visitMethodInsn(INVOKESPECIAL, getInternalName(cls), "<init>", getConstructorDescriptor(cns));

                _finishJIT();

                Accessor acc = _initializeAccessor();

                if (cnsRes.length > 1 && cnsRes[1] != null && !cnsRes[1].trim().equals("")) {
                    return new Union(acc, cnsRes[1].toCharArray());
                }

                return acc;
            }
            else {
                Class cls = findClass(factory, new String(property));

                assert debug("NEW " + getInternalName(cls));
                mv.visitTypeInsn(NEW, getInternalName(cls));
                assert debug("DUP");
                mv.visitInsn(DUP);

                Constructor cns = cls.getConstructor(EMPTYCLS);

                assert debug("INVOKESPECIAL <init>");

                mv.visitMethodInsn(INVOKESPECIAL, getInternalName(cls), "<init>", getConstructorDescriptor(cns));

                _finishJIT();
                Accessor acc = _initializeAccessor();

                if (cnsRes.length > 1 && cnsRes[1] != null && !cnsRes[1].trim().equals("")) {
                    return new Union(acc, cnsRes[1].toCharArray());
                }

                return acc;
            }
        }
        catch (ClassNotFoundException e) {
            throw new CompileException("class or class reference not found: " + new String(property));
        }
        catch (Exception e) {
            throw new OptimizationFailure("could not optimize construtor: " + new String(property), e);
        }
    }

    public Class getEgressType() {
        return returnType;
    }

    private void dumpAdvancedDebugging() {
        if (buildLog == null) return;

        System.out.println("JIT Compiler Dump for: <<" + (expr == null ? null : new String(expr)) + ">>\n-------------------------------\n");
        System.out.println(buildLog.toString());
        System.out.println("\n<END OF DUMP>\n");
        if (MVEL.isFileDebugging()) {
            try {
                FileWriter writer = ParseTools.getDebugFileWriter();
                writer.write(buildLog.toString());
                writer.flush();
                writer.close();
            }
            catch (IOException e) {
                //empty
            }
        }
    }

    public boolean isLiteralOnly() {
        return literal;
    }
}
