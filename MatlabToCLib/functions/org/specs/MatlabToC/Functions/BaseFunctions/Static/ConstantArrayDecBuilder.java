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

package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.Check;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Builder for 'zeros' function, when using declared matrixes.
 * 
 * <p>
 * It's a matrix creator function for declared implementation, which means that returns a matrix, which is given as an
 * input to the function.
 * 
 * @author Joao Bispo
 * 
 */
public class ConstantArrayDecBuilder {

    public static InstanceProvider createNumericInputs(String functionName, int setValue) {
        Check check = data -> {

            // Get input types
            List<VariableType> argumentTypes = data.getInputTypes();

            // Check if last one is string
            boolean isLastString = StringTypeUtils.isString(SpecsCollections.last(argumentTypes));
            int offset = 0;
            if (isLastString) {
                offset = 1;
            }

            // Check inputs that are not string
            // for (int i = 0; i < argumentTypes.size() - 1; i++) {
            for (int i = 0; i < argumentTypes.size() - offset; i++) {
                VariableType type = argumentTypes.get(i);

                // Check if it has a numeric element
                if (!ScalarUtils.hasScalarType(type)) {
                    return false;
                }

                // Get Numeric
                VariableType numericType = ScalarUtils.toScalar(type);

                // Check if constant
                if (!ScalarUtils.hasConstant(numericType)) {
                    return false;
                }

            }

            return true;

        };
        MatisseChecker checker = new MatisseChecker()
                .addCheck(check)
                .not().dynamicOutput();

        InstanceProvider base = data -> ConstantArrayDecInstance.newProvider(functionName, setValue).newCInstance(data);
        return MatlabInstanceProvider.create(checker, base, InputsFilter.EMPTY_FILTER);

    }

    public static InstanceProvider createMatrixInputs(String functionName, int setValue) {
        // Check if first matrix has all values defined
        Check check = data -> MatrixUtils.getMatrix(data.getInputTypes().get(0)).getShape().hasValues();

        MatisseChecker checker = new MatisseChecker()
                // Inputs can be matrix with a string with the type
                .numOfInputsRange(1, 2)
                // First input is a matrix
                .isMatrix(0)
                .addCheck(check)
                // Only when there dynamic allocation is disabled
                .not().dynamicAllocationEnabled();

        InstanceProvider base = data -> {

            // Get values from input matrix and pass them as input types
            List<Number> values = MatrixUtils.getMatrix(data.getInputTypes().get(0)).getShape().getValues();
            VariableType intType = data.getNumerics().newInt();

            // Create nodes with the values
            List<CNode> valueNodes = new ArrayList<>();
            values.forEach(value -> valueNodes.add(CNodeFactory.newCNumber(value, intType)));

            // Extract the types from the nodes
            List<VariableType> inputTypes = CNodeUtils.getVariableTypes(valueNodes);

            // Add last type if string
            VariableType lastType = SpecsCollections.last(data.getInputTypes());
            if (StringTypeUtils.isString(lastType)) {
                inputTypes.add(lastType);
            }

            // Change the input types
            data = data.createWithContext(inputTypes);

            return ConstantArrayDecInstance.newProvider(functionName, setValue).newCInstance(data);
        };

        return MatlabInstanceProvider.create(checker, base, InputsFilter.EMPTY_FILTER);
    }

}
