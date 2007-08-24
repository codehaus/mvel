package org.mvel.util;

import org.mvel.ASTLinkedList;
import org.mvel.ASTNode;
import org.mvel.Operator;
import org.mvel.ast.And;
import org.mvel.ast.BinaryOperation;
import org.mvel.ast.EndOfStatement;
import org.mvel.ast.Or;

public class CompilerTools {


    /**
     * Optimize the AST, by reducing any stack-based-operations to dedicated nodes where possible.
     *
     * @param astLinkedList - AST to be optimized.
     * @param secondPassOptimization - perform a second pass optimization to optimize boolean expressions.
     * @return optimized AST
     */
    public static ASTLinkedList optimizeAST(ASTLinkedList astLinkedList, boolean secondPassOptimization) {
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
                    optimizedAst.addTokenNode(tk);
                    if (tk instanceof EndOfStatement) {
                        astLinkedList.setCurrentNode(tkOp);
                        continue;
                    }

                    optimizedAst.addTokenNode(tkOp);
                }
                else if (tkOp.isOperator() && tkOp.getOperator() < 12) {
                    // handle math and equals
                    BinaryOperation bo = new BinaryOperation(tkOp.getOperator(), tk, astLinkedList.nextNode());
                    tkOp2 = null;

                    /**
                     * If we have a chain of math/comparitive operators then we fill them into the tree
                     * right here.
                     */
                    while (astLinkedList.hasMoreNodes() && (tkOp2 = astLinkedList.nextNode()).isOperator()
                            && tkOp2.getFields() != -1 && tkOp2.getOperator() < 12) {
                        bo = new BinaryOperation(((tkOp = tkOp2).getOperator()), bo, astLinkedList.nextNode());
                    }
                    optimizedAst.addTokenNode(bo);

                    if (tkOp2 != null && tkOp2 != tkOp) {
                        optimizedAst.addTokenNode(tkOp2);
                    }
                }

                else {
                    optimizedAst.addTokenNode(tk);
                    if (tk instanceof EndOfStatement) {
                        astLinkedList.setCurrentNode(tkOp);
                        continue;
                    }

                    optimizedAst.addTokenNode(tkOp);
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
            astLinkedList = optimizedAst;
            astLinkedList.reset();

            optimizedAst = new ASTLinkedList();

            while (astLinkedList.hasMoreNodes()) {
                if ((tk = astLinkedList.nextNode()).getFields() == -1) {
                    optimizedAst.addTokenNode(tk);
                }
                else if (astLinkedList.hasMoreNodes()) {
                    if ((tkOp = astLinkedList.nextNode()).getFields() == -1) {
                        optimizedAst.addTokenNode(tk);
                        if (tk instanceof EndOfStatement) {
                            astLinkedList.setCurrentNode(tkOp);
                        }

                        optimizedAst.addTokenNode(tkOp);
                    }
                    else if (tkOp.isOperator()
                            && (tkOp.getOperator() == Operator.AND || tkOp.getOperator() == Operator.OR)) {

                        tkOp2 = null;
                        ASTNode bool = null;

                        switch (tkOp.getOperator()) {
                            case Operator.AND:
                                bool = new And(tk, astLinkedList.nextNode());
                                break;
                            case Operator.OR:
                                bool = new Or(tk, astLinkedList.nextNode());
                        }

                        /**
                         * If we have a chain of math/comparitive operators then we fill them into the tree
                         * right here.
                         */
                        while (astLinkedList.hasMoreNodes() && (tkOp2 = astLinkedList.nextNode()).isOperator()
                                && (tkOp2.isOperator(Operator.AND) || tkOp2.isOperator(Operator.OR))) {

                            switch ((tkOp = tkOp2).getOperator()) {
                                case Operator.AND:
                                    bool = new And(bool, astLinkedList.nextNode());
                                    break;
                                case Operator.OR:
                                    bool = new Or(bool, astLinkedList.nextNode());
                            }
                        }

                        optimizedAst.addTokenNode(bool);

                        if (tkOp2 != null && tkOp2 != tkOp) {
                            optimizedAst.addTokenNode(tkOp2);
                        }
                    }
                    else {
                        optimizedAst.addTokenNode(tk);
                        if (tk instanceof EndOfStatement) {
                            astLinkedList.setCurrentNode(tkOp);
                        }

                        optimizedAst.addTokenNode(tkOp);
                    }
                }
                else {
                    optimizedAst.addTokenNode(tk);
                }
            }
        }

        return optimizedAst;
    }

}
