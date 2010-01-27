/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mvel2.asm;

/**
 * A {@link ClassVisitor} that generates classes in bytecode form. More
 * precisely this visitor generates a byte array conforming to the Java class
 * file format. It can be used alone, to generate a Java class "from scratch",
 * or with one or more {@link ClassReader ClassReader} and adapter class visitor
 * to generate a modified class from one or more existing Java classes.
 *
 * @author Eric Bruneton
 */
public class ClassWriter implements ClassVisitor {

    /**
     * Flag to automatically compute the maximum stack size and the maximum
     * number of local variables of methods. If this flag is set, then the
     * arguments of the {@link MethodVisitor#visitMaxs visitMaxs} method of the
     * {@link MethodVisitor} returned by the {@link #visitMethod visitMethod}
     * method will be ignored, and computed automatically from the signature and
     * the bytecode of each method.
     *
     * @see #ClassWriter(int)
     */
    public final static int COMPUTE_MAXS = 1;

    /**
     * Flag to automatically compute the stack map frames of methods from
     * scratch. If this flag is set, then the calls to the
     * {@link MethodVisitor#visitFrame} method are ignored, and the stack map
     * frames are recomputed from the methods bytecode. The arguments of the
     * {@link MethodVisitor#visitMaxs visitMaxs} method are also ignored and
     * recomputed from the bytecode. In other words, computeFrames implies
     * computeMaxs.
     *
     * @see #ClassWriter(int)
     */
    public final static int COMPUTE_FRAMES = 2;

    /**
     * The type of instructions without any argument.
     */
    final static int NOARG_INSN = 0;

    /**
     * The type of instructions with an signed byte argument.
     */
    final static int SBYTE_INSN = 1;

    /**
     * The type of instructions with an signed short argument.
     */
    final static int SHORT_INSN = 2;

    /**
     * The type of instructions with a local variable index argument.
     */
    final static int VAR_INSN = 3;

    /**
     * The type of instructions with an implicit local variable index argument.
     */
    final static int IMPLVAR_INSN = 4;

    /**
     * The type of instructions with a type descriptor argument.
     */
    final static int TYPE_INSN = 5;

    /**
     * The type of field and method invocations instructions.
     */
    final static int FIELDORMETH_INSN = 6;

    /**
     * The type of the INVOKEINTERFACE instruction.
     */
    final static int ITFMETH_INSN = 7;

    /**
     * The type of instructions with a 2 bytes bytecode offset label.
     */
    final static int LABEL_INSN = 8;

    /**
     * The type of instructions with a 4 bytes bytecode offset label.
     */
    final static int LABELW_INSN = 9;

    /**
     * The type of the LDC instruction.
     */
    final static int LDC_INSN = 10;

    /**
     * The type of the LDC_W and LDC2_W instructions.
     */
    final static int LDCW_INSN = 11;

    /**
     * The type of the IINC instruction.
     */
    final static int IINC_INSN = 12;

    /**
     * The type of the TABLESWITCH instruction.
     */
    final static int TABL_INSN = 13;

    /**
     * The type of the LOOKUPSWITCH instruction.
     */
    final static int LOOK_INSN = 14;

    /**
     * The type of the MULTIANEWARRAY instruction.
     */
    final static int MANA_INSN = 15;

    /**
     * The type of the WIDE instruction.
     */
    final static int WIDE_INSN = 16;

    /**
     * The instruction types of all JVM opcodes.
     */
    static byte[] TYPE;

    /**
     * The type of CONSTANT_Class constant pool items.
     */
    final static int CLASS = 7;

    /**
     * The type of CONSTANT_Fieldref constant pool items.
     */
    final static int FIELD = 9;

    /**
     * The type of CONSTANT_Methodref constant pool items.
     */
    final static int METH = 10;

    /**
     * The type of CONSTANT_InterfaceMethodref constant pool items.
     */
    final static int IMETH = 11;

    /**
     * The type of CONSTANT_String constant pool items.
     */
    final static int STR = 8;

    /**
     * The type of CONSTANT_Integer constant pool items.
     */
    final static int INT = 3;

    /**
     * The type of CONSTANT_Float constant pool items.
     */
    final static int FLOAT = 4;

    /**
     * The type of CONSTANT_Long constant pool items.
     */
    final static int LONG = 5;

    /**
     * The type of CONSTANT_Double constant pool items.
     */
    final static int DOUBLE = 6;

    /**
     * The type of CONSTANT_NameAndType constant pool items.
     */
    final static int NAME_TYPE = 12;

