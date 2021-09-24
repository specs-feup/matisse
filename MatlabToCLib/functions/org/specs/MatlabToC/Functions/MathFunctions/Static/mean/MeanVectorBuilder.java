/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MathFunctions.Static.mean;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

/**
 * Builder for the Maltab built-in function 'mean' for the cases where the input is a numeric vector. </br>
 * </br>
 * <b>Example call:</b>
 * 
 * <pre>
 * {@code
 * o = mean(v),
 * where v is a vector
 * }
 * </pre>
 * 
 * @author Pedro Pinto
 * 
 */
public class MeanVectorBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

        if (!checkRule(builderData)) {
            return null;
        }

        FunctionType functionType = getType(builderData);
        String functionName = "mean_vec" + FunctionInstanceUtils.getTypesSuffix(functionType.getArgumentsTypes());
        String filename = MatlabCFilename.MatrixMath.getCFilename();

        CInstructionList body = new CInstructionList(functionType);

        Variable inputMatrix = functionType.getInputVar("matrix");

        CNode inputMatrixNode = CNodeFactory.newVariable(inputMatrix);

        InstanceProvider sumProvider = MathFunction.SUM.getMatlabFunction();
        ProviderData sumData = builderData.createFromNodes(inputMatrixNode);
        CNode sumNode = CNodeFactory.newFunctionCall(sumProvider.getCheckedInstance(sumData), inputMatrixNode);

        InstanceProvider numelProvider = MatlabBuiltin.NUMEL.getMatlabFunction();
        ProviderData numelData = builderData.createFromNodes(inputMatrixNode);
        CNode numelNode = CNodeFactory.newFunctionCall(numelProvider.getCheckedInstance(numelData), inputMatrixNode);

        InstanceProvider divisionProvider = MatlabOp.LeftDivision.getMatlabFunction();
        ProviderData divisionData = builderData.createFromNodes(sumNode, numelNode);
        CNode divisionNode = CNodeFactory.newFunctionCall(divisionProvider.getCheckedInstance(divisionData), sumNode,
                numelNode);

        body.addReturn(divisionNode);

        return new InstructionsInstance(functionType, functionName, filename, body);
    }

    @Override
    public FunctionType getType(ProviderData builderData) {
        if (!checkRule(builderData)) {
            return null;
        }

        MatrixType matrix = builderData.getInputType(MatrixType.class, 0);
        ScalarType returnType = builderData.getNumerics().newDouble();

        return FunctionTypeBuilder.newSimple()
                .addInput("matrix", matrix)
                .returning(returnType)
                .build();
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        // FunctionSettings settings = builderData.getFunctionSettings();
        List<VariableType> originalTypes = builderData.getInputTypes();

        // Need to have exactly 1 input
        if (originalTypes.size() != 1) {
            return false;
        }

        VariableType inputType = originalTypes.get(0);
        if (!(inputType instanceof MatrixType)) {
            return false;
        }

        // This input needs to be a numeric vector
        MatrixType matrix = (MatrixType) inputType;

        if (!matrix.getTypeShape().isKnown1D()) {
            return false;
        }

        if (!ScalarUtils.isScalar(MatrixUtils.getElementType(matrix))) {
            return false;
        }

        return true;
    }
}
