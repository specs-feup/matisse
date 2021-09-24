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

package org.specs.CIRFunctions.MatrixDec;

import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getFilename;
import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getInputName;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class MatrixColFunction extends AInstanceBuilder {

    public MatrixColFunction(ProviderData data) {
        super(data);
    }

    public FunctionType getFunctionType() {
        List<MatrixType> inputMatrices = getData()
                .getInputTypes(getInputNames(getData().getNumInputs()), MatrixType.class);

        return newFunctionTypes(inputMatrices);
    }

    /**
     * @param elementType
     * @param numMatrices
     * @return
     */
    @Override
    public FunctionInstance create() {
        List<MatrixType> inputMatrices = getData()
                .getInputTypes(getInputNames(getData().getNumInputs()), MatrixType.class);

        // Verify matrices sizes
        verifyInputs(inputMatrices);

        // Name of the function
        String functionName = newFunctionName(inputMatrices);

        // Build FunctionTypes
        FunctionType fTypes = newFunctionTypes(inputMatrices);

        CInstructionList insts = new CInstructionList(fTypes);

        // Int type
        VariableType intType = getNumerics().newInt();

        // Initialize offset
        CNode offsetVar = CNodeFactory.newVariable("currentOffset", intType);
        // CToken zeroToken = CToken.build(0);
        CNode zeroToken = CNodeFactory.newCNumber(0, intType);

        // Code: 'currentOffset = 0;'
        insts.addAssignment(offsetVar, zeroToken);

        // Variable indunctionVar = new Variable("i", intType);
        VariableNode colVar = CNodeFactory.newVariable("col", intType);
        VariableNode outPosVar = CNodeFactory.newVariable("outPos", intType);
        VariableNode rowVar = CNodeFactory.newVariable("row", intType);

        insts.addAssignment(outPosVar, CNodeFactory.newCNumber(0));

        Variable out = fTypes.getReturnVar();
        MatrixType outType = (MatrixType) out.getType();

        ForNodes forNodes = new ForNodes(getData());

        int numCols = out.getType().getTypeShape().getDim(1);

        CInstructionList outerLoop = new CInstructionList();

        int numInputs = inputMatrices.size();
        for (int inputId = 0; inputId < numInputs; ++inputId) {
            String inputName = fTypes.getCInputNames().get(inputId);
            MatrixType inputType = inputMatrices.get(inputId);

            CNode inputRows = FunctionInstanceUtils.getFunctionCall(
                    inputType.functions().getDim(),
                    getData(),
                    CNodeFactory.newVariable(inputName, inputType),
                    CNodeFactory.newCNumber(0));

            CNode offset = FunctionInstanceUtils.getFunctionCall(COperator.Multiplication,
                    getData(),
                    colVar,
                    CNodeFactory.newCNumber(inputType.getTypeShape().getDim(0)));

            CInstructionList innerLoop = new CInstructionList();

            CNode getIndex = FunctionInstanceUtils.getFunctionCall(COperator.Addition,
                    getData(),
                    rowVar,
                    offset);

            CNode value = FunctionInstanceUtils.getFunctionCall(
                    inputType.functions().get(),
                    getData(),
                    CNodeFactory.newVariable(inputName, inputType),
                    getIndex);

            innerLoop.addInstruction(FunctionInstanceUtils.getFunctionCall(
                    outType.functions().set(),
                    getData(),
                    CNodeFactory.newVariable(out),
                    outPosVar,
                    value));

            innerLoop.addAssignment(outPosVar,
                    FunctionInstanceUtils.getFunctionCall(
                            COperator.Addition,
                            getData(),
                            outPosVar,
                            CNodeFactory.newCNumber(1)));

            CNode innerFor = forNodes.newForLoopBlock(rowVar, inputRows, innerLoop.get());
            outerLoop.addInstruction(innerFor);
        }

        insts.addInstruction(forNodes.newForLoopBlock(colVar, CNodeFactory.newCNumber(numCols), outerLoop.get()));

        insts.addReturn(CNodeFactory.newVariable(out));

        FunctionInstance instance = new InstructionsInstance(fTypes, functionName, getFilename(), insts);

        return instance;
    }

    /**
     * @param inputMatrices
     * @return
     */
    private static FunctionType newFunctionTypes(List<MatrixType> inputMatrices) {
        // Get number of matrices
        int numMatrices = inputMatrices.size();

        List<String> inputNames = getInputNames(numMatrices);

        // Output
        String outputName = "out";

        // Output type has the same number of columns and as many rows as the sum of the rows of the given matrices
        VariableType elementType = MatrixUtils.getElementType(inputMatrices.get(0));
        int numCols = inputMatrices.get(0).getTypeShape().getDim(1);
        int numRows = 0;
        for (VariableType matrixType : inputMatrices) {
            int numRowsMatrix = MatrixUtils.getShapeDims(matrixType).get(0);
            numRows += numRowsMatrix;
        }

        VariableType outType = VariableTypeFactory.newDeclaredMatrix(elementType, numRows, numCols);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputMatrices, outputName,
                outType);

        return fTypes;
    }

    private static List<String> getInputNames(int numMatrices) {
        // Build input names
        String inputPrefix = "value";

        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numMatrices; i++) {
            String inputName = getInputName(inputPrefix, i);
            inputNames.add(inputName);
        }
        return inputNames;
    }

    /**
     * Verifies if all input matrices have the same number of columns.
     * 
     * @param inputMatrices
     */
    private static void verifyInputs(List<MatrixType> inputMatrices) {
        // Get the number of columns of the first matrix
        Integer numCols = inputMatrices.get(0).getTypeShape().getDim(1);

        for (int i = 1; i < inputMatrices.size(); i++) {
            Integer numColsSecond = inputMatrices.get(i).getTypeShape().getDim(1);
            if (!numCols.equals(numColsSecond)) {
                throw new RuntimeException("Input " + (i + 1) + "has a different number of columns (" + numColsSecond
                        + ") that first input (" + numCols + ")");
            }
        }

    }

    /**
     * The function is characterized by the element type and the sizes of the input matrices.
     * 
     * @param inputMatrices
     * @return
     */
    private static String newFunctionName(List<MatrixType> inputMatrices) {

        StringBuilder builder = new StringBuilder();

        builder.append("new_col_array_");

        // Get element type
        VariableType elementType = MatrixUtils.getElementType(inputMatrices.get(0));
        builder.append(elementType.getSmallId());

        for (MatrixType inputMatrix : inputMatrices) {
            String shape = inputMatrix.getTypeShape().getString();
            builder.append("_");
            builder.append(shape);
        }

        return builder.toString();
    }

}