    /**
     * The type of CONSTANT_Utf8 constant pool items.
     */
    final static int UTF8 = 1;

    /**
     * Normal type Item stored in the ClassWriter {@link ClassWriter#typeTable},
     * instead of the constant pool, in order to avoid clashes with normal
     * constant pool items in the ClassWriter constant pool's hash table.
     */
    final static int TYPE_NORMAL = 13;

    /**
     * Uninitialized type Item stored in the ClassWriter
     * {@link ClassWriter#typeTable}, instead of the constant pool, in order to
     * avoid clashes with normal constant pool items in the ClassWriter constant
     * pool's hash table.
     */
    final static int TYPE_UNINIT = 14;

    /**
     * Merged type Item stored in the ClassWriter {@link ClassWriter#typeTable},
     * instead of the constant pool, in order to avoid clashes with normal
     * constant pool items in the ClassWriter constant pool's hash table.
     */
    final static int TYPE_MERGED = 15;

    /**
     * The class reader from which this class writer was constructed, if any.
     */
    ClassReader cr;

    /**
     * Minor and major version numbers of the class to be generated.
     */
    int version;

    /**
     * Index of the next item to be added in the constant pool.
     */
    int index;

    /**
     * The constant pool of this class.
     */
    ByteVector pool;

    /**
     * The constant pool's hash table data.
     */
    Item[] items;

    /**
     * The threshold of the constant pool's hash table.
     */
    int threshold;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    Item key;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    Item key2;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    Item key3;

    /**
     * A type table used to temporarily store internal names that will not
     * necessarily be stored in the constant pool. This type table is used by
     * the control flow and data flow analysis algorithm used to compute stack
     * map frames from scratch. This array associates to each index <tt>i</tt>
     * the Item whose index is <tt>i</tt>. All Item objects stored in this
     * array are also stored in the {@link #items} hash table. These two arrays
     * allow to retrieve an Item from its index or, conversly, to get the index
     * of an Item from its value. Each Item stores an internal name in its
     * {@link Item#strVal1} field.
     */
    Item[] typeTable;

    /**
     * Number of elements in the {@link #typeTable} array.
     */
    private short typeCount;

    /**
     * The access flags of this class.
     */
    private int access;

    /**
     * The constant pool item that contains the internal name of this class.
     */
    private int name;

    /**
     * The internal name of this class.
     */
    String thisName;

    /**
     * The constant pool item that contains the signature of this class.
     */
    private int signature;

    /**
     * The constant pool item that contains the internal name of the super class
     * of this class.
     */
    private int superName;

    /**
     * Number of interfaces implemented or extended by this class or interface.
     */
    private int interfaceCount;

    /**
     * The interfaces implemented or extended by this class or interface. More
     * precisely, this array contains the indexes of the constant pool items
     * that contain the internal names of these interfaces.
     */
    private int[] interfaces;

    /**
     * The index of the constant pool item that contains the name of the source
     * file from which this class was compiled.
     */
    private int sourceFile;

    /**
     * The SourceDebug attribute of this class.
     */
    private ByteVector sourceDebug;

    /**
     * The constant pool item that contains the name of the enclosing class of
     * this class.
     */
    private int enclosingMethodOwner;

    /**
     * The constant pool item that contains the name and descriptor of the
     * enclosing method of this class.
     */
    private int enclosingMethod;

    /**
     * The runtime visible annotations of this class.
     */
    private AnnotationWriter anns;

    /**
     * The runtime invisible annotations of this class.
     */
    private AnnotationWriter ianns;

    /**
     * The non standard attributes of this class.
     */
    private Attribute attrs;

    /**
     * The number of entries in the InnerClasses attribute.
     */
    private int innerClassesCount;

    /**
     * The InnerClasses attribute.
     */
    private ByteVector innerClasses;

    /**
     * The fields of this class. These fields are stored in a linked list of
     * {@link FieldWriter} objects, linked to each other by their
     * {@link FieldWriter#next} field. This field stores the first element of
     * this list.
     */
    FieldWriter firstField;

    /**
     * The fields of this class. These fields are stored in a linked list of
     * {@link FieldWriter} objects, linked to each other by their
     * {@link FieldWriter#next} field. This field stores the last element of
     * this list.
     */
    FieldWriter lastField;

    /**
     * The methods of this class. These methods are stored in a linked list of
     * {@link MethodWriter} objects, linked to each other by their
     * {@link MethodWriter#next} field. This field stores the first element of
     * this list.
     */
    MethodWriter firstMethod;

