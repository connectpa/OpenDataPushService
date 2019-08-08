package it.connectpa.odataservice.service;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

public class FilterExpressionVisitor implements ExpressionVisitor<Object> {

    private final Entity currentEntity;

    public FilterExpressionVisitor(Entity currentEntity) {
        this.currentEntity = currentEntity;
    }

    @Override
    public Object visitBinaryOperator(final BinaryOperatorKind operator, final Object left, final Object right)
            throws ExpressionVisitException, ODataApplicationException {
        if (null == operator) {
            throw new ODataApplicationException("No operation",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        } else {
            switch (operator) {
                case ADD:
                case MOD:
                case MUL:
                case DIV:
                case SUB:
                    return evaluateArithmeticOperation(operator, left, right);
                case EQ:
                case NE:
                case GE:
                case GT:
                case LE:
                case LT:
                    return evaluateComparisonOperation(operator, left, right);
                case AND:
                case OR:
                    return evaluateBooleanOperation(operator, left, right);
                default:
                    throw new ODataApplicationException("Binary operation " + operator.name() + " is not implemented",
                            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
            }
        }
    }

    @Override
    public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand) throws ExpressionVisitException,
            ODataApplicationException {
        // OData allows two different unary operators
        // Checking if the type of the operator and operand fit together
        if (operator == UnaryOperatorKind.NOT && operand instanceof Boolean) {
            // 1.) boolean negation
            return !(Boolean) operand;
        } else if (operator == UnaryOperatorKind.MINUS && operand instanceof Integer) {
            // 2.) arithmetic minus
            return -(Integer) operand;
        }

        // Operation not processed, throw an exception
        throw new ODataApplicationException("Invalid type for unary operator",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public Object visitMethodCall(final MethodKind methodCall, final List<Object> parameters)
            throws ExpressionVisitException, ODataApplicationException {
        // only one method call is implemented here
        if (methodCall == MethodKind.CONTAINS) {
            // "Contains" gets two parameters, both have to be of type String
            if (parameters.get(0) instanceof String && parameters.get(1) instanceof String) {
                String valueParam1 = (String) parameters.get(0);
                String valueParam2 = (String) parameters.get(1);

                return valueParam1.contains(valueParam2);
            } else {
                throw new ODataApplicationException("Contains needs two parametes of type Edm.String",
                        HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
            }
        } else {
            throw new ODataApplicationException("Method call " + methodCall + " not implemented",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
    }

    @Override
    public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression) throws
            ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lamdba references are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public Object visitLiteral(final Literal literal) throws ExpressionVisitException, ODataApplicationException {
        String literalAsString = literal.getText();
        if (literal.getType() instanceof EdmString) {
            String stringLiteral = "";
            if (literal.getText().length() > 2) {
                stringLiteral = literalAsString.substring(1, literalAsString.length() - 1);
            }
            return stringLiteral;
        } else {
            try {
                return Integer.parseInt(literalAsString);
            } catch (NumberFormatException e) {
                throw new ODataApplicationException("Only Edm.Int32 and Edm.String literals are implemented",
                        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
            }
        }
    }

    @Override
    public Object visitMember(final Member member) throws ExpressionVisitException, ODataApplicationException {
        //here it is only for primitive properties.
        final List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();

        // UriParser checks if the property has been defined in service metadata document.
        if (uriResourceParts.size() == 1 && uriResourceParts.get(0) instanceof UriResourcePrimitiveProperty) {
            UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResourceParts.get(0);
            return currentEntity.getProperty(uriResourceProperty.getProperty().getName()).getValue();
        } else {
            throw new ODataApplicationException("Only primitive properties are implemented in filter expressions",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
    }

    @Override
    public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Aliases are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Type literals are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public Object visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lamdba references are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public Object visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException,
            ODataApplicationException {
        throw new ODataApplicationException("Enums are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    private Object evaluateArithmeticOperation(final BinaryOperatorKind operator, final Object left,
            final Object right) throws ODataApplicationException {
        // First check if the type of both operands is numerical
        if (left instanceof Integer && right instanceof Integer) {
            Integer valueLeft = (Integer) left;
            Integer valueRight = (Integer) right;

            switch (operator) {
                case ADD:
                    return valueLeft + valueRight;
                case SUB:
                    return valueLeft - valueRight;
                case MUL:
                    return valueLeft * valueRight;
                case DIV:
                    return valueLeft / valueRight;
                default:
                    // BinaryOperatorKind,MOD
                    return valueLeft % valueRight;
            }
        } else {
            throw new ODataApplicationException("Arithmetic operations needs two numeric operands",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }
    }

    private Object evaluateComparisonOperation(final BinaryOperatorKind operator, final Object left,
            final Object right) throws ODataApplicationException {
        // Checking the types are equals
        if (left.getClass().equals(right.getClass()) && left instanceof Comparable) {
            int result;
            if (left instanceof Integer) {
                result = ((Comparable<Integer>) (Integer) left).compareTo((Integer) right);
            } else if (left instanceof String) {
                result = ((Comparable<String>) (String) left).compareTo((String) right);
            } else if (left instanceof Boolean) {
                result = ((Comparable<Boolean>) (Boolean) left).compareTo((Boolean) right);
            } else {
                throw new ODataApplicationException("Class " + left.getClass().getCanonicalName() + " not expected",
                        HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
            }

            switch (operator) {
                case EQ:
                    return result == 0;
                case NE:
                    return result != 0;
                case GE:
                    return result >= 0;
                case GT:
                    return result > 0;
                case LE:
                    return result <= 0;
                default:
                    // BinaryOperatorKind.LT
                    return result < 0;
            }

        } else {
            throw new ODataApplicationException("Comparision needs two equal types",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }
    }

    private Object evaluateBooleanOperation(final BinaryOperatorKind operator, final Object left, final Object right)
            throws ODataApplicationException {
        // First check that both operands are of type Boolean
        if (left instanceof Boolean && right instanceof Boolean) {
            Boolean valueLeft = (Boolean) left;
            Boolean valueRight = (Boolean) right;

            // Than calculate the result value
            if (operator == BinaryOperatorKind.AND) {
                return valueLeft && valueRight;
            } else {
                // OR
                return valueLeft || valueRight;
            }
        } else {
            throw new ODataApplicationException("Boolean operations needs two numeric operands",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }
    }

}
