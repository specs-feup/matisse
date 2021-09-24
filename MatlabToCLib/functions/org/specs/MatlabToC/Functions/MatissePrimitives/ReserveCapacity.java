/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabToC.Functions.MatissePrimitives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.TemporaryUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.FunctionInputsNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ReserveCapacity extends AInstanceBuilder {

    private static final Checker CHECKER = new MatisseChecker()
            .numOfInputsAtLeast(2)
            .numOfOutputs(1)
            .ofType(MatrixType.class, 0)
            .areScalarFrom(1);

    private ReserveCapacity(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        MatrixType matrixType = getData().getInputType(MatrixType.class, 0);

        List<ScalarType> indexTypes = new ArrayList<>();
        List<String> indexNames = new ArrayList<>();

        for (int i = 1; i < getData().getNumInputs(); ++i) {
            ScalarType indexType = getData().getInputType(ScalarType.class, i);
            indexTypes.add(indexType);

            indexNames.add("index" + i);
        }

        MatrixType outputType = (MatrixType) getData().getOutputType();
        if (outputType == null) {
            outputType = DynamicMatrixType.newInstance(matrixType.matrix().getElementType());
        }

        FunctionTypeBuilder functionTypeBuilder = FunctionTypeBuilder
                .newWithSingleOutputAsInput();

        boolean referenceMode = matrixType instanceof DynamicMatrixType && matrixType.equals(outputType);

        if (referenceMode) {
            functionTypeBuilder.addReferenceInput("out", matrixType);
        } else {
            functionTypeBuilder.addInput("in", matrixType);
        }

        functionTypeBuilder
                .addInputs(indexNames, indexTypes)
                .addOutputAsInput("out", outputType);

        FunctionType functionType = functionTypeBuilder.build();
        String functionName = "MATISSE_reserve_capacity" + FunctionInstanceUtils.getTypesSuffix(functionType) + "_"
                + outputType.getSmallId();

        CNode inNode = CNodeFactory.newVariable("in", matrixType);
        CNode outNode = CNodeFactory.newVariable("out", outputType);

        CInstructionList body = new CInstructionList(functionType);

        if (referenceMode) {
            body.addAssignment(inNode, outNode);
        }

        List<CNode> indexNodes = new ArrayList<>();
        for (int i = 0; i < indexNames.size(); ++i) {
            CNode var = CNodeFactory.newVariable(indexNames.get(i), indexTypes.get(i));
            indexNodes.add(var);
        }

        InstanceProvider zerosProvider = MatlabBuiltin.ZEROS.getMatlabFunction();

        CNode returnOut = CNodeFactory.newReturn(outNode);

        if (outputType instanceof StaticMatrixType) {
            throw new NotImplementedException("Resizing static matrices");
        }

        StdlibFunctions stdlibFunctions = new StdlibFunctions(getNumerics());
        CNode abort = getFunctionCall(stdlibFunctions.abort());

        CNode freeIn = null;
        if (matrixType instanceof DynamicMatrixType) {
            if (!referenceMode) {
                freeIn = getFunctionCall(matrixType.matrix().functions().free(), inNode);
            }
        } else {
            freeIn = CNodeFactory.newComment("No need to free statically allocated matrix.");
        }

        List<CNode> canNotResizeErrorCase = new ArrayList<>();
        FunctionCallNode printErrorMessage = getFunctionCall(CStdioFunction.PRINTF,
                CNodeFactory.newString("In assignment A(I) = B, a matrix A can not be resized", 8));
        canNotResizeErrorCase.add(printErrorMessage);
        canNotResizeErrorCase.add(abort);

        List<CNode> largeEnoughCase = new ArrayList<>();
        if (matrixType instanceof DynamicMatrixType) {
            largeEnoughCase.add(CNodeFactory.newAssignment(outNode, inNode));
        } else {
            ProviderData createData = getData().createFromNodes(inNode);
            FunctionCallNode allocCall = outputType.matrix().functions()
                    .createFromMatrix()
                    .getCheckedInstance(createData)
                    .newFunctionCall(inNode);
            allocCall.getFunctionInputs().setInput(1, outNode);

            largeEnoughCase.add(allocCall);

            ProviderData copyData = getData().createFromNodes(inNode, outNode);
            largeEnoughCase.add(outputType.matrix().functions().copy()
                    .getCheckedInstance(copyData)
                    .newFunctionCall(inNode, outNode));
        }
        largeEnoughCase.add(returnOut);

        CNode ndims = getFunctionCall(matrixType.matrix().functions().numDims(), inNode);

        List<CNode> elements = new ArrayList<>();

        if (indexNodes.size() == 1) {

            CNode indexNode = indexNodes.get(0);

            List<CNode> zerosRowNodes = Arrays.asList(CNodeFactory.newCNumber(1), indexNode);
            ProviderData zerosRowData = getData().createFromNodes(zerosRowNodes);
            zerosRowData.setOutputType(outputType);
            FunctionCallNode allocateRowMatrix = zerosProvider.getCheckedInstance(zerosRowData).newFunctionCall(
                    zerosRowNodes);
            FunctionInputsNode allocateRowInputs = allocateRowMatrix.getFunctionInputs();
            assert TemporaryUtils.isTemporaryName(((VariableNode) allocateRowInputs.getChild(2)).getVariableName());
            allocateRowInputs.setInput(2, outNode);

            List<CNode> zerosColNodes = Arrays.asList(indexNode, CNodeFactory.newCNumber(1));
            ProviderData zerosColData = getData().createFromNodes(zerosColNodes);
            zerosColData.setOutputType(outputType);
            FunctionCallNode allocateColMatrix = zerosProvider.getCheckedInstance(zerosColData).newFunctionCall(
                    zerosColNodes);
            FunctionInputsNode allocateColInputs = allocateColMatrix.getFunctionInputs();
            assert TemporaryUtils.isTemporaryName(((VariableNode) allocateColInputs.getChild(2)).getVariableName());
            allocateColInputs.setInput(2, outNode);

            if (matrixType.matrix().usesDynamicAllocation()) {
                // If in is NULL, then we allocate a brand new row matrix.
                CNode inMatrixIsNull = CNodeFactory.newLiteral("in == NULL", getNumerics().newInt());
                List<CNode> nullCaseInstructions = new ArrayList<>();
                nullCaseInstructions.add(allocateRowMatrix);
                nullCaseInstructions.add(returnOut);
                body.addIf(inMatrixIsNull, nullCaseInstructions);
            }

            // in is not NULL.

            FunctionCallNode numelInNode = getFunctionCall(MatlabBuiltin.NUMEL.getMatlabFunction(), inNode);
            VariableType elementsType = numelInNode.getVariableType();
            assert ScalarUtils.isScalar(elementsType);

            CNode elementsNode = CNodeFactory.newVariable("elements", elementsType);
            body.addInstruction(CNodeFactory.newAssignment(elementsNode, numelInNode));
            elements.add(elementsNode);

            // Check if matrix is large enough.
            CNode largeEnoughCondition = getFunctionCall(COperator.LessThanOrEqual, indexNode, elementsNode);
            body.addIf(largeEnoughCondition, largeEnoughCase);

            // Maybe matrix is empty?
            CNode emptyCondition = getFunctionCall(COperator.Equal, elementsNode, CNodeFactory.newCNumber(0));
            List<CNode> emptyCase = new ArrayList<>();
            if (freeIn != null) {
                emptyCase.add(freeIn);
            }
            emptyCase.add(allocateRowMatrix);
            emptyCase.add(returnOut);
            body.addIf(emptyCondition, emptyCase);

            // Number of dimensions mustn't be greater than 2.
            CNode ndimsMoreThan2 = getFunctionCall(COperator.GreaterThan, ndims, CNodeFactory.newCNumber(2));
            body.addIf(ndimsMoreThan2, canNotResizeErrorCase);

            CNode size1 = getFunctionCall(matrixType.matrix().functions().getDim(), inNode, CNodeFactory.newCNumber(0));
            VariableType nRowsType = size1.getVariableType();
            CNode nRows = CNodeFactory.newVariable("nrows", nRowsType);
            body.addAssignment(nRows, size1);

            CNode size2 = getFunctionCall(matrixType.matrix().functions().getDim(), inNode, CNodeFactory.newCNumber(1));
            VariableType nColsType = size2.getVariableType();
            CNode nCols = CNodeFactory.newVariable("ncols", nColsType);
            body.addAssignment(nCols, size2);

            body.addComment("In case *out == in:");
            body.addLiteralInstruction("*out = NULL;");

            CNode moreThan1ColCondition = getFunctionCall(COperator.GreaterThan, nCols, CNodeFactory.newCNumber(1));
            CNode moreThan1RowCondition = getFunctionCall(COperator.GreaterThan, nRows, CNodeFactory.newCNumber(1));

            List<CNode> moreThan1RowCase = new ArrayList<>();
            moreThan1RowCase.add(IfNodes.newIfThen(moreThan1ColCondition, canNotResizeErrorCase));
            moreThan1RowCase.add(allocateColMatrix);
            List<CNode> just1RowCase = new ArrayList<>();
            just1RowCase.add(allocateRowMatrix);
            body.addInstruction(IfNodes.newIfThenElse(moreThan1RowCondition, moreThan1RowCase, just1RowCase));
        } else {
            ProviderData zerosInitializeData = getData().createFromNodes(indexNodes);
            zerosInitializeData.setOutputType(outputType);
            FunctionCallNode allocateInitializeMatrix = zerosProvider.getCheckedInstance(zerosInitializeData)
                    .newFunctionCall(
                            indexNodes);
            FunctionInputsNode allocateInitializeInputs = allocateInitializeMatrix.getFunctionInputs();
            assert TemporaryUtils.isTemporaryName(((VariableNode) allocateInitializeInputs.getChild(indexNodes.size()))
                    .getVariableName());
            allocateInitializeInputs.setInput(indexNodes.size(), outNode);

            List<CNode> allocateIndicesInstructions = new ArrayList<>();
            allocateIndicesInstructions.add(allocateInitializeMatrix);
            allocateIndicesInstructions.add(returnOut);

            if (matrixType.matrix().usesDynamicAllocation()) {
                // If in is NULL, then we allocate a brand new row matrix.
                CNode inMatrixIsNull = CNodeFactory.newLiteral("in == NULL", getNumerics().newInt());

                body.addIf(inMatrixIsNull, allocateIndicesInstructions);
            }

            // in is not NULL.

            for (int i = 0; i < indexTypes.size(); ++i) {
                CNode elementVariable = CNodeFactory.newVariable("elements" + (i + 1), getNumerics().newInt());
                elements.add(elementVariable);
            }

            ProviderData sizeData = getData()
                    .create(matrixType);
            sizeData.setNargouts(elements.size());

            FunctionCallNode size = MatlabBuiltin.SIZE.getMatlabFunction()
                    .getCheckedInstance(sizeData)
                    .newFunctionCall(inNode);
            FunctionInputsNode sizeInputs = size.getFunctionInputs();
            for (int i = 0; i < elements.size(); ++i) {
                assert TemporaryUtils.isTemporaryName(((VariableNode) sizeInputs.getInputs().get(i + 1))
                        .getVariableName());

                sizeInputs.setInput(i + 1, elements.get(i));
            }
            body.addInstruction(size);

            CNode inRangeCondition = null;
            CNode zerosCondition = null;

            for (int i = elements.size() - 1; i >= 0; --i) {
                CNode element = elements.get(i);

                CNode isInRange = getFunctionCall(COperator.LessThanOrEqual, indexNodes.get(i), element);
                CNode isZero = getFunctionCall(COperator.Equal, element, CNodeFactory.newCNumber(0));

                if (inRangeCondition == null) {
                    assert zerosCondition == null;

                    inRangeCondition = isInRange;
                    zerosCondition = isZero;
                } else {
                    assert zerosCondition != null;

                    inRangeCondition = getFunctionCall(COperator.LogicalAnd, isInRange, inRangeCondition);
                    zerosCondition = getFunctionCall(COperator.LogicalOr, isZero, zerosCondition);
                }
            }

            assert inRangeCondition != null;
            assert zerosCondition != null;

            body.addIf(inRangeCondition, largeEnoughCase);

            body.addIf(zerosCondition, allocateIndicesInstructions);

            body.addIf(
                    getFunctionCall(COperator.GreaterThan, ndims, CNodeFactory.newCNumber(elements.size())),
                    canNotResizeErrorCase);

            body.addComment("In case *out == in:");
            body.addLiteralInstruction("*out = NULL;");

            // Perform actual allocation

            List<CNode> allocationInputs = new ArrayList<>();
            for (int i = 0; i < elements.size(); ++i) {
                CNode max = getFunctionCall(MathFunction.MAX.getMatlabFunction(), indexNodes.get(i), elements.get(i));
                allocationInputs.add(max);
            }

            ProviderData newAllocationData = getData()
                    .createFromNodes(allocationInputs);
            newAllocationData.setOutputType(outputType);

            FunctionCallNode newAllocation = MatlabBuiltin.ZEROS
                    .getMatlabFunction()
                    .getCheckedInstance(newAllocationData).newFunctionCall(allocationInputs);
            FunctionInputsNode newAllocationInputs = newAllocation.getFunctionInputs();
            assert TemporaryUtils.isTemporaryName(((VariableNode) newAllocationInputs.getInputs().get(
                    allocationInputs.size())).getVariableName());
            newAllocationInputs.setInput(allocationInputs.size(), outNode);

            body.addInstruction(newAllocation);
        }

        body.addComment("Copy old elements");

        assert indexTypes.size() == elements.size();

        List<VariableNode> inductionVars = new ArrayList<>();
        for (int i = 0; i < indexTypes.size(); ++i) {
            VariableNode inductionVar = CNodeFactory.newVariable("i" + (i + 1), getNumerics().newInt());
            inductionVars.add(inductionVar);
        }

        List<CNode> getArguments = new ArrayList<>();
        getArguments.add(inNode);
        getArguments.addAll(inductionVars);
        CNode getNode = getFunctionCall(matrixType.matrix().functions().get(), getArguments);

        List<CNode> setArguments = new ArrayList<>();
        setArguments.add(outNode);
        setArguments.addAll(inductionVars);
        setArguments.add(getNode);
        CNode copyElement = getFunctionCall(outputType.matrix().functions().set(), setArguments);

        CNode currentNode = copyElement;
        for (int i = elements.size() - 1; i >= 0; --i) {
            currentNode = new ForNodes(getData()).newForLoopBlock(inductionVars.get(i), elements.get(i), currentNode);
        }

        body.addInstruction(currentNode);

        if (freeIn != null) {
            body.addInstruction(freeIn);
        }

        body.addInstruction(returnOut);

        return new InstructionsInstance(functionName, CirFilename.ALLOCATED.getFilename(), body);
    }

    public static InstanceProvider getProvider() {
        return new MatlabInstanceProviderHelper(ReserveCapacity.CHECKER, data -> new ReserveCapacity(data).create());
    }
}