    /**
     * The methods of this class. These methods are stored in a linked list of
     * {@link MethodWriter} objects, linked to each other by their
     * {@link MethodWriter#next} field. This field stores the last element of
     * this list.
     */
    MethodWriter lastMethod;

    /**
     * <tt>true</tt> if the maximum stack size and number of local variables
     * must be automatically computed.
     */
    private boolean computeMaxs;

    /**
     * <tt>true</tt> if the stack map frames must be recomputed from scratch.
     */
    private boolean computeFrames;

    /**
     * <tt>true</tt> if the stack map tables of this class are invalid. The
     * {@link MethodWriter#resizeInstructions} method cannot transform existing
     * stack map tables, and so produces potentially invalid classes when it is
     * executed. In this case the class is reread and rewritten with the
     * {@link #COMPUTE_FRAMES} option (the resizeInstructions method can resize
     * stack map tables when this option is used).
     */
    boolean invalidFrames;

    // ------------------------------------------------------------------------
    // Static initializer
    // ------------------------------------------------------------------------

    /**
     * Computes the instruction types of JVM opcodes.
     */
    static {
        int i;
        byte[] b = new byte[220];
        String s = "AAAAAAAAAAAAAAAABCKLLDDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAADD"
                + "DDDEEEEEEEEEEEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAMAAAAAAAAAAAAAAAAAAAAIIIIIIIIIIIIIIIIDNOAA"
                + "AAAAGGGGGGGHAFBFAAFFAAQPIIJJIIIIIIIIIIIIIIIIII";
        for (i = 0; i < b.length; ++i) {
            b[i] = (byte) (s.charAt(i) - 'A');
        }
        TYPE = b;

        // code to generate the above string
        //
        // // SBYTE_INSN instructions
        // b[Constants.NEWARRAY] = SBYTE_INSN;
        // b[Constants.BIPUSH] = SBYTE_INSN;
        //
        // // SHORT_INSN instructions
        // b[Constants.SIPUSH] = SHORT_INSN;
        //
        // // (IMPL)VAR_INSN instructions
        // b[Constants.RET] = VAR_INSN;
        // for (i = Constants.ILOAD; i <= Constants.ALOAD; ++i) {
        // b[i] = VAR_INSN;
        // }
        // for (i = Constants.ISTORE; i <= Constants.ASTORE; ++i) {
        // b[i] = VAR_INSN;
        // }
        // for (i = 26; i <= 45; ++i) { // ILOAD_0 to ALOAD_3
        // b[i] = IMPLVAR_INSN;
        // }
        // for (i = 59; i <= 78; ++i) { // ISTORE_0 to ASTORE_3
        // b[i] = IMPLVAR_INSN;
        // }
        //
        // // TYPE_INSN instructions
        // b[Constants.NEW] = TYPE_INSN;
        // b[Constants.ANEWARRAY] = TYPE_INSN;
        // b[Constants.CHECKCAST] = TYPE_INSN;
        // b[Constants.INSTANCEOF] = TYPE_INSN;
        //
        // // (Set)FIELDORMETH_INSN instructions
        // for (i = Constants.GETSTATIC; i <= Constants.INVOKESTATIC; ++i) {
        // b[i] = FIELDORMETH_INSN;
        // }
        // b[Constants.INVOKEINTERFACE] = ITFMETH_INSN;
        //
        // // LABEL(W)_INSN instructions
        // for (i = Constants.IFEQ; i <= Constants.JSR; ++i) {
        // b[i] = LABEL_INSN;
        // }
        // b[Constants.IFNULL] = LABEL_INSN;
        // b[Constants.IFNONNULL] = LABEL_INSN;
        // b[200] = LABELW_INSN; // GOTO_W
        // b[201] = LABELW_INSN; // JSR_W
        // // temporary opcodes used internally by ASM - see Label and
        // MethodWriter
        // for (i = 202; i < 220; ++i) {
        // b[i] = LABEL_INSN;
        // }
        //
        // // LDC(_W) instructions
        // b[Constants.LDC] = LDC_INSN;
        // b[19] = LDCW_INSN; // LDC_W
        // b[20] = LDCW_INSN; // LDC2_W
        //
        // // special instructions
        // b[Constants.IINC] = IINC_INSN;
        // b[Constants.TABLESWITCH] = TABL_INSN;
        // b[Constants.LOOKUPSWITCH] = LOOK_INSN;
        // b[Constants.MULTIANEWARRAY] = MANA_INSN;
        // b[196] = WIDE_INSN; // WIDE
        //
        // for (i = 0; i < b.length; ++i) {
        // System.err.print((char)('A' + b[i]));
        // }
        // System.err.println();
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link ClassWriter} object.
     *
     * @param flags option flags that can be used to modify the default behavior
     *              of this class. See {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
     */
    public ClassWriter(final int flags) {
        index = 1;
        pool = new ByteVector();
        items = new Item[256];
        threshold = (int) (0.75d * items.length);
        key = new Item();
        key2 = new Item();
        key3 = new Item();
        this.computeMaxs = (flags & COMPUTE_MAXS) != 0;
        this.computeFrames = (flags & COMPUTE_FRAMES) != 0;
    }

    /**
     * Constructs a new {@link ClassWriter} object and enables optimizations for
     * "mostly add" bytecode transformations. These optimizations are the
     * following:
     * <p/>
     * <ul> <li>The constant pool from the original class is copied as is in
     * the new class, which saves time. New constant pool entries will be added
     * at the end if necessary, but unused constant pool entries <i>won't be
     * removed</i>.</li> <li>Methods that are not transformed are copied as
     * is in the new class, directly from the original class bytecode (i.e.
     * without emitting visit events for all the method instructions), which
     * saves a <i>lot</i> of time. Untransformed methods are detected by the
     * fact that the {@link ClassReader} receives {@link MethodVisitor} objects
     * that come from a {@link ClassWriter} (and not from a custom
     * {@link ClassAdapter} or any other {@link ClassVisitor} instance).</li>
     * </ul>
     *
     * @param classReader the {@link ClassReader} used to read the original
     *                    class. It will be used to copy the entire constant pool from the
     *                    original class and also to copy other fragments of original
     *                    bytecode where applicable.
     * @param flags       option flags that can be used to modify the default behavior
     *                    of this class. See {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
     */
    public ClassWriter(final ClassReader classReader, final int flags) {
        this(flags);
        classReader.copyPool(this);
        this.cr = classReader;
    }

    // ------------------------------------------------------------------------
    // Implementation of the ClassVisitor interface
    // ------------------------------------------------------------------------

    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = newClass(name);
        thisName = name;
        if (signature != null) {
            this.signature = newUTF8(signature);
        }
        this.superName = superName == null ? 0 : newClass(superName);
        if (interfaces != null && interfaces.length > 0) {
            interfaceCount = interfaces.length;
            this.interfaces = new int[interfaceCount];
            for (int i = 0; i < interfaceCount; ++i) {
                this.interfaces[i] = newClass(interfaces[i]);
            }
        }
    }

