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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsLogs;

public class MinMaxMatricesDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxMatricesDecBuilder(MinMax minOrMax) {
        this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

        if (!checkRule(builderData)) {
            return null;
        }

        // Process the original types to remove the last input ( the number of outputs )
        List<VariableType> originalTypes = builderData.getInputTypes();
        originalTypes.remove(originalTypes.size() - 1);

        // Call the correct instance
        // return new MinMaxDecFunctions(builderData.getSetupData(),
        // minOrMax).newMinMaxMatricesDecInstance(originalTypes);
        return new MinMaxFunctions(builderData, minOrMax).newMinMaxMatricesDecInstance();
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        List<VariableType> originalTypes = builderData.getInputTypes();

        // See if we can use declared matrices
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // We need three inputs, the declared numeric matrices and the number of outputs ( that will be removed )
        if (originalTypes.size() != 2) {
            return false;
        }

        // The number of outputs must be exactly 1
        if (builderData.getNargouts().orElse(1) != 1) {
            SpecsLogs.msgInfo("Error using " + minOrMax.getName() + "\n" + minOrMax.getName().toUpperCase()
                    + " with two matrices to compare and two output arguments is not supported. Number of outputs: "
                    + builderData.getNargouts().get());
            return false;
        }

        VariableType firstInput = originalTypes.get(0);
        VariableType secondInput = originalTypes.get(1);

        if (!MatrixUtils.isStaticMatrix(firstInput) || !MatrixUtils.isStaticMatrix(secondInput)) {
            return false;
        }

        // The matrices must have the same shape
        List<Integer> firstShape = MatrixUtils.getShapeDims(firstInput);
        List<Integer> secondShape = MatrixUtils.getShapeDims(secondInput);

        if (!firstShape.equals(secondShape)) {
            return false;
        }

        // Check if matrixes element types are of type numeric
        if (!ScalarUtils.hasScalarType(firstInput)) {
            return false;
        }

        if (!ScalarUtils.hasScalarType(secondInput)) {
            return false;
        }

        return true;
    }

    @Override
    public InputsFilter getInputsFilter() {
        return new InputsFilter() {

            @Override
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

                // We only need the first two inputs, the matrices
                inputArguments = Arrays.asList(inputArguments.get(0), inputArguments.get(1));

                return inputArguments;
            }
        };
    }
}
