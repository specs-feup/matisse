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

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.LibraryFunctions.CLibraryAvoidable;
import org.specs.CIRFunctions.LibraryFunctions.LibraryFunctions;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;

import pt.up.fe.specs.util.SpecsLogs;

public class MatrixCopy extends AInstanceBuilder {

    private final static String VARNAME_SOURCE = "source_matrix";
    private final static String VARNAME_TARGET = "target_matrix";

    public MatrixCopy(ProviderData data) {
        super(data);
    }

    /**
     * Creates an instance that copies the elements of a matrix to another.
     * 
     * <p>
     * Only copies the elements, does not try to allocate memory.
     * 
     * <p>
     * Inputs: <br>
     * - A matrix, from where the value will be read;<br>
     * - A matrix, whose values will be set;<br>
     */
    @Override
    public FunctionInstance create() {

        MatrixType inputType = getData().getInputType(MatrixType.class, 0);
        MatrixType outputType = getData().getInputType(MatrixType.class, 1);

        // EXPERIMENT: If only static matrices, use memcpy
        // List<MatrixType> inputTypes = getData().getInputTypes(MatrixType.class);
        // if (inputTypes.stream()
        // .filter(matrixType -> matrixType.matrix().usesDynamicAllocation())
        // .count() == 0) {
        if (!inputType.matrix().usesDynamicAllocation() && !outputType.matrix().usesDynamicAllocation()) {
            // Check arrays have the same type and the same size
            if (inputType.getSmallId().equals(outputType.getSmallId())
                    && inputType.matrix().getElementType().canBeAssignmentCopied()) {
                if (!getSettings().get(CirKeys.AVOID).avoid(CLibraryAvoidable.MEMCPY)) {
                    SpecsLogs.msgLib("Should use Memcpy more generally?");
                    return LibraryFunctions.newMemcpyDec(inputType, outputType).getCheckedInstance(getData());
                }
            }
        }

        FunctionType functionTypes = newFunctionTypes();

        CInstructionList cBody = newInstructions(functionTypes);

        String functionName = "copy_" + functionTypes.getCInputTypes().get(0).getSmallId() + "_"
                + functionTypes.getCInputTypes().get(1).getSmallId();

        String cFilename = "lib/matrix";

        InstructionsInstance instance = new InstructionsInstance(functionName, cFilename, cBody);

        return instance;

    }

    private FunctionType newFunctionTypes() {
        MatrixType sourceMatrix = getTypeAtIndex(MatrixType.class, 0);
        MatrixType targetMatrix = getTypeAtIndex(MatrixType.class, 1);

        return FunctionType.newInstanceWithOutputsAsInputs(Arrays.asList(MatrixCopy.VARNAME_SOURCE),
                Arrays.asList(sourceMatrix),
                MatrixCopy.VARNAME_TARGET, targetMatrix);
    }

    private CInstructionList newInstructions(FunctionType functionTypes) {
        CInstructionList cBody = new CInstructionList(functionTypes);

        MatrixType sourceMatrix = functionTypes.getInput(MatrixType.class, 0);
        if (sourceMatrix instanceof DynamicMatrixType) {
            // Remove shape information
            sourceMatrix = DynamicMatrixType.newInstance(sourceMatrix.matrix().getElementType());
        }

        MatrixType targetMatrix = functionTypes.getOutput(MatrixType.class, 0);

        VariableNode inductionToken = CNodeFactory.newVariable("i", getNumerics().newInt());
        VariableNode sourceToken = CNodeFactory.newVariable(MatrixCopy.VARNAME_SOURCE, sourceMatrix);
        VariableNode targetToken = CNodeFactory.newVariable(MatrixCopy.VARNAME_TARGET, targetMatrix);

        CNode endValue = getFunctionCall(sourceMatrix.matrix().functions().numel(), sourceToken);

        CNode getToken = getFunctionCall(sourceMatrix.matrix().functions().get(), sourceToken, inductionToken);
        CNode setToken = getFunctionCall(targetMatrix.matrix().functions().set(), targetToken, inductionToken,
                getToken);

        CNode forToken = (new ForNodes(getSettings())).newForLoopBlock(inductionToken, endValue, setToken);

        cBody.addInstruction(forToken);

        cBody.addReturn(targetToken);

        return cBody;
    }

    public static InstanceProvider getProvider() {
        return (ProviderData data) -> new MatrixCopy(data).create();
    }
}
