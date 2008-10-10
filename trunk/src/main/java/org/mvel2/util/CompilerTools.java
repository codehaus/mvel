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
package org.mvel2.util;

import org.mvel2.CompileException;
import org.mvel2.Operator;
import static org.mvel2.Operator.PTABLE;
import org.mvel2.ParserContext;
import org.mvel2.ast.*;
import static org.mvel2.compiler.AbstractParser.getCurrentThreadParserContext;
import org.mvel2.compiler.Accessor;
import org.mvel2.compiler.CompiledExpression;

import java.util.LinkedHashMap;
import java.util.Map;

public class CompilerTools {
    /**
     * Optimize the AST, by reducing any stack-based-operations to dedicated nodes where possible.
     *
     * @param astLinkedList          - AST to be optimized.
     * @param secondPassOptimization - perform a second pass optimization to optimize boolean expressions.
     * @param ctx                    - The parser context
     * @return optimized AST
     */
    public static ASTLinkedList optimizeAST(ASTLinkedList astLinkedList, boolean secondPassOptimization, ParserContext ctx) {
        ASTLinkedList optimizedAst = new ASTLinkedList();
        ASTNode tk, tkOp, tkOp2;

        /**
         * Re-process the AST and optimize it.
         */
        while (astLinkedList.hasMoreNodes()) {
            if ((tk = astLinkedList.nextNode()).getFields() == -1) {
                optimizedAst.addTokenNode(tk);
            }
            else if (astLinkedList.hasMoreNodes()) {
                if ((tkOp = astLinkedList.nextNode()).getFields() == -1) {
                    optimizedAst.addTokenNode(tk, tkOp);
                }
                else if (tkOp.isOperator() && tkOp.getOperator() < 21) {
                    int op;
                    int op2;

                    BinaryOperation bo = new BinaryOperation(op = tkOp.getOperator(), tk, astLinkedList.nextNode(), ctx);

                    tkOp2 = null;

                    /**
                     * If we have a chain of math/comparitive operators then we fill them into the tree
                     * right here.
                     */
                    while (astLinkedList.hasMoreNodes() && (tkOp2 = astLinkedList.nextNode()).isOperator()
                            && tkOp2.getFields() != -1 && (op2 = tkOp2.getOperator()) < 21) {

                        if (PTABLE[op2] > PTABLE[op]) {
                            bo.setRightMost(new BinaryOperation(op2, bo.getRightMost(), astLinkedList.nextNode(), ctx));
                        }
                        else if (bo.getOperation() != op2 && PTABLE[op] == PTABLE[op2]) {
                            if (PTABLE[bo.getOperation()] == PTABLE[op2]) {
                                bo = new BinaryOperation(op2, bo, astLinkedList.nextNode(), ctx);
                            }
                            else {
                                bo.setRight(new BinaryOperation(op2, bo.getRight(), astLinkedList.nextNode(), ctx));
                            }
                        }
                        else if (PTABLE[bo.getOperation()] >= PTABLE[op2]) {
                            bo = new BinaryOperation(op2, bo, astLinkedList.nextNode(), ctx);
                        }
                        else {
                            bo.setRight(new BinaryOperation(op2, bo.getRight(), astLinkedList.nextNode(), ctx));
                        }

                        op = op2;
                        tkOp = tkOp2;
                    }

                    optimizedAst.addTokenNode(bo);

                    if (tkOp2 != null && tkOp2 != tkOp) {
                        optimizedAst.addTokenNode(tkOp2);
                    }
                }
                else if (!tkOp.isOperator() && tk.getLiteralValue() instanceof Class) {
                    optimizedAst.addTokenNode(new DeclTypedVarNode(tkOp.getName(), (Class) tk.getLiteralValue(), 0));
                }
                else if (tkOp.isOperator(Operator.REGEX)) {
                    optimizedAst.addTokenNode(new RegExMatchNode(tk, astLinkedList.nextNode()));
                }
                else if (tkOp.isOperator(Operator.CONTAINS)) {
                    optimizedAst.addTokenNode(new Contains(tk, astLinkedList.nextNode()));
                }
                else {
                    optimizedAst.addTokenNode(tk, tkOp);
                }
            }
            else {
                optimizedAst.addTokenNode(tk);
            }
        }

        if (secondPassOptimization) {
            /**
             * Perform a second pass optimization for boolean conditions.
             */
            (astLinkedList = optimizedAst).reset();
            optimizedAst = new ASTLinkedList();

            while (astLinkedList.hasMoreNodes()) {
                if ((tk = astLinkedList.nextNode()).getFields() == -1) {
                    optimizedAst.addTokenNode(tk);
                }
                else if (astLinkedList.hasMoreNodes()) {
                    if ((tkOp = astLinkedList.nextNode()).getFields() == -1) {
                        optimizedAst.addTokenNode(tk, tkOp);
                    }
                    else if (tkOp.isOperator()
                            && (tkOp.getOperator() == Operator.AND || tkOp.getOperator() == Operator.OR)) {

                        tkOp2 = null;
                        ASTNode bool = null;

                        switch (tkOp.getOperator()) {
                            case Operator.AND:
                                bool = new And(tk, astLinkedList.nextNode(), ctx.isStrongTyping());
                                break;
                            case Operator.OR:
                                bool = new Or(tk, astLinkedList.nextNode(), ctx.isStrongTyping());
                        }

                        while (astLinkedList.hasMoreNodes() && (tkOp2 = astLinkedList.nextNode()).isOperator()
                                && (tkOp2.isOperator(Operator.AND) || tkOp2.isOperator(Operator.OR))) {

                            switch ((tkOp = tkOp2).getOperator()) {
                                case Operator.AND:
                                    bool = new And(bool, astLinkedList.nextNode(), ctx.isStrongTyping());
                                    break;
                                case Operator.OR:
                                    bool = new Or(bool, astLinkedList.nextNode(), ctx.isStrongTyping());
                            }
                        }

                        optimizedAst.addTokenNode(bool);

                        if (tkOp2 != null && tkOp2 != tkOp) {
                            optimizedAst.addTokenNode(tkOp2);
                        }
                    }
                    else {
                        optimizedAst.addTokenNode(tk, tkOp);
                    }
                }
                else {
                    optimizedAst.addTokenNode(tk);
                }
            }
        }

        return optimizedAst;
    }

