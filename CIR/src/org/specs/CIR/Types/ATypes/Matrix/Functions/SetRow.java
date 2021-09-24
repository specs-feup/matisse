/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Types.ATypes.Matrix.Functions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixResource;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Builder for SetRow.
 * 
 * <p>
 * This class uses old idioms and should not be taken as example, it needs refactoring. However, refactoring was not
 * done yet because this class is not supposed to be used, it only here for compatibility with old code until it is
 * confirmed that can be replaced (in this case, for transformation MultipleSetToFor).
 * 
 * @author JoaoBispo
 *
 */
public class SetRow extends AInstanceBuilder {

    public SetRow(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        // Should have only four arguments
        if (getData().getInputTypes().size() != 4) {
            throw new RuntimeException("Expecting 4 arguments:" + getData().getInputTypes());
        }

        // First argument is of type matrix
        MatrixType inputMatrix = getTypeAtIndex(MatrixType.class, 0);

        // Fourth argument is of type matrix
        MatrixType valuesMatrix = getTypeAtIndex(MatrixType.class, 3);

        return newSetRow(inputMatrix, valuesMatrix, getData());
    }

    private FunctionInstance newSetRow(VariableType inputMatrix, VariableType valuesMatrix, ProviderData data) {

        // Name of the function
        String functionName = getFunctionName("set_row", Arrays.asList(inputMatrix, valuesMatrix));

        // String functionName = IdUtils.getImplementation(inputMatrix) + "_set_row_" + IdUtils.getType(inputMatrix) +
        // "_"
        // + IdUtils.getType(valuesMatrix);

        // Input names
        String matrixName = "m";
        String offset = "offset";
        String elements = "elements";
        String valuesName = "values";

        List<String> inputNames = Arrays.asList(matrixName, offset, elements, valuesName);

        // Input types
        VariableType intType = data.getNumerics().newInt();
        List<VariableType> inputTypes = Arrays.asList(inputMatrix, intType, intType, valuesMatrix);

        // FunctionTypes
        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        // Build body
        String resource = MatrixResource.SET_ROW_BODY.getResource();
        Replacer cBody = new Replacer(SpecsIo.getResource(resource));

        // Create numel call
        CNode valuesVar = CNodeFactory.newVariable(valuesName, valuesMatrix);

        CNode numelCall = getFunctionCall(((MatrixType) valuesMatrix).matrix().functions().numel(), valuesVar);

        cBody.replace("<NUMELS_VALUES_CALL>", numelCall.getCode());

        // Get values->0
        CNode zero = CNodeFactory.newCNumber(0);
        CNode getVZero = getFunctionCall(MatrixFunction.GET, valuesVar, zero);

        // Set M(i)->values(0)
        CNode inputsVar = CNodeFactory.newVariable(matrixName, inputMatrix);
        CNode inductionVar = CNodeFactory.newVariable("i", intType);
        CNode offsetVar = CNodeFactory.newVariable(offset, intType);

        CNode iPlusOffset = getFunctionCall(COperator.Addition, inductionVar, offsetVar);

        CNode setMZero = getFunctionCall(MatrixFunction.SET, inputsVar, iPlusOffset, getVZero);

        cBody.replace("<SET_M_IOFFSET_VALUES0>", setMZero.getCode());

        // Get values->i
        CNode getVI = getFunctionCall(MatrixFunction.GET, valuesVar, inductionVar);

        // Set M(i)->values(i)
        // CToken setMI = FunctionUtils.getFunctionCall(MatrixProvider.SET, inputsVar, iPlusOffset,
        CNode setMI = getFunctionCall(MatrixFunction.SET, inputsVar, iPlusOffset, getVI);

        cBody.replace("<SET_M_IOFFSET_VALUESI>", setMI.getCode());

        LiteralInstance setRowInstance = new LiteralInstance(functionTypes, functionName,
                CirFilename.MATRIX.getFilename(), cBody.toString());

        // Set includes
        setRowInstance.setCustomImplementationIncludes(SystemInclude.Stdlib, SystemInclude.Stdio);

        // Set instance
        setRowInstance.getCustomImplementationInstances().add(numelCall, getVZero, setMZero, getVI, setMI);

        return setRowInstance;

    }
}
