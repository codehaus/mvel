
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
package org.mvel.optimizers;

import org.mvel.CompileException;
import org.mvel.optimizers.impl.asm.ASMAccessorOptimizer;
import org.mvel.optimizers.impl.asm.ASMOptimizer;
import org.mvel.optimizers.impl.refl.ReflectiveOptimizer;

import java.util.HashMap;
import java.util.Map;

public class OptimizerFactory {
    public static String SAFE_REFLECTIVE = "Reflective";

    private static String defaultOptimizer;
    private static final Map<String, Optimizer> optimizers = new HashMap<String, Optimizer>();
    private static final Map<String, AccessorOptimizer> accessorCompilers = new HashMap<String, AccessorOptimizer>();

    static {
        accessorCompilers.put(SAFE_REFLECTIVE, new ReflectiveOptimizer());
        /**
         * By default, activate the JIT if ASM is present in the classpath
         */
        try {
            Class.forName("org.objectweb.asm.ClassWriter");

            optimizers.put(defaultOptimizer = "ASM", new ASMOptimizer());
            accessorCompilers.put("ASM", new ASMAccessorOptimizer());
        }

        catch (ClassNotFoundException e) {
            defaultOptimizer = SAFE_REFLECTIVE;
        }
        catch (Throwable e) {
            e.printStackTrace();
            System.err.println("[MVEL] Notice: Possible incorrect version of ASM present (3.0 required).  " +
                    "Disabling JIT compiler.  Reflective Optimizer will be used.");
            defaultOptimizer = SAFE_REFLECTIVE;
        }

        if (Boolean.getBoolean("mvel.disable.jit"))
            defaultOptimizer = SAFE_REFLECTIVE;
    }

    public static void registerOptimizer(Optimizer optimizer) {
        optimizers.put(optimizer.getName(), optimizer);
    }

    public static Optimizer getDefaultOptimizer() {
        return optimizers.get(defaultOptimizer);
    }

    public static AccessorOptimizer getDefaultAccessorCompiler() {
        try {
            return accessorCompilers.get(defaultOptimizer).getClass().newInstance();
        }
        catch (Exception e) {
            throw new CompileException("unable to instantiate accessor compiler", e);
        }
    }

    public static AccessorOptimizer getAccessorCompiler(String name) {
        try {
            return accessorCompilers.get(name).getClass().newInstance();
        }
        catch (Exception e) {
            throw new CompileException("unable to instantiate accessor compiler", e);
        }
    }
}
