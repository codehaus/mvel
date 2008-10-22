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
package org.mvel2.math;

import org.mvel2.CompileException;
import org.mvel2.ConversionException;
import static org.mvel2.DataConversion.convert;
import org.mvel2.DataTypes;
import static org.mvel2.DataTypes.BIG_DECIMAL;
import static org.mvel2.DataTypes.EMPTY;
import static org.mvel2.Operator.*;
import static org.mvel2.Soundex.soundex;
import org.mvel2.Unit;
import org.mvel2.debug.DebugTools;
import org.mvel2.util.InternalNumber;
import static org.mvel2.util.ParseTools.*;

import static java.lang.String.valueOf;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * @author Christopher Brock
 */
public strictfp class MathProcessor {
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    public static Object doOperations(Object val1, int operation, Object val2) {
        int type1 = val1 == null ? DataTypes.NULL : __resolveType(val1.getClass());
        int type2 = val2 == null ? DataTypes.NULL : __resolveType(val2.getClass());

        switch (type1) {
            case BIG_DECIMAL:
                switch (type2) {
                    case BIG_DECIMAL:
                        return doBigDecimalArithmetic((BigDecimal) val1, operation, (BigDecimal) val2, false);
                    default:
                        if (type2 > 99) {
                            return doBigDecimalArithmetic((BigDecimal) val1, operation, getInternalNumberFromType(val2, type2), false);
                        }
                        else {
                            return _doOperations(type1, val1, operation, type2, val2);
                        }
                }
            default:
                return _doOperations(type1, val1, operation, type2, val2);

        }
    }

    private static Object doBigDecimalArithmetic(final BigDecimal val1, final int operation, final BigDecimal val2, boolean iNumber) {
        switch (operation) {
            case ADD:
                if (iNumber) {
                    return narrowType(val1.add(val2, MATH_CONTEXT));
                }
                else {
                    return val1.add(val2, MATH_CONTEXT);
                }
            case DIV:
                if (iNumber) {
                    return narrowType(val1.divide(val2, MATH_CONTEXT));
                }
                else {
                    return val1.divide(val2, MATH_CONTEXT);
                }

            case SUB:
                if (iNumber) {
                    return narrowType(val1.subtract(val2, MATH_CONTEXT));
                }
                else {
                    return val1.subtract(val2, MATH_CONTEXT);
                }
            case MULT:
                if (iNumber) {
                    return narrowType(val1.multiply(val2, MATH_CONTEXT));
                }
                else {
                    return val1.multiply(val2, MATH_CONTEXT);
                }

            case POWER:
                if (iNumber) {
                    return narrowType(val1.pow(val2.intValue(), MATH_CONTEXT));
                }
                else {
                    return val1.pow(val2.intValue(), MATH_CONTEXT);
                }

            case MOD:
                if (iNumber) {
                    return narrowType(val1.remainder(val2));
                }
                else {
                    return val1.remainder(val2);
                }

            case GTHAN:
                return val1.compareTo(val2) == 1 ? Boolean.TRUE : Boolean.FALSE;
            case GETHAN:
                return val1.compareTo(val2) >= 0 ? Boolean.TRUE : Boolean.FALSE;
            case LTHAN:
                return val1.compareTo(val2) == -1 ? Boolean.TRUE : Boolean.FALSE;
            case LETHAN:
                return val1.compareTo(val2) <= 0 ? Boolean.TRUE : Boolean.FALSE;
            case EQUAL:
                return val1.compareTo(val2) == 0 ? Boolean.TRUE : Boolean.FALSE;
            case NEQUAL:
                return val1.compareTo(val2) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }

    private static Object _doOperations(int type1, Object val1, int operation, int type2, Object val2) {
        if (operation < 20) {
            if (type1 > 99 && type1 == type2) {
                return doOperationsSameType(type1, val1, operation, val2);
            }
            else if ((type1 > 99 && (type2 > 99))
                    || (operation != 0 && isNumber(val1) && isNumber(val2))) {
                return doBigDecimalArithmetic(getInternalNumberFromType(val1, type1),
                        operation,
                        getInternalNumberFromType(val2, type2), true);
            }
            else if (operation != ADD &&
                    (type1 == 15 || type2 == 15) &&
                    type1 != type2 && type1 != EMPTY && type2 != EMPTY) {

                return doOperationNonNumeric(convert(val1, Boolean.class), operation, convert(val2, Boolean.class));
            }
            // Fix for: MVEL-56
            else if ((type1 == 1 || type2 == 1) && (type1 == 8 || type1 == 112 || type2 == 8 || type2 == 112)) {
                if (type1 == 1) {
                    return doOperationNonNumeric(val1, operation, valueOf(val2));
                }
                else {
                    return doOperationNonNumeric(valueOf(val1), operation, val2);
                }
            }
        }
        return doOperationNonNumeric(val1, operation, val2);
    }

    private static Object doOperationNonNumeric(final Object val1, final int operation, final Object val2) {
        switch (operation) {
            case ADD:
                return valueOf(val1) + valueOf(val2);

            case EQUAL:
                return safeEquals(val2, val1) ? Boolean.TRUE : Boolean.FALSE;

            case NEQUAL:
                return safeNotEquals(val2, val1) ? Boolean.TRUE : Boolean.FALSE;

            case SUB:
            case DIV:
            case MULT:
            case MOD:
            case GTHAN:
                if (val1 instanceof Comparable) {
                    return val2 != null && (((Comparable) val1).compareTo(val2) >= 1 ? Boolean.TRUE : Boolean.FALSE);
                }
                else {
                    return Boolean.FALSE;
                }
                //     break;

            case GETHAN:
                if (val1 instanceof Comparable) {
                    //noinspection unchecked
                    return val2 != null && ((Comparable) val1).compareTo(val2) >= 0 ? Boolean.TRUE : Boolean.FALSE;
                }
                else {
                    return Boolean.FALSE;
                }


            case LTHAN:
                if (val1 instanceof Comparable) {
                    //noinspection unchecked
                    return val2 != null && ((Comparable) val1).compareTo(val2) <= -1 ? Boolean.TRUE : Boolean.FALSE;
                }
                else {
                    return Boolean.FALSE;
                }


            case LETHAN:
                if (val1 instanceof Comparable) {
                    //noinspection unchecked
                    return val2 != null && ((Comparable) val1).compareTo(val2) <= 0 ? Boolean.TRUE : Boolean.FALSE;
                }
                else {
                    return Boolean.FALSE;
                }


            case SOUNDEX:
                return soundex(String.valueOf(val1)).equals(soundex(String.valueOf(val2)));

            case STR_APPEND:
                return valueOf(val1) + valueOf(val2);
        }

        throw new CompileException("could not perform numeric operation on non-numeric types: left-type="
                + (val1 != null ? val1.getClass().getName() : "null") + "; right-type="
                + (val2 != null ? val2.getClass().getName() : "null")
                + " [vals (" + valueOf(val1) + ", " + valueOf(val2) + ") operation=" + DebugTools.getOperatorName(operation) + " (opcode:" + operation + ") ]");

    }

    private static Boolean safeEquals(final Object val1, final Object val2) {
        if (val1 != null) {
            return val1.equals(val2) ? Boolean.TRUE : Boolean.FALSE;
        }
        else return val2 == null || (val2.equals(val1) ? Boolean.TRUE : Boolean.FALSE);
    }

    private static Boolean safeNotEquals(final Object val1, final Object val2) {
        if (val1 != null) {
            return !val1.equals(val2) ? Boolean.TRUE : Boolean.FALSE;
        }
        else return (val2 != null && !val2.equals(val1)) ? Boolean.TRUE : Boolean.FALSE;
    }

    private static Object doOperationsSameType(int type1, Object val1, int operation, Object val2) {
        switch (type1) {
            case DataTypes.INTEGER:
            case DataTypes.W_INTEGER:
                switch (operation) {
                    case ADD:
                        return ((Integer) val1) + ((Integer) val2);
                    case SUB:
                        return ((Integer) val1) - ((Integer) val2);
                    case DIV:
                        return narrowType(new BigDecimal((Integer) val1, MATH_CONTEXT).divide(new BigDecimal((Integer) val2), MATH_CONTEXT));
                    case MULT:
                        return ((Integer) val1) * ((Integer) val2);
                    case POWER:
                        double d = Math.pow((Integer) val1, (Integer) val2);
                        if (d > Integer.MAX_VALUE) return d;
                        else return (int) d;
                    case MOD:
                        return ((Integer) val1) % ((Integer) val2);

                    case GTHAN:
                        return ((Integer) val1) > ((Integer) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case GETHAN:
                        return ((Integer) val1) >= ((Integer) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LTHAN:
                        return ((Integer) val1) < ((Integer) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LETHAN:
                        return ((Integer) val1) <= ((Integer) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case EQUAL:
                        return ((Integer) val1).intValue() == ((Integer) val2).intValue() ? Boolean.TRUE : Boolean.FALSE;
                    case NEQUAL:
                        return ((Integer) val1).intValue() != ((Integer) val2).intValue() ? Boolean.TRUE : Boolean.FALSE;

                    case BW_AND:
                        return (Integer) val1 & (Integer) val2;
                    case BW_OR:
                        return (Integer) val1 | (Integer) val2;
                    case BW_SHIFT_LEFT:
                        return (Integer) val1 << (Integer) val2;
                    case BW_SHIFT_RIGHT:
                        return (Integer) val1 >> (Integer) val2;
                    case BW_USHIFT_RIGHT:
                        return (Integer) val1 >>> (Integer) val2;
                    case BW_XOR:
                        return (Integer) val1 ^ (Integer) val2;
                }

            case DataTypes.SHORT:
            case DataTypes.W_SHORT:
                switch (operation) {
                    case ADD:
                        return ((Short) val1) + ((Short) val2);
                    case SUB:
                        return ((Short) val1) - ((Short) val2);
                    case DIV:
                        return narrowType(new InternalNumber((Short) val1, MATH_CONTEXT).divide(new InternalNumber((Short) val2), MATH_CONTEXT));
                    case MULT:
                        return ((Short) val1) * ((Short) val2);
                    case POWER:
                        double d = Math.pow((Short) val1, (Short) val2);
                        if (d > Short.MAX_VALUE) return d;
                        else return (short) d;
                    case MOD:
                        return ((Short) val1) % ((Short) val2);

                    case GTHAN:
                        return ((Short) val1) > ((Short) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case GETHAN:
                        return ((Short) val1) >= ((Short) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LTHAN:
                        return ((Short) val1) < ((Short) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LETHAN:
                        return ((Short) val1) <= ((Short) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case EQUAL:
                        return ((Short) val1).shortValue() == ((Short) val2).shortValue() ? Boolean.TRUE : Boolean.FALSE;
                    case NEQUAL:
                        return ((Short) val1).shortValue() != ((Short) val2).shortValue() ? Boolean.TRUE : Boolean.FALSE;

                    case BW_AND:
                        return (Short) val1 & (Short) val2;
                    case BW_OR:
                        return (Short) val1 | (Short) val2;
                    case BW_SHIFT_LEFT:
                        return (Short) val1 << (Short) val2;
                    case BW_SHIFT_RIGHT:
                        return (Short) val1 >> (Short) val2;
                    case BW_USHIFT_RIGHT:
                        return (Short) val1 >>> (Short) val2;
                    case BW_XOR:
                        return (Short) val1 ^ (Short) val2;
                }

            case DataTypes.LONG:
            case DataTypes.W_LONG:
                switch (operation) {
                    case ADD:
                        return ((Long) val1) + ((Long) val2);
                    case SUB:
                        return ((Long) val1) - ((Long) val2);
                    case DIV:
                        return narrowType(new InternalNumber((Long) val1, MATH_CONTEXT).divide(new InternalNumber((Long) val2), MATH_CONTEXT));
                    case MULT:
                        return ((Long) val1) * ((Long) val2);
                    case POWER:
                        double d = Math.pow((Long) val1, (Long) val2);
                        if (d > Long.MAX_VALUE) return d;
                        else return (long) d;
                    case MOD:
                        return ((Long) val1) % ((Long) val2);

                    case GTHAN:
                        return ((Long) val1) > ((Long) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case GETHAN:
                        return ((Long) val1) >= ((Long) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LTHAN:
                        return ((Long) val1) < ((Long) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LETHAN:
                        return ((Long) val1) <= ((Long) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case EQUAL:
                        return ((Long) val1).longValue() == ((Long) val2).longValue() ? Boolean.TRUE : Boolean.FALSE;
                    case NEQUAL:
                        return ((Long) val1).longValue() != ((Long) val2).longValue() ? Boolean.TRUE : Boolean.FALSE;

                    case BW_AND:
                        return (Long) val1 & (Long) val2;
                    case BW_OR:
                        return (Long) val1 | (Long) val2;
                    case BW_SHIFT_LEFT:
                        return (Long) val1 << (Long) val2;
                    case BW_USHIFT_LEFT:
                        throw new UnsupportedOperationException("unsigned left-shift not supported");

                    case BW_SHIFT_RIGHT:
                        return (Long) val1 >> (Long) val2;
                    case BW_USHIFT_RIGHT:
                        return (Long) val1 >>> (Long) val2;
                    case BW_XOR:
                        return (Long) val1 ^ (Long) val2;
                }

            case DataTypes.UNIT:
                val2 = ((Unit) val1).convertFrom(val2);
                val1 = ((Unit) val1).getValue();

            case DataTypes.DOUBLE:
            case DataTypes.W_DOUBLE:
                switch (operation) {
                    case ADD:
                        return ((Double) val1) + ((Double) val2);
                    case SUB:
                        return ((Double) val1) - ((Double) val2);
                    case DIV:
                        return narrowType(new InternalNumber((Double) val1, MATH_CONTEXT).divide(new InternalNumber((Double) val2), MATH_CONTEXT));
                    case MULT:
                        return ((Double) val1) * ((Double) val2);
                    case POWER:
                        return Math.pow((Double) val1, (Double) val2);
                    case MOD:
                        return ((Double) val1) % ((Double) val2);

                    case GTHAN:
                        return ((Double) val1) > ((Double) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case GETHAN:
                        return ((Double) val1) >= ((Double) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LTHAN:
                        return ((Double) val1) < ((Double) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LETHAN:
                        return ((Double) val1) <= ((Double) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case EQUAL:
                        return ((Double) val1).doubleValue() == ((Double) val2).doubleValue() ? Boolean.TRUE : Boolean.FALSE;
                    case NEQUAL:
                        return ((Double) val1).doubleValue() != ((Double) val2).doubleValue() ? Boolean.TRUE : Boolean.FALSE;

                    case BW_AND:
                    case BW_OR:
                    case BW_SHIFT_LEFT:
                    case BW_SHIFT_RIGHT:
                    case BW_USHIFT_RIGHT:
                    case BW_XOR:
                        throw new CompileException("bitwise operation on a non-fixed-point number.");
                }

            case DataTypes.FLOAT:
            case DataTypes.W_FLOAT:
                switch (operation) {
                    case ADD:
                        return ((Float) val1) + ((Float) val2);
                    case SUB:
                        return ((Float) val1) - ((Float) val2);
                    case DIV:
                        return narrowType(new InternalNumber((Float) val1, MATH_CONTEXT).divide(new InternalNumber((Float) val2), MATH_CONTEXT));
                    case MULT:
                        return ((Float) val1) * ((Float) val2);
                    case POWER:
                        return narrowType(new InternalNumber((Float) val1, MATH_CONTEXT).pow(new InternalNumber((Float) val2).intValue(), MATH_CONTEXT));
                    case MOD:
                        return ((Float) val1) % ((Float) val2);

                    case GTHAN:
                        return ((Float) val1) > ((Float) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case GETHAN:
                        return ((Float) val1) >= ((Float) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LTHAN:
                        return ((Float) val1) < ((Float) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case LETHAN:
                        return ((Float) val1) <= ((Float) val2) ? Boolean.TRUE : Boolean.FALSE;
                    case EQUAL:
                        return ((Float) val1).floatValue() == ((Float) val2).floatValue() ? Boolean.TRUE : Boolean.FALSE;
                    case NEQUAL:
                        return ((Float) val1).floatValue() != ((Float) val2).floatValue() ? Boolean.TRUE : Boolean.FALSE;

                    case BW_AND:
                    case BW_OR:
                    case BW_SHIFT_LEFT:
                    case BW_SHIFT_RIGHT:
                    case BW_USHIFT_RIGHT:
                    case BW_XOR:
                        throw new CompileException("bitwise operation on a non-fixed-point number.");
                }

            case DataTypes.BIG_INTEGER:
                switch (operation) {
                    case ADD:
                        return ((BigInteger) val1).add(((BigInteger) val2));
                    case SUB:
                        return ((BigInteger) val1).subtract(((BigInteger) val2));
                    case DIV:
                        return ((BigInteger) val1).divide(((BigInteger) val2));
                    case MULT:
                        return ((BigInteger) val1).multiply(((BigInteger) val2));
                    case POWER:
                        return ((BigInteger) val1).pow(((BigInteger) val2).intValue());
                    case MOD:
                        return ((BigInteger) val1).remainder(((BigInteger) val2));

                    case GTHAN:
                        return ((BigInteger) val1).compareTo(((BigInteger) val2)) == 1 ? Boolean.TRUE : Boolean.FALSE;
                    case GETHAN:
                        return ((BigInteger) val1).compareTo(((BigInteger) val2)) >= 0 ? Boolean.TRUE : Boolean.FALSE;
                    case LTHAN:
                        return ((BigInteger) val1).compareTo(((BigInteger) val2)) == -1 ? Boolean.TRUE : Boolean.FALSE;
                    case LETHAN:
                        return ((BigInteger) val1).compareTo(((BigInteger) val2)) <= 0 ? Boolean.TRUE : Boolean.FALSE;
                    case EQUAL:
                        return ((BigInteger) val1).compareTo(((BigInteger) val2)) == 0 ? Boolean.TRUE : Boolean.FALSE;
                    case NEQUAL:
                        return ((BigInteger) val1).compareTo(((BigInteger) val2)) != 0 ? Boolean.TRUE : Boolean.FALSE;

                    case BW_AND:
                    case BW_OR:
                    case BW_SHIFT_LEFT:
                    case BW_SHIFT_RIGHT:
                    case BW_USHIFT_RIGHT:
                    case BW_XOR:
                        throw new CompileException("bitwise operation on a number greater than 32-bits not possible");
                }


            default:
                switch (operation) {
                    case EQUAL:
                        return safeEquals(val2, val1);
                    case NEQUAL:
                        return safeNotEquals(val2, val1);
                    case ADD:
                        return valueOf(val1) + valueOf(val2);
                }
        }
        return null;
    }

    private static InternalNumber getInternalNumberFromType(Object in, int type) {
        if (in == null)
            return new InternalNumber(0, MATH_CONTEXT);
        switch (type) {
            case BIG_DECIMAL:
                return new InternalNumber(((BigDecimal) in).doubleValue());
            case DataTypes.BIG_INTEGER:
                return new InternalNumber((BigInteger) in, MathContext.DECIMAL128);
            case DataTypes.W_INTEGER:
                return new InternalNumber((Integer) in, MathContext.DECIMAL32);
            case DataTypes.W_LONG:
                return new InternalNumber((Long) in, MathContext.DECIMAL64);
            case DataTypes.STRING:
                return new InternalNumber((String) in, MathContext.DECIMAL64);
            case DataTypes.W_FLOAT:
                return new InternalNumber((Float) in, MathContext.DECIMAL64);
            case DataTypes.W_DOUBLE:
                return new InternalNumber((Double) in, MathContext.DECIMAL64);
            case DataTypes.W_SHORT:
                return new InternalNumber((Short) in, MathContext.DECIMAL32);
            case DataTypes.W_CHAR:
                return new InternalNumber((Character) in, MathContext.DECIMAL32);
            case DataTypes.W_BOOLEAN:
                return new InternalNumber(((Boolean) in) ? 1 : 0);
                //return InternalNumber.valueOf(((Boolean) in) ? 1 : 0);
            case DataTypes.UNIT:
                return new InternalNumber(((Unit) in).getValue(), MathContext.DECIMAL64);
        }

        throw new ConversionException("cannot convert <" + in + "> to a numeric type");
    }
}