    public void visitSource(final String file, final String debug) {
        if (file != null) {
            sourceFile = newUTF8(file);
        }
        if (debug != null) {
            sourceDebug = new ByteVector().putUTF8(debug);
        }
    }

    public void visitOuterClass(
            final String owner,
            final String name,
            final String desc) {
        enclosingMethodOwner = newClass(owner);
        if (name != null && desc != null) {
            enclosingMethod = newNameType(name, desc);
        }
    }

    public AnnotationVisitor visitAnnotation(
            final String desc,
            final boolean visible) {
        ByteVector bv = new ByteVector();
        // write type, and reserve space for values count
        bv.putShort(newUTF8(desc)).putShort(0);
        AnnotationWriter aw = new AnnotationWriter(this, true, bv, bv, 2);
        if (visible) {
            aw.next = anns;
            anns = aw;
        }
        else {
            aw.next = ianns;
            ianns = aw;
        }
        return aw;
    }

    public void visitAttribute(final Attribute attr) {
        attr.next = attrs;
        attrs = attr;
    }

    public void visitInnerClass(
            final String name,
            final String outerName,
            final String innerName,
            final int access) {
        if (innerClasses == null) {
            innerClasses = new ByteVector();
        }
        ++innerClassesCount;
        innerClasses.putShort(name == null ? 0 : newClass(name));
        innerClasses.putShort(outerName == null ? 0 : newClass(outerName));
        innerClasses.putShort(innerName == null ? 0 : newUTF8(innerName));
        innerClasses.putShort(access);
    }

    public FieldVisitor visitField(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final Object value) {
        return new FieldWriter(this, access, name, desc, signature, value);
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions) {
        return new MethodWriter(this,
                access,
                name,
                desc,
                signature,
                exceptions,
                computeMaxs,
                computeFrames);
    }

    public void visitEnd() {
    }

    // ------------------------------------------------------------------------
    // Other public methods
    // ------------------------------------------------------------------------

