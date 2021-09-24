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

package org.specs.CIRFunctions.Scalar;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.MatrixAlloc.TensorCreationUtils;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class ScalarFunctions extends AInstanceBuilder {

    private final FunctionInstance op;

    public ScalarFunctions(ProviderData data, FunctionInstance op) {
        super(data);

        this.op = op;
    }

    @Override
    public FunctionInstance create() {

        List<VariableType> inputTypes = getData().getInputTypes();

        // Name of the function
        String functionName = "scalar_matrix_" + op.getCName();

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();

        String scalarName = "scalar";
        inputNames.add(scalarName);

        String matrixName = "matrix";
        inputNames.add(matrixName);

        // FunctionTypes
        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, op.getFunctionType()
                .getCReturnType());

        CInstructionList body = new CInstructionList(functionTypes);

        // Condition: numel(matrix) != 1
        MatrixType matrixType = (MatrixType) inputTypes.get(1);
        CNode matrixVar = CNodeFactory.newVariable(matrixName, matrixType);
        CNode numelCall = getFunctionCall(matrixType.matrix().functions().numel(), matrixVar);

        CNode condition = getFunctionCall(COperator.NotEqual, numelCall, CNodeFactory.newCNumber(1));

        body.addInstruction(new TensorCreationUtils(getSettings()).getIfFailure(condition,
                "ERROR: Matrix dimensions must agree."));

        // Add 'return op(scalar, get(matrix, 1)'
        CNode getCall = getFunctionCall(MatrixFunction.GET, matrixVar, CNodeFactory.newCNumber(0));

        CNode scalarVar = CNodeFactory.newVariable(scalarName, inputTypes.get(0));
        body.addReturn(CNodeFactory.newFunctionCall(op, scalarVar, getCall));

        InstructionsInstance instance = new InstructionsInstance(functionName, CirFilename.SCALAR.getFilename(), body);

        return instance;
    }
}
