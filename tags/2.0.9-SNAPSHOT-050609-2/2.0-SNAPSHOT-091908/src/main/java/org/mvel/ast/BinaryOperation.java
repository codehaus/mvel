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
package org.mvel.ast;

import org.mvel.CompileException;
import org.mvel.Operator;
import static org.mvel.Operator.PTABLE;
import org.mvel.ParserContext;
import org.mvel.debug.DebugTools;
import static org.mvel.debug.DebugTools.getOperatorSymbol;
import org.mvel.integration.VariableResolverFactory;
import static org.mvel.util.ParseTools.doOperations;

public class BinaryOperation extends ASTNode {
    private final int operation;
    private ASTNode left;
    private ASTNode right;

    public BinaryOperation(int operation, ASTNode left, ASTNode right) {
        this.operation = operation;
        this.left = left;
        this.right = right;

        setReturnType();
    }

    public BinaryOperation(int operation, ASTNode left, ASTNode right, ParserContext ctx) {
        this.operation = operation;
        this.left = left;
        this.right = right;

        if (ctx.isStrongTyping()) {
            switch (operation) {
                case Operator.ADD:
                    if (left.getEgressType() == String.class || right.getEgressType() == String.class) {
                        break;
                    }

                default:
                    if (!left.getEgressType().isAssignableFrom(right.getEgressType())) {
                        throw new CompileException("incompatible types in statement: " + right.getEgressType() + " (assignment from: " + left.getEgressType() + ")");
                    }
            }
        }

        setReturnType();
    }

    private void setReturnType() {
        switch (operation) {
            case Operator.LETHAN:
            case Operator.LTHAN:
            case Operator.GETHAN:
            case Operator.GTHAN:
            case Operator.EQUAL:
            case Operator.NEQUAL:
            case Operator.AND:
            case Operator.OR:
            case Operator.CONTAINS:
            case Operator.CONVERTABLE_TO:
                egressType = Boolean.class;
                break;

            case Operator.ADD:
            case Operator.SUB:
            case Operator.MULT:
            case Operator.DIV:
            case Operator.POWER:
                boolean foo = !!true;

                egressType = bestFitType(left.egressType, right.egressType);
                break;
                
            case Operator.BW_AND:
            case Operator.BW_OR:
            case Operator.BW_XOR:
            case Operator.BW_SHIFT_RIGHT:
            case Operator.BW_SHIFT_LEFT:
            case Operator.BW_USHIFT_LEFT:
            case Operator.BW_USHIFT_RIGHT:
            case Operator.BW_NOT:
                egressType = Integer.class;
                break;

            case Operator.STR_APPEND:
                egressType = String.class;
                break;

            default:
                throw new RuntimeException("unknown type: " + operation);
        }
    }

    private static Class bestFitType(Class a, Class b) {
        return a;
    }


    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        return doOperations(left.getReducedValueAccelerated(ctx, thisValue, factory), operation, right.getReducedValueAccelerated(ctx, thisValue, factory));
    }


    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        throw new RuntimeException("unsupported AST operation");
    }

    public int getOperation() {
        return operation;
    }

    //  public void setOperation(int operation) {
    //     assert operation != -1;
    //      this.operation = operation;
    //  }

    public ASTNode getLeft() {
        return left;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
    }

    public ASTNode getRight() {
        return right;
    }

    public ASTNode getRightMost() {
        BinaryOperation n = this;
        while (n.right != null && n.right instanceof BinaryOperation) {
            n = (BinaryOperation) n.right;
        }
        return n.right;
    }

    public BinaryOperation getRightBinary() {
        return right != null && right instanceof BinaryOperation ? (BinaryOperation) right : null;
    }

    public void setRight(ASTNode right) {
        this.right = right;
    }

    public void setRightMost(ASTNode right) {
        BinaryOperation n = this;
        while (n.right != null && n.right instanceof BinaryOperation) {
            n = (BinaryOperation) n.right;
        }
        n.right = right;
    }

    public int getPrecedence() {
        return PTABLE[operation];
    }

    public boolean isGreaterPrecedence(BinaryOperation o) {
        return o.getPrecedence() > PTABLE[operation];
    }

    public String toString() {
        return "(" + left + " " + getOperatorSymbol(operation) + " " + right + ")";
    }
}