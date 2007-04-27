package org.mvel.debug;

import org.mvel.*;
import static org.mvel.Operator.ADD;
import static org.mvel.Operator.SUB;

import java.io.Serializable;

/**
 * @author Christopher Brock
 */
public class DebugTools {

    public static String decompile(Serializable expr) {
        if (expr instanceof CompiledExpression) return decompile((CompiledExpression) expr);
        else if (expr instanceof ExecutableAccessor) return "CANNOT DECOMPILE OPTIMIZED STATEMENT";

        else return "NOT A KNOWN PAYLOAD";
    }

    public static String decompile(CompiledExpression cExp) {
        TokenIterator iter = cExp.getTokens();
        Token tk;

        int node = 0;

        StringBuffer sbuf = new StringBuffer("Expression Decompile\n-------------\n");

        while (iter.hasMoreTokens()) {
            tk = iter.nextToken();

            sbuf.append("(").append(node++).append(") ");

            if (tk.isLiteral()) {
                sbuf.append("PUSH LITERAL '").append(tk.getLiteralValue()).append("'");
            }
            else if (tk.isOperator()) {
                sbuf.append("OPERATOR [").append(getOperatorName(tk.getOperator())).append("]: ")
                        .append(tk.getName());
            }
            else if (tk.isIdentifier()) {
                sbuf.append("PUSH VAR :: " + tk.getName());
            }

            sbuf.append("\n");

        }

        sbuf.append("==END==");

        return sbuf.toString();
    }


    public static String getOperatorName(int operator) {
        switch (operator) {
            case ADD:
                return "ADD";
            case SUB:
                return "SUBTRACT";
            case Operator.ASSIGN:
                return "ASSIGN";
            case Operator.ASSIGN_ADD:
                return "ASSIGN_ADD";
            case Operator.ASSIGN_STR_APPEND:
                return "ASSIGN_STR_APPEND";
            case Operator.ASSIGN_SUB:
                return "ASSIGN_SUB";
            case Operator.BW_AND:
                return "BIT_AND";
            case Operator.BW_OR:
                return "BIT_OR";
            case Operator.BW_SHIFT_LEFT:
                return "BIT_SHIFT_LEFT";
            case Operator.BW_SHIFT_RIGHT:
                return "BIT_SHIFT_RIGHT";
            case Operator.BW_USHIFT_LEFT:
                return "BIT_UNSIGNED_SHIFT_LEFT";
            case Operator.BW_USHIFT_RIGHT:
                return "BIT_UNSIGNED_SHIFT_RIGHT";
            case Operator.BW_XOR:
                return "BIT_XOR";
            case Operator.CONTAINS:
                return "CONTAINS";
            case Operator.CONVERTABLE_TO:
                return "CONVERTABLE_TO";
            case Operator.DEC:
                return "DECREMENT";
            case Operator.DEC_ASSIGN:
                return "DECREMENT_ASSIGN";
            case Operator.DIV:
                return "DIVIDE";
            case Operator.DO:
                return "DO";
            case Operator.ELSE:
                return "ELSE";
            case Operator.END_OF_STMT:
                return "END_OF_STATEMENT";
            case Operator.EQUAL:
                return "EQUAL";
            case Operator.FOR:
                return "FOR";
            case Operator.FOREACH:
                return "FOREACH";
            case Operator.FUNCTION:
                return "FUNCTION";
            case Operator.GETHAN:
                return "GREATER_THAN_OR_EQUAL";
            case Operator.GTHAN:
                return "GREATHER_THAN";
            case Operator.IF:
                return "IF";
            case Operator.INC:
                return "INCREMENT";
            case Operator.INC_ASSIGN:
                return "INCREMENT_ASSIGN";
            case Operator.INSTANCEOF:
                return "INSTANCEOF";
            case Operator.LETHAN:
                return "LESS_THAN_OR_EQUAL";
            case Operator.LTHAN:
                return "LESS_THAN";
            case Operator.MOD:
                return "MODULUS";
            case Operator.MULT:
                return "MULTIPLY";
            case Operator.NEQUAL:
                return "NOT_EQUAL";
            case Operator.NEW:
                return "NEW_OBJECT";
            case Operator.OR:
                return "OR";
            case Operator.POWER:
                return "POWER_OF";
            case Operator.PROJECTION:
                return "PROJECT";
            case Operator.REGEX:
                return "REGEX";
            case Operator.RETURN:
                return "RETURN";
            case Operator.SIMILARITY:
                return "SIMILARITY";
            case Operator.SOUNDEX:
                return "SOUNDEX";
            case Operator.STR_APPEND:
                return "STR_APPEND";
            case Operator.SWITCH:
                return "SWITCH";
            case Operator.TERNARY:
                return "TERNARY_IF";
            case Operator.TERNARY_ELSE:
                return "TERNARY_ELSE";
            case Operator.WHILE:
                return "WHILE";
            case Operator.CHOR:
                return "CHAINED_OR";
        }


        return "UNKNOWN_OPERATOR";
    }
}
