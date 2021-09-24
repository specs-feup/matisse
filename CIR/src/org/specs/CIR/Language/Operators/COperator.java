/**
 * Copyright 2012 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.CIR.Language.Operators;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * InstanceProvider for C types, accepts
 * 
 * @author Joao Bispo
 * 
 */
public enum COperator implements InstanceProvider {

    Modulo("%", "%=", PrecedenceLevel.Multiplication),
    Multiplication("*", "*=", PrecedenceLevel.Multiplication),
    Division("/", "/=", PrecedenceLevel.Multiplication),
    Addition("+", "+=", "++", PrecedenceLevel.Addition),
    Subtraction("-", "-=", "--", PrecedenceLevel.Addition),
    UnaryPlus("+", PrecedenceLevel.PrefixIncrement, OperatorType.UnaryPrefix),
    UnaryMinus("-", PrecedenceLevel.PrefixIncrement, OperatorType.UnaryPrefix),

    LessThan("<", null, PrecedenceLevel.LessThan, OperatorType.Binary, false, true),
    LessThanOrEqual("<=", null, PrecedenceLevel.LessOrEqualTo, OperatorType.Binary, false, true),
    GreaterThan(">", null, PrecedenceLevel.GreaterThan, OperatorType.Binary, false, true),
    GreaterThanOrEqual(">=", null, PrecedenceLevel.GreaterOrEqualTo, OperatorType.Binary, false, true),
    Equal("==", null, PrecedenceLevel.Equality, OperatorType.Binary, false, true),
    NotEqual("!=", null, PrecedenceLevel.NotEqual, OperatorType.Binary, false, true),

    LogicalNegation("!", PrecedenceLevel.PrefixIncrement, OperatorType.UnaryPrefix),
    LogicalAnd("&&", PrecedenceLevel.LogicalAnd),
    LogicalOr("||", PrecedenceLevel.LogicalOr),

    BitwiseNot("~", PrecedenceLevel.PrefixIncrement, OperatorType.UnaryPrefix),
    BitwiseAnd("&", "&=", PrecedenceLevel.BitwiseAnd, OperatorType.Binary, true, false),
    BitwiseOr("|", "|=", PrecedenceLevel.BitwiseOr, OperatorType.Binary, true, false),
    BitwiseXor("^", "^=", PrecedenceLevel.BitwiseXor),
    BitwiseLeftShit("<<", "<<=", PrecedenceLevel.BitShift),
    BitwiseRightShit(">>", ">>=", PrecedenceLevel.BitShift),

    Ternary("?", null, null, PrecedenceLevel.TernaryConditional, OperatorType.Ternary, false, false);

    private final String coperator;
    private final String assignmentOperator;
    private final String unitAssignmentOperator;
    private final OperatorType type;
    private final boolean integerOp;
    private final boolean isComparison;
    private final PrecedenceLevel precedenceLevel;

    private COperator(String coperator, String assignmentOperator, String unitAssignmentOperator,
            PrecedenceLevel precedenceLevel, OperatorType type,
            boolean integerOp, boolean isComparison) {

        this.coperator = coperator;
        this.assignmentOperator = assignmentOperator;
        this.unitAssignmentOperator = unitAssignmentOperator;
        this.type = type;
        this.integerOp = integerOp;
        this.isComparison = isComparison;
        this.precedenceLevel = precedenceLevel;
    }

    private COperator(String coperator, String assignmentOperator,
            PrecedenceLevel precedenceLevel, OperatorType type,
            boolean integerOp, boolean isResultInteger) {

        this(coperator, assignmentOperator, null, precedenceLevel, type, integerOp, isResultInteger);
    }

    /**
     * 
     */
    private COperator(String coperator, PrecedenceLevel precedenceLevel) {
        this(coperator, null, null, precedenceLevel, OperatorType.Binary, false, false);
    }

    private COperator(String coperator, String assignmentOperator, PrecedenceLevel precedenceLevel) {
        this(coperator, assignmentOperator, null, precedenceLevel, OperatorType.Binary, false, false);
    }

    private COperator(String coperator, String assignmentOperator, String unitAssignmentOperator,
            PrecedenceLevel precedenceLevel) {
        this(coperator, assignmentOperator, unitAssignmentOperator, precedenceLevel, OperatorType.Binary, false, false);
    }

    /**
     * 
     */
    private COperator(String coperator, PrecedenceLevel precedenceLevel, OperatorType type) {
        this(coperator, null, null, precedenceLevel, type, false, false);
        // this.coperator = coperator;
        // this.type = type;
    }

    public boolean isComparison() {
        return this.isComparison;
    }

    /**
     * Whether the operator only applies to integers.
     */
    public boolean isIntegerOp() {
        return this.integerOp;
    }

    /**
     * The equivalent operator in C. E.g., "+" for 'plus'.
     * 
     * @return the coperator
     */
    public String getCoperator() {
        return this.coperator;
    }

    /**
     * Gets the operator for the assignment expression in C. E.g., "+=" for 'plus'
     * 
     * @return The operator, or null if none exists.
     */
    public String getAssignmentOperator() {
        return this.assignmentOperator;
    }