    /**
     * Returns the bytecode of the class that was build with this class writer.
     *
     * @return the bytecode of the class that was build with this class writer.
     */
    public byte[] toByteArray() {
        // computes the real size of the bytecode of this class
        int size = 24 + 2 * interfaceCount;
        int nbFields = 0;
        FieldWriter fb = firstField;
        while (fb != null) {
            ++nbFields;
            size += fb.getSize();
            fb = fb.next;
        }
        int nbMethods = 0;
        MethodWriter mb = firstMethod;
        while (mb != null) {
            ++nbMethods;
            size += mb.getSize();
            mb = mb.next;
        }
        int attributeCount = 0;
        if (signature != 0) {
            ++attributeCount;
            size += 8;
            newUTF8("Signature");
        }
        if (sourceFile != 0) {
            ++attributeCount;
            size += 8;
            newUTF8("SourceFile");
        }
        if (sourceDebug != null) {
            ++attributeCount;
            size += sourceDebug.length + 4;
            newUTF8("SourceDebugExtension");
        }
        if (enclosingMethodOwner != 0) {
            ++attributeCount;
            size += 10;
            newUTF8("EnclosingMethod");
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            ++attributeCount;
            size += 6;
            newUTF8("Deprecated");
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && (version & 0xffff) < Opcodes.V1_5) {
            ++attributeCount;
            size += 6;
            newUTF8("Synthetic");
        }
        if (innerClasses != null) {
            ++attributeCount;
            size += 8 + innerClasses.length;
            newUTF8("InnerClasses");
        }
        if (anns != null) {
            ++attributeCount;
            size += 8 + anns.getSize();
            newUTF8("RuntimeVisibleAnnotations");
        }
        if (ianns != null) {
            ++attributeCount;
            size += 8 + ianns.getSize();
            newUTF8("RuntimeInvisibleAnnotations");
        }
        if (attrs != null) {
            attributeCount += attrs.getCount();
            size += attrs.getSize(this, null, 0, -1, -1);
        }
        size += pool.length;
        // allocates a byte vector of this size, in order to avoid unnecessary
        // arraycopy operations in the ByteVector.enlarge() method
        ByteVector out = new ByteVector(size);
        out.putInt(0xCAFEBABE).putInt(version);
        out.putShort(index).putByteArray(pool.data, 0, pool.length);
        out.putShort(access).putShort(name).putShort(superName);
        out.putShort(interfaceCount);
        for (int i = 0; i < interfaceCount; ++i) {
            out.putShort(interfaces[i]);
        }
        out.putShort(nbFields);
        fb = firstField;
        while (fb != null) {
            fb.put(out);
            fb = fb.next;
        }
        out.putShort(nbMethods);
        mb = firstMethod;
        while (mb != null) {
            mb.put(out);
            mb = mb.next;
        }
        out.putShort(attributeCount);
        if (signature != 0) {
            out.putShort(newUTF8("Signature")).putInt(2).putShort(signature);
        }
        if (sourceFile != 0) {
            out.putShort(newUTF8("SourceFile")).putInt(2).putShort(sourceFile);
        }
        if (sourceDebug != null) {
            int len = sourceDebug.length - 2;
            out.putShort(newUTF8("SourceDebugExtension")).putInt(len);
            out.putByteArray(sourceDebug.data, 2, len);
        }
        if (enclosingMethodOwner != 0) {
            out.putShort(newUTF8("EnclosingMethod")).putInt(4);
            out.putShort(enclosingMethodOwner).putShort(enclosingMethod);
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            out.putShort(newUTF8("Deprecated")).putInt(0);
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && (version & 0xffff) < Opcodes.V1_5) {
            out.putShort(newUTF8("Synthetic")).putInt(0);
        }
        if (innerClasses != null) {
            out.putShort(newUTF8("InnerClasses"));
            out.putInt(innerClasses.length + 2).putShort(innerClassesCount);
            out.putByteArray(innerClasses.data, 0, innerClasses.length);
        }
        if (anns != null) {
            out.putShort(newUTF8("RuntimeVisibleAnnotations"));
            anns.put(out);
        }
        if (ianns != null) {
            out.putShort(newUTF8("RuntimeInvisibleAnnotations"));
            ianns.put(out);
        }
        if (attrs != null) {
            attrs.put(this, null, 0, -1, -1, out);
        }
        if (invalidFrames) {
            ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
            new ClassReader(out.data).accept(cw, ClassReader.SKIP_FRAMES);
            return cw.toByteArray();
        }
        return out.data;
    }

