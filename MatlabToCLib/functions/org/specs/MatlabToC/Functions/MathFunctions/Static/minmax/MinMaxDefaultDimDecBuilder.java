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
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsLogs;

public class MinMaxDefaultDimDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxDefaultDimDecBuilder(MinMax minOrMax) {
        this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

        if (!checkRule(builderData)) {
            return null;
        }

        MatrixType inputMatrixType = builderData.getInputType(MatrixType.class, 0);

        // Get the number of outputs
        // int numberOutputs = ScalarUtils.getConstant(originalTypes.get(1)).intValue();
        int numberOutputs = builderData.getInputType(ScalarType.class, 1).scalar().getConstant().intValue();

        // Remove the useless type ( number of outputs ) and add the default DIM, the first non-singular dimension
        int firstNonSing = inputMatrixType.getTypeShape().getFirstNonSingletonDimension();

        // Convert to MATLAB index
        firstNonSing += 1;

        // VariableType dim = VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(firstNonSing));
        // NumericFactoryG numerics = new NumericFactoryG(builderData.getSetup().getCBitSizes());
        NumericFactory numerics = builderData.getNumerics();
        VariableType dim = numerics.newInt(firstNonSing);
        List<VariableType> newTypes = Arrays.asList(inputMatrixType, dim);

        ProviderData newData = ProviderData.newInstance(builderData, newTypes);

        if (numberOutputs == 1) {
            // return MinMaxDimDecInstance.newInstance(newTypes, minOrMax);
            return MinMaxDimDecInstance.newInstance(newData, minOrMax);
        }

        return MinMaxDimIndexDecInstance.newInstance(newData, minOrMax);
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        // See if we can use declared matrices
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        List<VariableType> originalTypes = builderData.getInputTypes();

        // We need 2 inputs ( matrix, #outputs )
        if (originalTypes.size() != 2) {
            return false;
        }

        // The first input is a numeric declared matrix
        VariableType firstInput = originalTypes.get(0);
        if (!MatrixUtils.isStaticMatrix(firstInput)) {
            return false;
        }

        // Check if it has a numeric element
        if (!ScalarUtils.hasScalarType(firstInput)) {
            return false;
        }

        // The second input is the number of outputs of the original function call ( needs to be 1 or 2 )
        VariableType secondInput = originalTypes.get(1);
        if (!TypeVerification.isIntegerConstant(secondInput)) {
            return false;
        }
        if (!TypeVerification.isIntegerConstantValid(secondInput, 1, 2)) {
            SpecsLogs.msgInfo("Error using " + minOrMax.getName() + "\nToo many output arguments.");
            return false;
        }

        return true;
    }

    @Override
    public InputsFilter getInputsFilter() {
        return new InputsFilter() {

            @Override
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

                /* We only need the first input, the matrix. The DIM will be coded in the
                 * instance and the number of outputs is only useful to call the correct instance. */
                inputArguments = Arrays.asList(inputArguments.get(0));

                return inputArguments;
            }
        };
    }

}