    /**
     * Gets the operator for the assignment expression with a right side of 1 in C. E.g., "++" for 'plus'.
     * 
     * @return The operator, or null if none exists.
     */
    public String getUnitAssignmentOperator() {
        return this.unitAssignmentOperator;
    }

    /**
     * @return the type
     */
    public OperatorType getOperatorType() {
        return this.type;
    }

    public PrecedenceLevel getPrecedenceLevel() {
        return this.precedenceLevel;
    }

    public InstanceProvider getProvider(boolean propagateConstants, boolean invertArguments) {
        return new COperatorProvider(this, propagateConstants, invertArguments);
    }

    /**
     * 
     * @param propagateConstants
     * @return a COperator provider that does not invert the arguments.
     */
    public InstanceProvider getProvider(boolean propagateConstants) {
        return new COperatorProvider(this, propagateConstants, false);
    }

    /**
     * 
     * @return a COperator provider that does not invert the arguments and does not propagate constants.
     */
    public InstanceProvider getProvider() {
        return getProvider(false, false);
    }

    /**
     * Returns true if the number of inputs is adequate for the operation, or false otherwise.
     * 
     * @param size
     * @return
     */
    public boolean checkNumInputs(int numInputs) {

        switch (this.type) {
        case Binary:
            return numInputs == 2;
        case UnaryPrefix:
            return numInputs == 1;
        case UnarySuffix:
            return numInputs == 1;
        case Ternary:
            return numInputs == 3;
        default:
            SpecsLogs.warn("Case not defined:" + this.type);
            return false;
        }

    }

    /**
     * For each given type, checks if the input type is convertible to ScalarType.
     * 
     * <p>
     * Since this is a CNative operation, input types will be adapted accordingly.
     * 
     * @param inputTypes
     * @return
     */
    public static boolean checkTypes(List<VariableType> inputTypes) {

        for (VariableType inputType : inputTypes) {
            // Using scalar type
            if (!ScalarUtils.hasScalarType(inputType)) {
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.InstanceProvider#getInstance(java.util.List)
     */
    /**
     * Returns a COperator instance that does not invert the arguments and does not propagate constants.
     */
    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        return getProvider().newCInstance(data);
    }

    /**
     * 1. Checks if the number of inputs is the same of the number needed by the given operator. <br>
     * 2. Checks if all inputs are of type numeric
     */
    public boolean checkRule(List<VariableType> inputTypes) {

        // Check number of inputs
        if (!checkNumInputs(inputTypes.size())) {
            return false;
        }

        // Check types
        if (!checkTypes(inputTypes)) {
            return false;
        }

        return true;
    }

    /**
     * Generates code related to operations.
     * <p>
     * Ex.: a + b
     * 
     * @param op
     * @param arguments
     * @return
     */
    public String getCode(List<CNode> arguments) {

        if (arguments.isEmpty()) {
            SpecsLogs.warn("No arguments while implementing operator '" + this + "'.");
            return null;
        }

        // Generate code according to type
        CNode firstArgument = arguments.get(0);
        String code = null;
        switch (getOperatorType()) {
        case UnaryPrefix:
            code = getCoperator() + firstArgument.getCodeForContent(getPrecedenceLevel());
            break;
        case UnarySuffix:
            code = firstArgument.getCodeForContent(getPrecedenceLevel()) + getCoperator();
            break;
        case Binary: {
            StringBuilder builder = new StringBuilder();

            builder.append(firstArgument.getCodeForLeftSideOf(getPrecedenceLevel()));
            for (int i = 1; i < arguments.size(); i++) {
                if (i > 1) {
                    SpecsLogs.warn("Found COperator with more than two operands");
                }
                // Get other child
                CNode otherChild = arguments.get(i);

                // Get code
                String otherChildCode = otherChild.getCodeForRightSideOf(getPrecedenceLevel());

                // Without spaces between operands
                builder.append(" ");
                builder.append(getCoperator());
                builder.append(" ");
                builder.append(otherChildCode);
            }
            code = builder.toString();
            break;
        }
        case Ternary: {
            StringBuilder builder = new StringBuilder();

            builder.append(firstArgument.getCodeForLeftSideOf(getPrecedenceLevel()));

            assert this.coperator.equals("?");
            assert this.precedenceLevel == PrecedenceLevel.TernaryConditional;

            builder.append(" ");
            builder.append("?");
            builder.append(" ");
            builder.append(arguments.get(1).getCode());

            builder.append(" : ");
            builder.append(arguments.get(2).getCodeForRightSideOf(getPrecedenceLevel()));

            code = builder.toString();
            break;
        }
        default:
            throw new UnsupportedOperationException();
        }

        // HACK Because of bitwise_and and bitwise_or, .
        if (this == COperator.BitwiseAnd || this == COperator.BitwiseOr || this == COperator.BitwiseXor) {
            code = "(" + code + ")";
        }

        return code;
    }

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
        // System.out.println("INPUTS:" + data.getInputTypes());
        // System.out.println("RULE PASSES?" + checkRule(data.getInputTypes()));
        if (!checkRule(data.getInputTypes())) {
            return Optional.empty();
        }

        return Optional.of(getProvider());
    }

    public boolean isLogical() {
        return this == LogicalAnd || this == LogicalNegation || this == LogicalOr;
    }
}
