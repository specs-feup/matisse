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

package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CirUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.matisselib.types.MatlabElementType;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class ScalarDivisionInstance extends AInstanceBuilder {

    public static final String NAME_PREFIX = "scalar_division_";

    private final boolean invertArguments;
    private final VariableType defaultFloat;

    /**
     * @param data
     */
    public ScalarDivisionInstance(ProviderData data, boolean invertArguments, VariableType defaultFloat) {
        super(data);

        this.invertArguments = invertArguments;
        this.defaultFloat = defaultFloat;

    }

    public ScalarDivisionInstance(ProviderData data, boolean invertArguments) {
        this(data, invertArguments, getDefaultReal(data));
    }

    /**
     * Returns the default float type (as a weak type).
     * 
     * @param data
     * @return
     */
    private static VariableType getDefaultReal(ProviderData data) {
        // Get default float type
        VariableType defaultFloat = data.getSettings().get(CirKeys.DEFAULT_REAL);

        // System.out.println("DEFAULT FLOAT:" + defaultFloat);
        // Fix bit size
        // Instead of VariableType, should be a NumericTypeV2

        if (defaultFloat instanceof NumericTypeV2) {
            CTypeV2 cType = ((NumericTypeV2) defaultFloat).getCtype();
            defaultFloat = data.getNumerics().newNumeric(cType);
        } else {
            SpecsLogs.warn("Check if this type should appear here:" + defaultFloat);
        }

        // If it is a default value, should be weak
        if (CirUtils.useWeakTypes()) {
            defaultFloat = defaultFloat.setWeakType(true);

        }

        return defaultFloat;
    }

    /**
     * Creates a new provider for ScalarDivision where the default float is defined by configuration in instance time.
     * 
     * @param invertArguments
     * @return
     */
    public static InstanceProvider newProvider(final boolean invertArguments) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return new ScalarDivisionInstance(data, invertArguments).create();
            }
        };
    }

    /**
     * Creates a new provider for ScalarDivision where the default float is user-specified.
     * 
     * @param inputTypes
     * @param invertArguments
     * @param propagateConstants
     * @return
     */
    public static InstanceProvider newProvider(final boolean invertArguments, final VariableType defaultFloat) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return new ScalarDivisionInstance(data, invertArguments, defaultFloat).create();
            }
        };
    }

    @Override
    public FunctionInstance create() {

        List<VariableType> inputTypes = getData().getInputTypes();

        for (VariableType inputType : inputTypes) {
            if (inputType instanceof MatlabElementType) {
                throw new RuntimeException("Attempting to use MATLAB types in function built for C types.");
            }
        }

        Optional<VariableType> currentOutputType = Optional.empty();
        if (getData().getOutputTypes() != null && !getData().getOutputTypes().isEmpty()) {
            currentOutputType = Optional.ofNullable(getData().getOutputTypes().get(0));
        }

        FunctionType functionInputs = newFunctionTypes(inputTypes, currentOutputType, invertArguments,
                defaultFloat);

        String cFunctionName = ScalarDivisionInstance.NAME_PREFIX + functionInputs.getCReturnType().getSmallId();
        String cFilename = MatlabCFilename.ScalarMath.getCFilename();

        FunctionInstance instance = newInlinedInstance(functionInputs, cFunctionName, cFilename);

        return instance;
    }

    /**
     * 
     * @param functionInputs
     * @param cFunctionName
     * @param cFilename
     * @return
     */
    private static FunctionInstance newInlinedInstance(final FunctionType functionInputs, String cFunctionName,
            String cFilename) {

        InlineCode inlinedCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {

                // Get function input types
                List<VariableType> inputTypes = functionInputs.getArgumentsTypes();

                // Check if any of the arguments needs a cast
                List<CNode> divisionArgs = SpecsFactory.newArrayList();
                for (int i = 0; i < inputTypes.size(); i++) {
                    CNode arg = ConversionUtils.to(arguments.get(i), inputTypes.get(i));
                    divisionArgs.add(arg);
                }

                // Division between 2 elements
                Preconditions.checkArgument(divisionArgs.size() == 2,
                        "Division needs 2 arguments, has " + divisionArgs.size());

                CNode leftNode = divisionArgs.get(0);
                CNode rightNode = divisionArgs.get(1);

                PrecedenceLevel precedence = PrecedenceLevel.Division;

                String leftCode = leftNode.getCode();
                if (PrecedenceLevel.requireLeftParenthesis(precedence, leftNode.getPrecedenceLevel())) {
                    leftCode = "(" + leftCode + ")";
                }
                String rightCode = rightNode.getCode();
                if (PrecedenceLevel.requireRightParenthesis(precedence, rightNode.getPrecedenceLevel())) {
                    rightCode = "(" + rightCode + ")";
                }

                return leftCode + " / " + rightCode;
            }
        };

        InlinedInstance instance = new InlinedInstance(functionInputs, cFunctionName, inlinedCode);

        return instance;
    }

    /**
     * Builds the FunctionTypes for the ScalarDivision.
     * 
     * @param givenTypes
     * @param currentOutputType
     * @param invertArguments
     * @param defaultFloat
     * @param propagateConstants
     * @return
     */
    private FunctionType newFunctionTypes(List<VariableType> givenTypes, Optional<VariableType> currentOutputType,
            boolean invertArguments, VariableType defaultFloat) {

        List<String> inputNames = Arrays.asList("a", "b");

        String outputName = "result";

        Optional<ScalarType> scalarOutput = Optional.empty();
        if (currentOutputType.isPresent() && ScalarUtils.hasScalarType(currentOutputType.get())) {
            scalarOutput = Optional.ofNullable(ScalarUtils.toScalar(currentOutputType.get()));
        }

        VariableType outputType = getOutputType(givenTypes, invertArguments, scalarOutput,
                ScalarUtils.toScalar(defaultFloat));

        /*
        	// If current output type defined, convert it to scalar and use it. Otherwise, infer one
        	VariableType outputType;
        	
        	if (ScalarUtils.hasScalarType(currentOutputType)) {
        	    outputType = ScalarUtils.toScalar(currentOutputType);
        	} else {
        	    outputType = getOutputType(givenTypes, invertArguments, defaultFloat);
        
        	}
        */
        // Input types are the same as the output type, but without the constant (if any)
        VariableType inputType = ScalarUtils.setConstantString(outputType, null);
        List<VariableType> inputTypes = Arrays.asList(inputType, inputType);

        FunctionType functionInputs = FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);

        return functionInputs;
    }

    /**
     * Determines the output type of scalar division.
     * 
     * <p>
     * As default, output type is the lowest priority of the input types, to maintain compatibility with MATLAB results.
     * However, if input types have constants, the results is calculated and the output type is chosen based on that
     * result.
     * 
     * @param inputTypes
     * @param invertArguments
     * @param currentOutput
     * @return
     */
    private VariableType getOutputType(List<VariableType> inputTypes, boolean invertArguments,
            Optional<ScalarType> currentOutput, ScalarType defaultFloat) {

        // If currentOutput is defined, and current function level is 1, division type is equal to return type
        if (getData().getFunctionCallLevel() == 1 && currentOutput.isPresent() && !currentOutput.get().isWeakType()) {
            return currentOutput.get();
        }

        // Get return type
        List<ScalarType> scalarTypes = ScalarUtils.cast(inputTypes);
        ScalarType outType = getInferredType(scalarTypes, currentOutput);

        // If both inputs are not constants, return the highest numeric type
        // If both inputs are integer, return default float
        List<String> constants = ScalarUtils.getConstantStrings(scalarTypes);
        if (constants == null) {

            boolean allIntegers = true;
            for (VariableType inputType : inputTypes) {
                if (!ScalarUtils.isInteger(inputType)) {
                    allIntegers = false;
                    break;
                }
            }

            if (allIntegers) {
                // return currentOutput;
                return defaultFloat;
            }

            return outType;
        }

        // Calculate division
        Double valueA = Double.valueOf(constants.get(0));
        Double valueB = Double.valueOf(constants.get(1));
        Double result = null;

        if (invertArguments) {
            result = valueB / valueA;
        } else {
            result = valueA / valueB;
        }

        // If result is not integer and output type is integer, change output type to default float
        if (result.toString().contains(".") && outType.scalar().isInteger()) {
            outType = defaultFloat;
        }

        // If on the right side of an assignment, use the type of the output
        /*
        if (!getData().getOutputTypes().isEmpty()) {
            VariableType outputType = getData().getOutputTypes().get(0);
            if (outputType != null) {
        	return ScalarUtils.setConstant(outputType, result);
            }
        }
        */

        /*
        // If result is integer, always use max rank type, otherwise the operation might not be correct
        // For instance, if a/b and b is double, but the result is integer (2/0.1), it must be a real division, integer
        // division will give wrong results
        if (ParseUtils.isInteger(result)) {
            return ScalarUtils.setConstant(outType, result);
        }
        System.out.println("OUT TYPE:" + outType);
        // Otherwise, use the default float as result type
        return ScalarUtils.setConstant(defaultFloat, result);
        */
        // Use the calculated output type, and set the result
        return ScalarUtils.setConstant(outType, result);

    }

}