    /**
     * Returns an ordered Map of all functions declared within an compiled script.
     *
     * @param compile
     * @return - ordered Map
     */
    public static Map<String, Function> extractAllDeclaredFunctions(CompiledExpression compile) {
        Map<String, Function> allFunctions = new LinkedHashMap<String, Function>();
        ASTIterator instructions = new ASTLinkedList(compile.getInstructions());

        ASTNode n;
        while (instructions.hasMoreNodes()) {
            if ((n = instructions.nextNode()) instanceof Function) {
                allFunctions.put(n.getName(), (Function) n);
            }
        }

        return allFunctions;
    }

    public static void expectType(Accessor expression, Class type, boolean compileMode) {
        Class retType = expression.getKnownEgressType();
        if (compileMode && getCurrentThreadParserContext().isStrictTypeEnforcement()) {
            if (retType == null || !type.isAssignableFrom(retType)) {
                throw new CompileException("was expecting type: " + type.getName() + "; but found type: "
                        + (retType != null ? retType.getName() : "null"));
            }
        }
        else if (retType == null || !Object.class.equals(retType) && !type.isAssignableFrom(retType)) {
            throw new CompileException("was expecting type: " + type.getName() + "; but found type: "
                    + (retType != null ? retType.getName() : "null"));
        }
    }

    public static void expectType(ASTNode node, Class type, boolean compileMode) {
        Class retType = node.getEgressType();
        if (compileMode && getCurrentThreadParserContext().isStrictTypeEnforcement()) {
            if (retType == null || !type.isAssignableFrom(retType)) {
                throw new CompileException("was expecting type: " + type.getName() + "; but found type: "
                        + (retType != null ? retType.getName() : "null"));
            }
        }
        else if (retType == null || !Object.class.equals(retType) && !type.isAssignableFrom(retType)) {
            throw new CompileException("was expecting type: " + type.getName() + "; but found type: "
                    + (retType != null ? retType.getName() : "null"));
        }
    }

    public static Class getReturnTypeFromOp(int operation, Class left, Class right) {
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
                return Boolean.class;

            case Operator.ADD:
            case Operator.SUB:
            case Operator.MULT:
            case Operator.DIV:
            case Operator.POWER:
            case Operator.MOD:
                return left;

            case Operator.BW_AND:
            case Operator.BW_OR:
            case Operator.BW_XOR:
            case Operator.BW_SHIFT_RIGHT:
            case Operator.BW_SHIFT_LEFT:
            case Operator.BW_USHIFT_LEFT:
            case Operator.BW_USHIFT_RIGHT:
            case Operator.BW_NOT:
                return Integer.class;

            case Operator.STR_APPEND:
                return String.class;

            default:
                throw new RuntimeException("unknown type: " + operation);
        }
    }


}
