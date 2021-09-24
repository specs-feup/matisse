package org.specs.MatlabIR.MatlabLanguage;

import java.util.Map;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CellStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ParenthesisStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.classmap.ClassSet;

/**
 * Matlab Operators.
 * 
 * <p>
 * In MATLAB, all operators are left-associative (yes, including exponentiation).
 * 
 * @author JoaoBispo
 *
 */
public enum MatlabOperator {

    Transpose(".'", "transpose", 10, 1),
    Power(".^", "power", 10, 2),
    ComplexConjugateTranspose("'", "ctranspose", 10, 1),
    MatrixPower("^", "mpower", 10, 2),

    UnaryPlus(".+", "uplus", 9, 1),
    UnaryMinus(".-", "uminus", 9, 1),
    /**
     * '~'
     */
    LogicalNegation("~", "not", 9, 1),

    Multiplication(".*", "times", 8, 2),
    RightDivision("./", "rdivide", 8, 2),
    LeftDivision(".\\", "ldivide", 8, 2),
    MatrixMultiplication("*", "mtimes", 8, 2),
    MatrixRightDivision("/", "mrdivide", 8, 2),
    MatrixLeftDivision("\\", "mldivide", 8, 2),

    Addition("+", "plus", 7, 2),
    Subtraction("-", "minus", 7, 2),

    /**
     * ':'
     */
    Colon(":", "colon", 6, 2),

    LessThan("<", "lt", 5, 2),
    LessThanOrEqual("<=", "le", 5, 2),
    GreaterThan(">", "gt", 5, 2),
    GreaterThanOrEqual(">=", "ge", 5, 2),
    Equal("==", "eq", 5, 2),
    NotEqual("~=", "ne", 5, 2),

    ElementWiseAnd("&", "and", 4, 2),

    ElementWiseOr("|", "or", 3, 2),

    ShortCircuitAnd("&&", "MATISSE_short_circuit_and", 2, 2),

    ShortCircuitOr("||", "MATISSE_short_circuit_or", 1, 2);
    //
    // private static final Set<MType> invalidOperands;
    //
    // static {
    // invalidOperands = FactoryUtils.newHashSet();
    //
    // MatlabOperator.invalidOperands.add(MType.ParenthesisStart);
    // MatlabOperator.invalidOperands.add(MType.CellStart);
    // MatlabOperator.invalidOperands.add(MType.SquareBracketsStart);
    // MatlabOperator.invalidOperands.add(MType.Assignment);
    // MatlabOperator.invalidOperands.add(MType.SubscriptSeparator);
    // MatlabOperator.invalidOperands.add(MType.Row);
    // }

    private static final ClassSet<MatlabNode> INVALID_OPERANDS;

    static {
	INVALID_OPERANDS = new ClassSet<>();

	MatlabOperator.INVALID_OPERANDS.add(ParenthesisStartNode.class);
	MatlabOperator.INVALID_OPERANDS.add(CellStartNode.class);
	MatlabOperator.INVALID_OPERANDS.add(SquareBracketsStartNode.class);
	MatlabOperator.INVALID_OPERANDS.add(AssignmentNode.class);
	MatlabOperator.INVALID_OPERANDS.add(SubscriptSeparatorNode.class);
	MatlabOperator.INVALID_OPERANDS.add(RowNode.class);
    }

    private MatlabOperator(String literal, String functionName, int priority, int numOperands) {
	this.literal = literal;
	this.functionName = functionName;
	this.priority = priority;
	this.numOperands = numOperands;
    }

    private final String literal;
    private final String functionName;
    private final int priority;
    private final int numOperands;

    private static final Map<String, MatlabOperator> opMap;

    static {
	opMap = SpecsFactory.newHashMap();

	for (MatlabOperator op : values()) {
	    MatlabOperator previousOp = MatlabOperator.opMap.put(op.getLiteral(), op);
	    if (previousOp != null) {
		SpecsLogs.warn("Operator '" + previousOp
			+ "' has the same literal representation as operator '" + op + "'.");
	    }
	}
    }

    public static MatlabOperator getOp(String literal) {
	return MatlabOperator.opMap.get(literal);
    }

    /**
     * Returns the number of operands of an operator.
     * 
     * @param literal
     *            The operator symbol, as returned by getLiteral(). Remember than unary plus or minus are ".+" and ".-"
     *            respectively, not "+" and "-".
     * @return 1 for unary operators, 2 for binary operators. The range operator (:) can be ternary, but this function
     *         still returns 2 for that case since the range operator *can* receive only two operators.
     */
    public static int getNumOperands(String literal) {

	MatlabOperator mOp = MatlabOperator.opMap.get(literal);

	return mOp.getNumOperands();
    }

    /**
     * Returns a literal that represents the operator. Usually, this is the operator symbol. However, for unary + and -,
     * the literals ".+" and ".-" are used to distinguish them from their binary counterparts.
     * 
     * @return The literal string.
     */
    public String getLiteral() {
	return this.literal;
    }

    public int getNumOperands() {
	return this.numOperands;
    }

    public int getPriority() {
	return this.priority;
    }

    /**
     * Returns the MATLAB operator symbol. This differs from getLiteral() because MATISSE refers to unary + and unary -
     * by the literals ".+" and ".-", whereas this function returns valid MATLAB symbols.
     * 
     * @return A string of the MATLAB operator symbol.
     */
    public String getMatlabString() {
	switch (this) {
	case UnaryPlus:
	    return "+";
	case UnaryMinus:
	    return "-";
	default:
	    return this.literal;
	}
    }

    public String getFunctionName() {
	return this.functionName;
    }

    /*
    public boolean isHiherPriority(MatlabOperator anOp) {
    if (anOp.priority > priority) {
        return true;
    }
    
    return false;
    
    }
    */

    public boolean isBinary() {
	return this.numOperands == 2;
    }

    /**
     * 
     * @param isInsideIfCondition
     * @param state
     * @param lastToken
     * @return true if the given MatlabToken represents a valid operand. False otherwise.
     */
    public static boolean isValidLeftOperand(MatlabNode operandToken, boolean isInsideIfCondition) {

	// Check types that are always invalid
	// if (MatlabOperator.invalidOperands.contains(operandToken.getType())) {
	if (MatlabOperator.INVALID_OPERANDS.contains(operandToken)) {
	    return false;
	}

	// Operators are also invalid left operands, unless they are transpose
	if (operandToken instanceof OperatorNode) {
	    // MatlabOperator op = MatlabOperator.getOp(operandToken.getContentString());
	    MatlabOperator op = ((OperatorNode) operandToken).getOp();
	    if (op != MatlabOperator.Transpose && op != MatlabOperator.ComplexConjugateTranspose) {
		return false;
	    }
	}

	// If previous node is the keyword 'if' and we are inside an if condition, is invalid
	// TODO: This could be avoided if 'currentNodes' hide 'if' when parsing the condition
	if (operandToken instanceof ReservedWordNode &&
		((ReservedWordNode) operandToken).getWord() == ReservedWord.If) {

	    return false;
	}

	return true;
    }

}