    // ------------------------------------------------------------------------
    // Utility methods: constant pool management
    // ------------------------------------------------------------------------

    /**
     * Adds a number or string constant to the constant pool of the class being
     * build. Does nothing if the constant pool already contains a similar item.
     *
     * @param cst the value of the constant to be added to the constant pool.
     *            This parameter must be an {@link Integer}, a {@link Float}, a
     *            {@link Long}, a {@link Double}, a {@link String} or a
     *            {@link Type}.
     * @return a new or already existing constant item with the given value.
     */
    Item newConstItem(final Object cst) {
        if (cst instanceof Integer) {
            int val = ((Integer) cst).intValue();
            return newInteger(val);
        }
        else if (cst instanceof Byte) {
            int val = ((Byte) cst).intValue();
            return newInteger(val);
        }
        else if (cst instanceof Character) {
            int val = ((Character) cst).charValue();
            return newInteger(val);
        }
        else if (cst instanceof Short) {
            int val = ((Short) cst).intValue();
            return newInteger(val);
        }
        else if (cst instanceof Boolean) {
            int val = ((Boolean) cst).booleanValue() ? 1 : 0;
            return newInteger(val);
        }
        else if (cst instanceof Float) {
            float val = ((Float) cst).floatValue();
            return newFloat(val);
        }
        else if (cst instanceof Long) {
            long val = ((Long) cst).longValue();
            return newLong(val);
        }
        else if (cst instanceof Double) {
            double val = ((Double) cst).doubleValue();
            return newDouble(val);
        }
        else if (cst instanceof String) {
            return newString((String) cst);
        }
        else if (cst instanceof Type) {
            Type t = (Type) cst;
            return newClassItem(t.getSort() == Type.OBJECT
                    ? t.getInternalName()
                    : t.getDescriptor());
        }
        else {
            throw new IllegalArgumentException("value " + cst);
        }
    }

    /**
     * Adds a number or string constant to the constant pool of the class being
     * build. Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     *
     * @param cst the value of the constant to be added to the constant pool.
     *            This parameter must be an {@link Integer}, a {@link Float}, a
     *            {@link Long}, a {@link Double} or a {@link String}.
     * @return the index of a new or already existing constant item with the
     *         given value.
     */
    public int newConst(final Object cst) {
        return newConstItem(cst).index;
    }

    /**
     * Adds an UTF8 string to the constant pool of the class being build. Does
     * nothing if the constant pool already contains a similar item. <i>This
     * method is intended for {@link Attribute} sub classes, and is normally not
     * needed by class generators or adapters.</i>
     *
     * @param value the String value.
     * @return the index of a new or already existing UTF8 item.
     */
    public int newUTF8(final String value) {
        key.set(UTF8, value, null, null);
        Item result = get(key);
        if (result == null) {
            pool.putByte(UTF8).putUTF8(value);
            result = new Item(index++, key);
            put(result);
        }
        return result.index;
    }

    /**
     * Adds a class reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     *
     * @param value the internal name of the class.
     * @return a new or already existing class reference item.
     */
    Item newClassItem(final String value) {
        key2.set(CLASS, value, null, null);
        Item result = get(key2);
        if (result == null) {
            pool.put12(CLASS, newUTF8(value));
            result = new Item(index++, key2);
            put(result);
        }
        return result;
    }

    /**
     * Adds a class reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     *
     * @param value the internal name of the class.
     * @return the index of a new or already existing class reference item.
     */
    public int newClass(final String value) {
        return newClassItem(value).index;
    }

    /**
     * Adds a field reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     *
     * @param owner the internal name of the field's owner class.
     * @param name  the field's name.
     * @param desc  the field's descriptor.
     * @return a new or already existing field reference item.
     */
    Item newFieldItem(final String owner, final String name, final String desc) {
        key3.set(FIELD, owner, name, desc);
        Item result = get(key3);
        if (result == null) {
            put122(FIELD, newClass(owner), newNameType(name, desc));
            result = new Item(index++, key3);
            put(result);
        }
        return result;
    }

    /**
     * Adds a field reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     *
     * @param owner the internal name of the field's owner class.
     * @param name  the field's name.
     * @param desc  the field's descriptor.
     * @return the index of a new or already existing field reference item.
     */
    public int newField(final String owner, final String name, final String desc) {
        return newFieldItem(owner, name, desc).index;
    }

