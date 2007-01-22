package org.mvel;

import org.mvel.integration.VariableResolverFactory;

import java.math.BigDecimal;

public class ExecutableAccessor implements ExecutableStatement {
    private Token accessor;

    private Class ingress;
    private Class egress;
    private boolean convertable;

    private boolean booleanMode;
    private boolean returnBigDecimal;

    public ExecutableAccessor(Token accessor, boolean booleanMode, boolean returnBigDecimal) {
        this.accessor = accessor;
        this.booleanMode = booleanMode;
        this.returnBigDecimal = returnBigDecimal;
    }


    public Object getValue(Object staticContext, VariableResolverFactory factory) {
        Object result = accessor.getOptimizedValue(staticContext, staticContext, factory).getValue();
        if (booleanMode) {
            if (result instanceof Boolean) return result;
            else if (result instanceof Token) {
                if (((Token) result).getValue() instanceof Boolean) {
                    return ((Token) result).getValue();
                }
                return !BlankLiteral.INSTANCE.equals(((Token) result).getValue());
            }
            else if (result instanceof BigDecimal) {
                return !BlankLiteral.INSTANCE.equals(((BigDecimal) result).floatValue());
            }
            throw new CompileException("unknown exception in expression: encountered unknown stack element: " + result);
        }
        else if (result instanceof Token) {
            result = ((Token) result).getValue();
        }
        if (accessor.isNumeric()) {
            result = accessor.getNumericValue();
            if (returnBigDecimal) return result;
            else if (((BigDecimal) result).scale() > 14) {
                return ((BigDecimal) result).floatValue();
            }
            else if (((BigDecimal) result).scale() > 0) {
                return ((BigDecimal) result).doubleValue();
            }
            else if (((BigDecimal) result).longValue() > Integer.MAX_VALUE) {
                return ((BigDecimal) result).longValue();
            }
            else {
                return ((BigDecimal) result).intValue();
            }
        }
        else
            return result;
    }


    public void setKnownIngressType(Class type) {
        this.ingress = type;
    }

    public void setKnownEgressType(Class type) {
        this.egress = type;
    }

    public Class getKnownIngressType() {
        return ingress;
    }

    public Class getKnownEgressType() {
        return egress;
    }

    public boolean isConvertableIngressEgress() {
        return convertable;
    }

    public void computeTypeConversionRule() {
        if (ingress != null && egress != null) {
            convertable = ingress.isAssignableFrom(egress);
        }
    }
}


