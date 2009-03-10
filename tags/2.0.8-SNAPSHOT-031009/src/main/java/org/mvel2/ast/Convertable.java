package org.mvel2.ast;

import org.mvel2.CompileException;
import org.mvel2.DataConversion;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.util.CompilerTools;

public class Convertable extends ASTNode {
    private ASTNode stmt;
    private ASTNode clsStmt;

    public Convertable(ASTNode stmt, ASTNode clsStmt) {
        this.stmt = stmt;
        this.clsStmt = clsStmt;
        CompilerTools.expectType(clsStmt, Class.class, true);
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        Object o = stmt.getReducedValueAccelerated(ctx, thisValue, factory);
        return o != null && DataConversion.canConvert(
                (Class) clsStmt.getReducedValueAccelerated(ctx, thisValue, factory), o.getClass());

    }

    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        try {

            Object o =  stmt.getReducedValue(ctx, thisValue, factory);
            if (o == null) return false;

            Class i = (Class) clsStmt.getReducedValue(ctx, thisValue, factory);
            if (i == null) throw new ClassCastException();


            return DataConversion.canConvert(i,o.getClass());
        }
        catch (ClassCastException e) {
            throw new CompileException("not a class reference: " + clsStmt.getName());
        }

    }

    public Class getEgressType() {
        return Boolean.class;
    }
}