    /**
     * Adds a method reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     *
     * @param owner the internal name of the method's owner class.
     * @param name  the method's name.
     * @param desc  the method's descriptor.
     * @param itf   <tt>true</tt> if <tt>owner</tt> is an interface.
     * @return a new or already existing method reference item.
     */
    Item newMethodItem(
            final String owner,
            final String name,
            final String desc,
            final boolean itf) {
        int type = itf ? IMETH : METH;
        key3.set(type, owner, name, desc);
        Item result = get(key3);
        if (result == null) {
            put122(type, newClass(owner), newNameType(name, desc));
            result = new Item(index++, key3);
            put(result);
        }
        return result;
    }

    /**
     * Adds a method reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     *
     * @param owner the internal name of the method's owner class.
     * @param name  the method's name.
     * @param desc  the method's descriptor.
     * @param itf   <tt>true</tt> if <tt>owner</tt> is an interface.
     * @return the index of a new or already existing method reference item.
     */
    public int newMethod(
            final String owner,
            final String name,
            final String desc,
            final boolean itf) {
        return newMethodItem(owner, name, desc, itf).index;
    }

    /**
     * Adds an integer to the constant pool of the class being build. Does
     * nothing if the constant pool already contains a similar item.
     *
     * @param value the int value.
     * @return a new or already existing int item.
     */
    Item newInteger(final int value) {
        key.set(value);
        Item result = get(key);
        if (result == null) {
            pool.putByte(INT).putInt(value);
            result = new Item(index++, key);
            put(result);
        }
        return result;
    }

    /**
     * Adds a float to the constant pool of the class being build. Does nothing
     * if the constant pool already contains a similar item.
     *
     * @param value the float value.
     * @return a new or already existing float item.
     */
    Item newFloat(final float value) {
        key.set(value);
        Item result = get(key);
        if (result == null) {
            pool.putByte(FLOAT).putInt(key.intVal);
            result = new Item(index++, key);
            put(result);
        }
        return result;
    }

    /**
     * Adds a long to the constant pool of the class being build. Does nothing
     * if the constant pool already contains a similar item.
     *
     * @param value the long value.
     * @return a new or already existing long item.
     */
    Item newLong(final long value) {
        key.set(value);
        Item result = get(key);
        if (result == null) {
            pool.putByte(LONG).putLong(value);
            result = new Item(index, key);
            put(result);
            index += 2;
        }
        return result;
    }

    /**
     * Adds a double to the constant pool of the class being build. Does nothing
     * if the constant pool already contains a similar item.
     *
     * @param value the double value.
     * @return a new or already existing double item.
     */
    Item newDouble(final double value) {
        key.set(value);
        Item result = get(key);
        if (result == null) {
            pool.putByte(DOUBLE).putLong(key.longVal);
            result = new Item(index, key);
            put(result);
            index += 2;
        }
        return result;
    }

    /**
     * Adds a string to the constant pool of the class being build. Does nothing
     * if the constant pool already contains a similar item.
     *
     * @param value the String value.
     * @return a new or already existing string item.
     */
    private Item newString(final String value) {
        key2.set(STR, value, null, null);
        Item result = get(key2);
        if (result == null) {
            pool.put12(STR, newUTF8(value));
            result = new Item(index++, key2);
            put(result);
        }
        return result;
    }

    /**
     * Adds a name and type to the constant pool of the class being build. Does
     * nothing if the constant pool already contains a similar item. <i>This
     * method is intended for {@link Attribute} sub classes, and is normally not
     * needed by class generators or adapters.</i>
     *
     * @param name a name.
     * @param desc a type descriptor.
     * @return the index of a new or already existing name and type item.
     */
    public int newNameType(final String name, final String desc) {
        key2.set(NAME_TYPE, name, desc, null);
        Item result = get(key2);
        if (result == null) {
            put122(NAME_TYPE, newUTF8(name), newUTF8(desc));
            result = new Item(index++, key2);
            put(result);
        }
        return result.index;
    }

    /**
     * Adds the given internal name to {@link #typeTable} and returns its index.
     * Does nothing if the type table already contains this internal name.
     *
     * @param type the internal name to be added to the type table.
     * @return the index of this internal name in the type table.
     */
    int addType(final String type) {
        key.set(TYPE_NORMAL, type, null, null);
        Item result = get(key);
        if (result == null) {
            result = addType(key);
        }
        return result.index;
    }

