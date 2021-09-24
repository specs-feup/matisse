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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.CNative.CNativeUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.Logical.LogicalType;
import org.suikasoft.MvelPlus.MvelSolver;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Implements a FunctionInstance for a C operators, for CNative types.
 * 
 * 
 * @author Joao Bispo
 * 
 */
public class COperatorBuilder extends AInstanceBuilder {

    private final COperatorData opData;

    /**
     * @param data
     */
    public COperatorBuilder(ProviderData data, COperatorData opData) {
        super(data);

        this.opData = opData;
    }

    public COperatorBuilder(ProviderData data, COperator op) {
        this(data, new COperatorData(op, false));
    }

    /**
     * Creates a FunctionImplementation representing a C native operation between scalars (e.g., a+b).
     * 
     * <p>
     * Rules for the input types: <br>
     * 1. The number of inputs has to be the same as the number needed by the given operator;<br>
     * 2. All inputs must be of type CNativeType;<br>
     * 
     * 
     * @param inputTypes
     * @param op
     * @param invertArgs
     * @return
     */
    @Override
    public FunctionInstance create() {

        FunctionType functionTypes = getFunctionType();

        String functionName = opData.op.name();
        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                String code = COperatorSolver.getCode(opData.op, arguments,
                        opData.invertArgs);

                return code;
            }
        };

        // Build C operator implementation
        InlinedInstance instance;
        String assignmentOperator = opData.op.getAssignmentOperator();

        if (!opData.invertArgs && assignmentOperator != null) {
            InlineCode assignmentInlineCode = new InlineCode() {
                @Override
                public String getInlineCode(List<CNode> arguments) {
                    StringBuilder builder = new StringBuilder();

                    String unitAssignment = opData.op.getUnitAssignmentOperator();
                    if (unitAssignment != null && isOne(arguments.get(1).getCode())) {
                        builder.append(unitAssignment);
                        builder.append(arguments.get(0).getCodeForContent(PrecedenceLevel.PrefixIncrement));

                        return builder.toString();
                    }

                    builder.append(arguments.get(0).getCodeForContent(PrecedenceLevel.Assignment));

                    builder.append(" ");
                    builder.append(assignmentOperator);
                    builder.append(" ");

                    builder.append(arguments.get(1).getCodeForContent(PrecedenceLevel.Assignment));

                    return builder.toString();
                }

                private boolean isOne(String code) {
                    return code.equals("1") || code.equals("1.0");
                }
            };

            instance = new COperatorInstance(functionTypes, functionName, inlineCode, assignmentInlineCode);
        } else {
            instance = new InlinedInstance(functionTypes, functionName, inlineCode);
        }
        instance.setCallPrecedenceLevel(opData.op.getPrecedenceLevel());
        instance.setMaintainLiteralTypes(opData.op.isComparison());

        return instance;
    }

    public FunctionType getFunctionType() {
        // Get the CNativeTypes for the inputs
        // List<VariableType> inputTypes = getData().getInputTypes();
        List<CNativeType> inputTypes = CNativeUtils.toCNative(getData().getInputTypes());

        // Build function types
        FunctionType functionTypes = newFunctionTypes(inputTypes, opData);
        return functionTypes;
    }

    /**
     * Builds the FunctionTypes for the COperator implementation.
     * 
     * @param argumentTypes
     * @param propagateConstants
     * @param propagateConstants2
     * @return
     */
    private FunctionType newFunctionTypes(List<CNativeType> argumentTypes, COperatorData data) {

        ScalarType outputType = calculateOutputType(argumentTypes, data);

        // Treat logical types
        argumentTypes = parseLogicals(argumentTypes, data);

        // if(argumentTypes)

        /*
        if (opData.op == COperator.Multiplication) {
        System.out.println("MULT INPUTS:" + argumentTypes);
        System.out.println("MULT OUTPUT:" + outputType);
        }
        */

        // Always convert the input types to the output type
        List<VariableType> inputTypes;
        if (data.op.isComparison()) {
            validateInputTypes(data.op, argumentTypes);

            inputTypes = new ArrayList<>(argumentTypes);
        } else {
            inputTypes = buildAllSameType(outputType, argumentTypes.size());
        }

        // Build FunctionInputs
        FunctionType functionInputs = FunctionType.newInstanceNotImplementable(inputTypes, outputType);

        return functionInputs;
    }

    private List<CNativeType> parseLogicals(List<CNativeType> argumentTypes, COperatorData data) {

        // If only one argument, return
        if (argumentTypes.size() == 1) {
            return argumentTypes;
        }

        Set<Integer> logicalTypeIndexes = IntStream.range(0, argumentTypes.size())
                .filter(i -> argumentTypes.get(i) instanceof LogicalType)
                .collect(() -> new HashSet<>(), (list, i) -> list.add(i), (list1, list2) -> list1.addAll(list2));

        // If no logical type found return input arguments
        if (logicalTypeIndexes.isEmpty()) {
            return argumentTypes;
        }

        // If all arguments are logicals, replace with ints
        if (logicalTypeIndexes.size() == argumentTypes.size()) {
            List<CNativeType> newInputs = new ArrayList<>();
            for (int i = 0; i < argumentTypes.size(); i++) {
                newInputs.add(getNumerics().newInt());
            }
            return newInputs;
            // return Arrays.asList(getNumerics().newInt(), getNumerics().newInt());
        }

        // Replace the logical type with the first type that is not logical
        int nonLogicalIndex = IntStream.range(0, argumentTypes.size())
                .filter(i -> !logicalTypeIndexes.contains(i))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected to find a non-logical type in " + argumentTypes));

        // Build the new input types
        CNativeType nonLogicalType = argumentTypes.get(nonLogicalIndex);
        List<CNativeType> newInputs = new ArrayList<>();
        for (int i = 0; i < argumentTypes.size(); i++) {
            CNativeType newInputType = logicalTypeIndexes.contains(i) ? nonLogicalType : argumentTypes.get(i);
            newInputs.add(newInputType);
        }

        return newInputs;
        // Preconditions.checkArgument(logicalTypeIndexes.size() == 1,
        // "Expected one logical type index: " + logicalTypeIndexes);
        //
        // int nonLogicalIndex = logicalTypeIndexes.get(0) == 0 ? 1 : 0;

        // return Arrays.asList(argumentTypes.get(nonLogicalIndex), argumentTypes.get(nonLogicalIndex));
    }

    private static void validateInputTypes(COperator op, List<CNativeType> argumentTypes) {
        assert argumentTypes.size() == 2;

        CNativeType type1 = argumentTypes.get(0);
        CNativeType type2 = argumentTypes.get(1);

        if (type1.scalar().isUnsigned() != type2.scalar().isUnsigned()) {
            throw new UnsupportedOperationException(
                    "Comparing signed and unsigned types: " + argumentTypes + ", with operator " + op);
        }
    }

    /**
     * @param inputTypes
     * @param type
     */
    private static List<VariableType> buildAllSameType(ScalarType type, int numTypes) {

        List<VariableType> typesList = Lists.newArrayList();

        // Remove constant information from type
        type = ScalarUtils.removeConstant(type);

        for (int i = 0; i < numTypes; i++) {
            typesList.add(type);
        }

        return typesList;
    }

    /**
     * All inputs must be scalar.
     * 
     * @param inputTypes
     * @param data
     * @param propagateConstants
     * @return
     */
    private ScalarType calculateOutputType(List<CNativeType> inputTypes, COperatorData data) {

        // Remove pointer info and cast again to CNative
        inputTypes = CNativeUtils.toCNative(ReferenceUtils.getType(inputTypes, false));

        // Parse input types using special rules
        inputTypes = parseInputTypes(inputTypes);

        Optional<ScalarType> supposedOutputType = getOutput();
        // supposedOutputType = parseOutputType(supposedOutputType);

        ScalarType inferredType = getInferredType(inputTypes, supposedOutputType);

        ScalarType outputType = (ScalarType) inferredType.copy();

        // Remove constant
        outputType = ScalarUtils.removeConstant(outputType);

        if (data.op.isComparison() || data.op.isLogical()) {
            // outputType = getNumerics().newInt();
            outputType = LogicalType.newInstance();
        } else if (data.op.isIntegerOp()) {
            // Transform into integer, if the operation is only over integer values
            outputType = outputType.scalar().toInteger();
        }

        boolean allWeakInts = isInferenceWeak(inputTypes);

        // If constant propagation disabled, and types are not weak integers, just return type
        if (!getData().isPropagateConstants() && !allWeakInts) {
            return outputType;
        }

        // Check if input types have constants
        List<String> constantStrings = ScalarUtils.getConstantStrings(inputTypes);

        if (constantStrings == null) {
            return outputType;
        }

        String result = solve(data.op, constantStrings, data.invertArgs);

        // If could not calculate constant, return value without constant
        if (result == null) {
            return outputType;
        }

        // Set constant
        outputType = outputType.scalar().setConstantString(result);
        // Set literal, if all inputs weak integers
        if (allWeakInts) {
            outputType = outputType.scalar().setLiteral(true);
        }

        ScalarType type = (ScalarType) ScalarUtils.setConstantString(outputType, result);

        return type;
    }

    private List<CNativeType> parseInputTypes(List<CNativeType> inputTypes) {
        // TODO: Not tested
        // If its a bitwise operation, only consider integer inputs
        if (opData.op.isIntegerOp()) {
            return onlyIntegers(inputTypes);
        }

        return inputTypes;
    }

    private List<CNativeType> onlyIntegers(List<CNativeType> inputTypes) {
        List<CNativeType> newTypes = new ArrayList<>();
        for (CNativeType type : inputTypes) {
            // If real, skip type
            if (!type.scalar().isInteger()) {
                continue;
            }

            newTypes.add(type);
        }

        // If input types are empty, add integer
        if (newTypes.isEmpty()) {
            newTypes.add(getNumerics().newInt());
        }

        return newTypes;
    }

    /**
     * @return
     */
    private Optional<ScalarType> getOutput() {
        VariableType outputType = getData().getOutputType();
        if (outputType == null) {
            return Optional.empty();
        }

        return Optional.of(ScalarUtils.toScalar(outputType));
    }

    public static String solve(COperator op, List<String> values, boolean invertArgs) {

        // Create function
        List<CNode> inputNumbers = SpecsFactory.newArrayList();
        for (String value : values) {
            CNumber newNumber = CLiteral.newNumber(value);
            inputNumbers.add(CNodeFactory.newParenthesis(CNodeFactory.newCNumber(newNumber)));
        }

        // Build function call
        // CToken functionCall = op.getFunctionCall(inputNumbers);

        // Get C code expression
        // String expression = CodeGeneratorUtils.tokenCode(functionCall);

        // Get C code expression
        String expression = COperatorSolver.getCode(op, inputNumbers, invertArgs);

        // Evaluate expression
        Object result = MvelSolver.eval(expression);
        if (result == null) {
            result = COperatorSolver.solve(op, inputNumbers, invertArgs);
            if (result == null) {
                // throw new RuntimeException("Could not solve expression '" + expression + "'");
                SpecsLogs.warn("Could not solve expression '" + expression + "'");
                SpecsLogs.warn("Op: " + op + "; Values: " + values);
                return null;
            }

        }

        String resultString = result.toString();

        resultString = parseResult(resultString);

        return resultString;
    }

    /**
     * Parses the result obtain by the solver (e.g., transforms 'false' into '0' and 'true' into '1')
     * 
     * @param resultString
     * @return
     */
    private static String parseResult(String resultString) {
        if (resultString.equals("false")) {
            return Integer.toString(0);
        }

        if (resultString.equals("true")) {
            return Integer.toString(1);
        }

        return resultString;
    }

}