    /**
     * Adds the given "uninitialized" type to {@link #typeTable} and returns its
     * index. This method is used for UNINITIALIZED types, made of an internal
     * name and a bytecode offset.
     *
     * @param type   the internal name to be added to the type table.
     * @param offset the bytecode offset of the NEW instruction that created
     *               this UNINITIALIZED type value.
     * @return the index of this internal name in the type table.
     */
    int addUninitializedType(final String type, final int offset) {
        key.type = TYPE_UNINIT;
        key.intVal = offset;
        key.strVal1 = type;
        key.hashCode = 0x7FFFFFFF & (TYPE_UNINIT + type.hashCode() + offset);
        Item result = get(key);
        if (result == null) {
            result = addType(key);
        }
        return result.index;
    }

    /**
     * Adds the given Item to {@link #typeTable}.
     *
     * @param item the value to be added to the type table.
     * @return the added Item, which a new Item instance with the same value as
     *         the given Item.
     */
    private Item addType(final Item item) {
        ++typeCount;
        Item result = new Item(typeCount, key);
        put(result);
        if (typeTable == null) {
            typeTable = new Item[16];
        }
        if (typeCount == typeTable.length) {
            Item[] newTable = new Item[2 * typeTable.length];
            System.arraycopy(typeTable, 0, newTable, 0, typeTable.length);
            typeTable = newTable;
        }
        typeTable[typeCount] = result;
        return result;
    }

    /**
     * Returns the index of the common super type of the two given types. This
     * method calls {@link #getCommonSuperClass} and caches the result in the
     * {@link #items} hash table to speedup future calls with the same
     * parameters.
     *
     * @param type1 index of an internal name in {@link #typeTable}.
     * @param type2 index of an internal name in {@link #typeTable}.
     * @return the index of the common super type of the two given types.
     */
    int getMergedType(final int type1, final int type2) {
        key2.type = TYPE_MERGED;
        key2.longVal = type1 | (((long) type2) << 32);
        key2.hashCode = 0x7FFFFFFF & (TYPE_MERGED + type1 + type2);
        Item result = get(key2);
        if (result == null) {
            String t = typeTable[type1].strVal1;
            String u = typeTable[type2].strVal1;
            key2.intVal = addType(getCommonSuperClass(t, u));
            result = new Item((short) 0, key2);
            put(result);
        }
        return result.intVal;
    }

    /**
     * Returns the common super type of the two given types. The default
     * implementation of this method <i>loads<i> the two given classes and uses
     * the java.lang.Class methods to find the common super class. It can be
     * overriden to compute this common super type in other ways, in particular
     * without actually loading any class, or to take into account the class
     * that is currently being generated by this ClassWriter, which can of
     * course not be loaded since it is under construction.
     *
     * @param type1 the internal name of a class.
     * @param type2 the internal name of another class.
     * @return the internal name of the common super class of the two given
     *         classes.
     */
    protected String getCommonSuperClass(final String type1, final String type2) {
        Class c, d;
        try {
            c = Thread.currentThread().getContextClassLoader().loadClass(type1.replace('/', '.'));
            d = Thread.currentThread().getContextClassLoader().loadClass(type2.replace('/', '.'));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        }
        else {
            do {
                c = c.getSuperclass();
            }
            while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }

    /**
     * Returns the constant pool's hash table item which is equal to the given
     * item.
     *
     * @param key a constant pool item.
     * @return the constant pool's hash table item which is equal to the given
     *         item, or <tt>null</tt> if there is no such item.
     */
    private Item get(final Item key) {
        Item i = items[key.hashCode % items.length];
        while (i != null && !key.isEqualTo(i)) {
            i = i.next;
        }
        return i;
    }

    /**
     * Puts the given item in the constant pool's hash table. The hash table
     * <i>must</i> not already contains this item.
     *
     * @param i the item to be added to the constant pool's hash table.
     */
    private void put(final Item i) {
        if (index > threshold) {
            int ll = items.length;
            int nl = ll * 2 + 1;
            Item[] newItems = new Item[nl];
            for (int l = ll - 1; l >= 0; --l) {
                Item j = items[l];
                while (j != null) {
                    int index = j.hashCode % newItems.length;
                    Item k = j.next;
                    j.next = newItems[index];
                    newItems[index] = j;
                    j = k;
                }
            }
            items = newItems;
            threshold = (int) (nl * 0.75);
        }
        int index = i.hashCode % items.length;
        i.next = items[index];
        items[index] = i;
    }

    /**
     * Puts one byte and two shorts into the constant pool.
     *
     * @param b  a byte.
     * @param s1 a short.
     * @param s2 another short.
     */
    private void put122(final int b, final int s1, final int s2) {
        pool.put12(b, s1).putShort(s2);
    }
}
