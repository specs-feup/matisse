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

package org.specs.CIRFunctions.MatrixAlloc;

import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getDimNames;
import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getFilename;
import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getInputName;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.LowLevel.CreateHelper;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Contains functions to create allocated matrixes.
 * 
 * @author Joao Bispo
 * @deprecated should move functionality to classes in package org.specs.CIRTypes.Types.DynamicMatrix.Functions
 */
@Deprecated
public class TensorCreationFunctions extends CirBuilder {

    // Move to function related with get_row_view (it needs to be created)
    private static final String GET_ROW_VIEW_POINTER_PREFIX = "get_row_view_pointer_";

    private final TensorFunctions tensorFunctions;
    private final ProviderData data;

    public static String getRowViewPointerPrefix() {
        return TensorCreationFunctions.GET_ROW_VIEW_POINTER_PREFIX;
    }

    /**
     * @param data
     */
    public TensorCreationFunctions(ProviderData data) {
        super(data);

        tensorFunctions = new TensorFunctions(data);
        this.data = data;
    }

    private ProviderData getData() {
        return data;
    }

    /**
     * 
     * New instance of a function which creates an 2D empty allocated matrix of type double. Returns the created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * (nothing)
     * 
     * @param tensorType
     * @param prefixName
     * @param setValue
     * @param numDims
     * @return
     */
    FunctionInstance newEmptyArrayAlloc() {

        int numDims = 2;

        // Element type
        // VariableType elementType = VariableTypeFactory.newDouble();
        VariableType elementType = getNumerics().newDouble();

        // Tensor type
        VariableType tensorType = DynamicMatrixType.newInstance(elementType, Arrays.asList(0, 0));

        // Name of the function
        String functionName = "empty_" + elementType.getSmallId();

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(null, null, "t", tensorType);

        // Get used instances
        FunctionInstance newTensorFunction = newArray(elementType, numDims);

        StringBuilder builder = new StringBuilder();

        // new_tensor_d(0, 0, t);
        builder.append(newTensorFunction.getCName());
        builder.append("(0, 0, t);\n");

        builder.append("\nreturn *t;\n");

        // Create instance
        LiteralInstance newSetTensor = new LiteralInstance(fTypes, functionName, getFilename(), builder.toString());

        // Set dependent instances
        newSetTensor.getCustomImplementationInstances().add(newTensorFunction);

        return newSetTensor;
    }

    private static final ThreadSafeLazy<String> newArrayHelperResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TensorCreationResource.NEW_ARRAY_BODY));

    /**
     * Creates a new instance of the function 'new_empty_tensor', which creates a new tensor structure. Returns an
     * allocated matrix with the size specified by the inputs and with data elements of the given 'tensorType'.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of integers, representing the size of each dimension of the matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * @return
     */
    public FunctionInstance newArray(VariableType elementType, int numDims) {

        String functionName = "new_array_" + elementType.getSmallId() + "_" + numDims;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            String inputName = getInputName(i);
            inputNames.add(inputName);
        }

        // Input types
        // VariableType intType = VariableTypeFactoryOld.newNumeric(NumericType.Cint);
        VariableType intType = getNumerics().newInt();
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            inputTypes.add(intType);
        }

        // FunctionTypes
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "t", tensorType);

        // Build body
        String cBody = TensorCreationFunctions.newArrayHelperResource.getValue();

        cBody = cBody.replace("<NUM_DIMS>", Integer.toString(numDims));
        // cBody = cBody.replace("<DIM_NAMES>", getDimNames(numDims));
        cBody = cBody.replace("<SHAPE_INIT>", getShapeInit(numDims));

        FunctionInstance helperFunction = newArrayHelper(elementType);
        cBody = cBody.replace("<CALL_NEW_HELPER>", helperFunction.getCName());

        LiteralInstance newEmptyTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        // Set instance
        newEmptyTensor.getCustomImplementationInstances().add(helperFunction);

        return newEmptyTensor;
    }

    private static CharSequence getShapeInit(int numDims) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < numDims; i++) {
            // shape[0] = dim_1;
            if (i != 0) {
                builder.append("\t");
            }
            String dimVariableName = "dim_" + (i + 1);
            builder.append("shape[" + i + "] = " + dimVariableName + " > 0 ? " + dimVariableName + ": 0;\n");

        }

        return builder.toString();
    }

    /**
     * Creates a new instance of the function 'new_array', which creates a new tensor structure. Returns an allocated
     * matrix with the size specified by the input matrix and with data elements of the given 'output' matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An integer matrix which represents the shape of the new matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * @param elementType
     * 
     * @return
     */
    // public static FunctionInstance newArrayFromMatrix(VariableType inputMatrixType,
    public InstanceProvider newArrayFromMatrix(final VariableType elementType) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return new TensorCreationFunctions(data).newArrayFromMatrixInstance(elementType);
            }
        };
    }

    private static final ThreadSafeLazy<String> newArrayFromMatrixBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TensorCreationResource.NEW_ARRAY_FROM_MATRIX_BODY));

    private FunctionInstance newArrayFromMatrixInstance(VariableType elementType) {

        MatrixType inputMatrixType = (MatrixType) getData().getInputTypes().get(0);

        String functionName = "new_array_" + inputMatrixType.getSmallId() + "_" + elementType.getSmallId();

        String inputName = "shape";
        String outputName = "t";

        TypeShape outputShape = TypeShape.newUndefinedShape();
        if (inputMatrixType.getTypeShape().isFullyDefined()) {
            List<Integer> dims = inputMatrixType.getTypeShape().getDims();
            if (dims.size() == 2 && dims.get(0) == 1 && dims.get(1) >= 2) {
                // If dims.get(1) > 2, then we don't really know the size.
                // But dims shape only represents the maximum number of dimensions anyway, so it's not a problem.
                outputShape = TypeShape.newDimsShape(dims.get(1));
            }
        }
        VariableType outputType = DynamicMatrixType.newInstance(elementType, outputShape);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(Arrays.asList(inputName),
                Arrays.asList(inputMatrixType), outputName, outputType);

        // Build body
        String cBody = TensorCreationFunctions.newArrayFromMatrixBodyResource.getValue();

        CNode inputMatrix = CNodeFactory.newVariable(inputName, inputMatrixType);

        // CToken numel = getHelper().getFunctionCall(MatrixFunction.NUMEL, inputMatrix);
        FunctionCallNode numel = getFunctionCall(((MatrixType) inputMatrixType).matrix().functions().numel(),
                inputMatrix);
        cBody = cBody.replace("<CALL_NUMEL_SHAPE>", numel.getCode());

        // CToken iterator = CTokenFactory.newVariable("i", VariableTypeFactory.newInt());
        CNode iterator = CNodeFactory.newVariable("i", getNumerics().newInt());
        FunctionCallNode get = getFunctionCall(MatrixFunction.GET, inputMatrix, iterator);
        cBody = cBody.replace("<CALL_GET_I>", get.getCode());

        FunctionInstance helperFunction = newArrayHelper(elementType);
        cBody = cBody.replace("<CALL_NEW_HELPER>", helperFunction.getCName());

        LiteralInstance newEmptyTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        // Set instance
        newEmptyTensor.getCustomImplementationInstances().add(numel.getFunctionInstance(), get.getFunctionInstance(),
                helperFunction);

        return newEmptyTensor;
    }

    /**
     * Creates a new instance of the function 'new_array', which creates a new tensor structure. Returns an allocated
     * matrix with the size specified by the inputs and with data elements of the given 'tensorType'.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A pointer to integer, representing the shape of the matrix;<br>
     * - An integer, with the number of dimensions of the matrix; <br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * @return
     */
    public FunctionInstance newArrayHelper(VariableType elementType) {

        return new CreateHelper(getData(), (ScalarType) elementType).create();
    }

    private static final ThreadSafeLazy<String> newConstArrayBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TensorCreationResource.NEW_CONST_ARRAY_BODY));

    /**
     * Creates a new instance of a function which creates and sets all the positions of the matrix to the given value.
     * Returns the created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of integers, representing the size of each dimension of the matrix;<br>
     * - A value of element type, representing the value that will be set in the matrix positions;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * @param tensorType
     * @param prefixName
     * @param numDims
     * @return
     */
    FunctionInstance newConstantArray(VariableType elementType, int numDims, ProviderData pdata) {

        // Name of the function
        String functionName = "new_const_array_" + elementType.getSmallId() + numDims;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            String inputName = getInputName(i);
            inputNames.add(inputName);
        }

        inputNames.add("value");

        // Input types
        // VariableType intType = VariableTypeFactoryOld.newNumeric(NumericType.Cint);
        VariableType intType = getNumerics().newInt();
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            inputTypes.add(intType);
        }

        inputTypes.add(elementType);

        // FunctionTypes
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "t", tensorType);

        // Get body of function
        String cBody = TensorCreationFunctions.newConstArrayBodyResource.getValue();

        // cBody = cBody.replace("<NUM_DIMS>", Integer.toString(numDims));
        cBody = cBody.replace("<DIM_NAMES>", getDimNames(numDims));

        FunctionInstance newTensorInstance = newArray(elementType, numDims);
        cBody = cBody.replace("<CALL_NEW>", newTensorInstance.getCName());

        // FunctionInstance setMatrixInstance = TensorProvider.SET_MATRIX_VALUES
        // .getInstance(tensorType);
        ProviderData setData = ProviderData.newInstance(pdata, tensorType);
        FunctionInstance setMatrixInstance = TensorProvider.SET_MATRIX_VALUES.newCInstance(setData);
        cBody = cBody.replace("<CALL_SET_MATRIX>", setMatrixInstance.getCName());

        // Create instance
        LiteralInstance newSetTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        // Set includes
        newSetTensor.setCustomImplementationIncludes(Arrays.asList(SystemInclude.Stdlib.getIncludeName()));

        // Set dependent instances
        newSetTensor.getCustomImplementationInstances().add(newTensorInstance, setMatrixInstance);

        return newSetTensor;
    }

    /**
     * Creates a new instance of the function 'new_array', which creates a matrix with the given values. Returns the
     * created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of values of element type, representing the values of the row matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * @param tensorType
     * @param prefixName
     * @param numElements
     * @return
     */
    FunctionInstance newArrayWithValues(VariableType elementType, List<Integer> shape) {

        TypeShape matrixShape = TypeShape.newInstance(shape);

        // Name of the function
        String functionName = "new_array_" + elementType.getSmallId() + "_" + matrixShape.getString();

        // Input names
        String inputPrefix = "value";

        // int numElements = MatrixUtils.getMatrixLength(shape);
        int numElements = matrixShape.getNumElements();

        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numElements; i++) {
            String inputName = getInputName(inputPrefix, i);
            inputNames.add(inputName);
        }

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numElements; i++) {
            inputTypes.add(elementType);
        }

        // Output
        String tensorName = "t";
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, tensorName,
                tensorType);

        // Start instruction list
        CInstructionList insts = new CInstructionList(fTypes);

        // Get body of function
        // String cBody = IoUtils.getResource(TensorCreationResource.ARRAY_WITH_VALUES);

        int numDims = shape.size();
        FunctionInstance newTensorInstance = newArray(elementType, numDims);

        // Build arguments with shape
        List<CNode> newArrayArgs = CNodeUtils.buildIntegerTokens(shape);
        // Add tensor variable
        CNode tensorPointerVar = CNodeFactory.newVariable(fTypes.getReturnVar());
        newArrayArgs.add(tensorPointerVar);

        // Add instruction that creates array
        insts.addFunctionCall(newTensorInstance, newArrayArgs);

        // Add blank line
        insts.addLiteralInstruction("");

        // Add tensor variable
        // CToken newTensorInstance.newFunctionCall(newArrayArgs);

        // cBody = cBody.replace("<CALL_NEW>", newTensorInstance.getCName());

        // cBody = cBody.replace("<NUM_VALUES>", Integer.toString(numElements));

        // int numDims = 2;
        // FunctionInstance newTensorInstance = newArray(elementType, numDims);
        // cBody = cBody.replace("<CALL_NEW>", newTensorInstance.getCName());

        FunctionInstance setInstance = tensorFunctions.newSetInline(elementType, 1);

        CNode tensorArg = CNodeFactory.newVariable(tensorName, fTypes.getCInputTypes().get(numElements));

        // StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numElements; i++) {
            CNode indexArg = CNodeFactory.newCNumber(i);
            CNode inputArg = CNodeFactory.newVariable(getInputName(inputPrefix, i), elementType);

            List<CNode> args = Arrays.asList(tensorArg, indexArg, inputArg);

            // Add set
            insts.addFunctionCall(setInstance, args);
            // CToken setCall = setInstance.newFunctionCall(args);

            // builder.append(CodeGeneratorUtils.tokenCode(setCall));
            // builder.append(";\n ");
        }

        // Add return of tensor
        insts.addReturn(tensorPointerVar);

        // cBody = cBody.replace("<CALL_SET_LIST>", builder.toString());

        // Create instance
        InstructionsInstance newArrayWithValues = new InstructionsInstance(fTypes, functionName, getFilename(), insts);
        // LiteralInstance newSetTensor = new LiteralInstance(fTypes, functionName, getFilename(),
        // cBody);
        // InstructionsInstance
        // Set dependent instances
        // newSetTensor.setCustomImplementationInstances(newTensorInstance, setInstance);

        // return newSetTensor;
        return newArrayWithValues;
    }

    /**
     * Creates a new instance of a function which creates a diagonal matrix with the values set to 1. Returns the
     * created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of integers, representing the size of each dimension of the matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * @param tensorType
     * @param prefixName
     * @param numDims
     * @return
     */
    FunctionInstance newEyeArray(VariableType elementType, int numDims, ProviderData pdata) {

        // Name of the function
        String functionName = "eye_" + elementType.getSmallId() + "_" + numDims;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            String inputName = getInputName(i);
            inputNames.add(inputName);
        }

        // Input types
        // VariableType intType = VariableTypeFactoryOld.newNumeric(NumericType.Cint);
        VariableType intType = getNumerics().newInt();
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numDims; i++) {
            inputTypes.add(intType);
        }

        // FunctionTypes
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "t", tensorType);

        // Get body of function
        String cBody = SpecsIo.getResource(TensorCreationResource.EYE_BODY);

        cBody = cBody.replace("<NUM_DIMS>", Integer.toString(numDims));
        cBody = cBody.replace("<DIM_NAMES>", getDimNames(numDims));

        cBody = cBody.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        cBody = cBody.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);

        FunctionInstance newTensorInstance = newConstantArray(elementType, numDims, pdata);
        cBody = cBody.replace("<CALL_NEW>", newTensorInstance.getCName());

        // FunctionInstance setInstance = TensorProvider.SET_VALUE.getInstance(Arrays
        // .asList(tensorType));
        // FunctionInstance setInstance = TensorFunctions.newSetLinear(tensorType);
        // FunctionInstance setInstance = TensorFunctions.newSet(tensorType, 1);
        // FunctionInstance setInstance = TensorFunctions.newSet(elementType, 1);

        // cBody = cBody.replace("<CALL_SET>", setInstance.getCName());

        // Create instance
        LiteralInstance newSetTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        // Set includes
        newSetTensor.setCustomImplementationIncludes(Arrays.asList(SystemInclude.Stdlib.getIncludeName()));

        // Set dependent instances
        // newSetTensor.setCustomImplementationInstances(newTensorInstance, setInstance);
        newSetTensor.getCustomImplementationInstances().add(newTensorInstance);

        return newSetTensor;
    }

    /**
     * @param elementType
     * @param numMatrices
     * @return
     */
    FunctionInstance newColWithMatrices(VariableType elementType, int numMatrices) {
        // Name of the function
        String functionName = "new_col_array_" + elementType.getSmallId() + "_" + numMatrices;

        // Input names
        String inputPrefix = "value";

        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numMatrices; i++) {
            String inputName = getInputName(inputPrefix, i);
            inputNames.add(inputName);
        }

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        for (int i = 0; i < numMatrices; i++) {
            inputTypes.add(tensorType);
        }

        // Output
        String tensorName = "t";

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, tensorName,
                tensorType);

        CInstructionList insts = new CInstructionList(fTypes);

        // Int type
        // VariableType intType = VariableTypeFactory.newInt();
        VariableType intType = getNumerics().newInt();

        // Check variable
        String checkVarname = "error";
        Variable checkVar = new Variable(checkVarname, intType);
        // CToken checkVar = CTokenFactory.newVariable(checkVarname, VariableTypeFactory.newInt());

        // Initialize check var
        insts.addAssignment(checkVar, CNodeFactory.newCNumber(0));

        // Add check
        CNode rowIndex = CNodeFactory.newCNumber(0);
        CNode colIndex = CNodeFactory.newCNumber(1);

        CNode errorAssign = CNodeFactory.newAssignment(checkVar, CNodeFactory.newCNumber(1));

        Variable numRows = new Variable("numRows", intType);
        Variable numCols = new Variable("numCols", intType);
        insts.addAssignment(numRows, CNodeFactory.newCNumber(0));
        insts.addAssignment(numCols, CNodeFactory.newCNumber(0));

        for (int i = 0; i < numMatrices; i++) {
            // Build if condition
            CNode inputVar = CNodeFactory.newVariable(inputNames.get(i), inputTypes.get(i));
            MatrixType inputMatrixType = (MatrixType) inputVar.getVariableType();
            InstanceProvider numelProvider = inputMatrixType.functions().numel();
            InstanceProvider dimProvider = inputMatrixType.functions().getDim();

            CNode numel = getFunctionCall(numelProvider, inputVar);
            CNode getDim1 = getFunctionCall(dimProvider, inputVar, rowIndex);
            CNode getDim2 = getFunctionCall(dimProvider, inputVar, colIndex);

            CNode notEmptyCall = getFunctionCall(COperator.NotEqual, numel, CNodeFactory.newCNumber(0));
            CNode diffDim2Call = getFunctionCall(COperator.NotEqual, getDim2, CNodeFactory.newVariable(numCols));

            CNode numColsUnset = getFunctionCall(COperator.Equal, CNodeFactory.newVariable(numCols),
                    CNodeFactory.newCNumber(0));

            CNode incrRows = CNodeFactory.newAssignment(numRows,
                    getFunctionCall(COperator.Addition, CNodeFactory.newVariable(numRows), getDim1));
            CNode setCols = CNodeFactory.newAssignment(numCols, getDim2);

            insts.addIf(notEmptyCall,
                    IfNodes.newIfThenElse(diffDim2Call,
                            IfNodes.newIfThenElse(numColsUnset,
                                    Arrays.asList(incrRows, setCols),
                                    Arrays.asList(errorAssign)),
                            incrRows));
        }

        // Error printf
        String errorMsg = "Dimensions of matrices being concatenated are not consistent.";
        CNode checkVarToken = CNodeFactory.newVariable(checkVar);
        insts.addNewline();
        insts.addInstruction(new TensorCreationUtils(getSettings()).getIfFailure(checkVarToken, errorMsg));

        // Sum number of rows
        insts.addNewline();
        CNode currentSum = null;
        // CToken zeroToken = CToken.build(0);
        CNode zeroToken = CNodeFactory.newCNumber(0, getNumerics().newInt());
        for (String inputName : inputNames) {
            // for (int i = 0; i < inputNames.size(); i++) {
            // String inputName = inputNames.get(i);
            Variable inputVar = fTypes.getInputVar(inputName);
            // FunctionInstance dim = TensorProvider.DIM_SIZE.getInstance(inputVar.getType());
            // CToken dimCall = dim.newFunctionCall(CToken.build(inputVar), zeroToken);
            CNode dimCall = getFunctionCall(TensorProvider.DIM_SIZE, CNodeFactory.newVariable(inputVar), zeroToken);
            // insts.addFunctionCall(dim, CToken.build(inputVar), zeroToken);
            if (currentSum == null) {
                currentSum = dimCall;
            } else {
                // currentSum = COperator.Addition.getFunctionCall(currentSum, dimCall);
                currentSum = getFunctionCall(COperator.Addition, currentSum, dimCall);
            }
            // rowSums.add(dimCall);
        }

        // Create new array
        FunctionInstance newArrayInstance = newArray(elementType, 2);
        Variable outVar = fTypes.getReturnVar();
        MatrixType outType = (MatrixType) outVar.getType();
        CNode out = CNodeFactory.newVariable(outVar);
        // insts.addFunctionCall(newArrayInstance, CToken.build(numRows), CToken.build(numCols), out);
        insts.addFunctionCall(newArrayInstance, CNodeFactory.newVariable(numRows), CNodeFactory.newVariable(numCols),
                out);

        VariableNode colVar = CNodeFactory.newVariable("col", intType);
        VariableNode outPosVar = CNodeFactory.newVariable("outPos", intType);
        VariableNode rowVar = CNodeFactory.newVariable("row", intType);

        insts.addAssignment(outPosVar, CNodeFactory.newCNumber(0));

        CInstructionList outerLoop = new CInstructionList();

        ForNodes forNodes = new ForNodes(getData());

        int numInputs = inputTypes.size();
        for (int inputId = 0; inputId < numInputs; ++inputId) {
            String inputName = inputNames.get(inputId);
            MatrixType inputType = (MatrixType) inputTypes.get(inputId);

            VariableNode input = CNodeFactory.newVariable(inputName, inputType);
            CNode inputRows = FunctionInstanceUtils.getFunctionCall(
                    inputType.functions().getDim(),
                    getData(),
                    input,
                    CNodeFactory.newCNumber(0));

            CNode offset = FunctionInstanceUtils.getFunctionCall(COperator.Multiplication,
                    getData(),
                    colVar,
                    FunctionInstanceUtils.getFunctionCall(inputType.functions().getDim(), getData(), input,
                            CNodeFactory.newCNumber(0)));

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
                    out,
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

        insts.addInstruction(forNodes.newForLoopBlock(colVar, CNodeFactory.newVariable(numCols), outerLoop.get()));

        // Add return
        insts.addReturn(out);

        FunctionInstance instance = new InstructionsInstance(fTypes, functionName, getFilename(), insts);

        return instance;
    }

    /**
     * Creates a new instance of a function which creates a diagonal matrix with the values set to 1. Returns the
     * created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which represents the input;<br>
     * - A pointer to an allocated matrix, which represents the output;<br>
     * - A pointer to an allocated matrix, which represents the temporary matrix that will save the result of the
     * operation between input and output;<br>
     * 
     * @param tensorType
     * @param prefixName
     * @param numDims
     * @return
     */
    /*
    static FunctionInstance newArrayWithReuse(VariableType elementType) {
    
    // Name of the function
    String functionName = "new_array_with_reuse_" + elementType.getSmallId();
    
    // Input names
    List<String> inputNames = Arrays.asList("input", "output", "temp");
    
    // Input types
    VariableType tensorType = VariableTypeFactory.newAllocMatrix(elementType);
    VariableType pointerToTensor = VariableTypeFactory.newPointerToMatrixAlloc(tensorType);
    // List<VariableType> inputTypes = Arrays.asList(tensorType, pointerToTensor);
    List<VariableType> inputTypes = Arrays.asList(tensorType, pointerToTensor, pointerToTensor);
    
    // FunctionTypes
    // FunctionTypes fTypes = FunctionTypes.newInstanceWithOutputsAsInputs(inputNames, inputTypes,
    // "temp", tensorType);
    FunctionTypes fTypes = FunctionTypes.newInstance(inputNames, inputTypes, null,
    	VariableTypeFactory.newVoid());
    
    // Get body of function
    String cBody = IoUtils.getResourceString(TensorCreationResource.NEW_ARRAY_WITH_REUSE_BODY);
    
    cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
    cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
    
    FunctionInstance newArrayWithHelperInstance = newArrayHelper(elementType);
    cBody = cBody.replace("<CALL_NEW_HELPER>", newArrayWithHelperInstance.getCName());
    
    // Create instance
    LiteralFunctionInstance newSetTensor = new LiteralFunctionInstance(fTypes, functionName,
    	getFilename(), cBody);
    
    // Set includes (stdlib for NULL)
    newSetTensor.setIncludesImplementation(SystemInclude.Stdlib.getIncludeName());
    
    // Set dependent instances
    newSetTensor.setDependentInstances(newArrayWithHelperInstance);
    
    return newSetTensor;
    }
    */

    /**
     * Returns a row matrix which represents a linear view of the input matrix, according to the given offset and
     * length.
     * 
     * <p>
     * getInstance receives:<br>
     * - Matrix from which the view will be built;<br>
     * - An integer representing the offset. Assumes zero-based indexing;<br>
     * - An integer the length of the view;<br>
     * 
     * @param elementType
     * @param copy
     *            if true, returns a version which copies the elements of the view to another array. Otherwise, it justs
     *            passes the pointer from the original matrix, plus an offset
     * @return
     */
    public FunctionInstance newGetRowView(VariableType elementType, boolean copy) {

        String type = null;
        if (copy) {
            type = "copy_";
        } else {
            type = "pointer_";
        }

        String functionName = "get_row_view_" + type + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        String offsetName = "offset";
        String lengthName = "length";

        List<String> inputNames = Arrays.asList(tensorName, offsetName, lengthName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        // VariableType intType = VariableTypeFactoryOld.newNumeric(NumericType.Cint);
        VariableType intType = getNumerics().newInt();

        List<VariableType> inputTypes = Arrays.asList(tensorType, intType, intType);

        // FunctionTypes
        String viewName = "view";
        MatrixType outputType = MatrixUtils.getView(tensorType);

        // Views created with this function have a row shape
        outputType = outputType.matrix().setShape(TypeShape.newRow());

        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, viewName,
                outputType);

        // Get name of the tensor structure
        // MatrixAllocData mData = VariableTypeContent.getMatrixAlloc(tensorType);
        // String tensorStructureName = mData.getInstance().getCName();
        String tensorStructureName = DynamicMatrixUtils.getStructInstance(tensorType).getCName();

        // Build body
        // String cBody = IoUtils.getResource(TensorCreationResource.NEW_ARRAY_VIEW_BODY);
        Replacer cBody = new Replacer(SpecsIo.getResource(TensorCreationResource.NEW_ARRAY_VIEW_BODY));

        cBody = cBody.replace("<TENSOR_STRUCT>", tensorStructureName);

        DynamicMatrixStruct.replaceFields(getData(), cBody);
        /*
        cBody = cBody.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
        cBody = cBody.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
        */
        cBody = cBody.replace("<DATA_TYPE>", CodeUtils.getType(elementType));

        String copyValue = "1";
        if (!copy) {
            copyValue = "0";
        }
        cBody = cBody.replace("<IS_COPY>", copyValue);

        LiteralInstance newEmptyTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody.toString());

        // Set includes
        Set<String> includes = SpecsFactory.newHashSet();
        includes.add(SystemInclude.Stdlib.getIncludeName());
        includes.add(SystemInclude.Stdio.getIncludeName());
        // includes.addAll(elementType.getIncludes());
        // newEmptyTensor.setCustomImplementationIncludes(stdLib, stdio);
        newEmptyTensor.setCustomImplementationIncludes(includes);

        return newEmptyTensor;
    }

    /**
     * Returns a row matrix which represents a linear view of the input matrix, according to the given offset and
     * length.
     * 
     * <p>
     * getInstance receives:<br>
     * - Matrix from which the view will be built;<br>
     * - An integer representing the offset. Assumes zero-based indexing;<br>
     * - An integer the length of the view;<br>
     * 
     * @param elementType
     * @param copy
     *            if true, returns a version which copies the elements of the view to another array. Otherwise, it justs
     *            passes the pointer from the original matrix, plus an offset
     * @return
     */
    public FunctionInstance newGetRowViewPointer(VariableType elementType, boolean copy) {

        String type = null;
        if (copy) {
            type = "copy_";
        } else {
            type = "pointer_";
        }

        String functionName = getRowViewPointerPrefix() + type + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        String offsetName = "offset";
        String lengthName = "length";

        List<String> inputNames = Arrays.asList(tensorName, offsetName, lengthName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        // VariableType intType = VariableTypeFactoryOld.newNumeric(NumericType.Cint);
        VariableType intType = getNumerics().newInt();

        List<VariableType> inputTypes = Arrays.asList(tensorType, intType, intType);

        // FunctionTypes
        String viewName = "view";
        MatrixType outputType = MatrixUtils.getView(tensorType);

        // Views created with this function have a row shape
        outputType = outputType.matrix().setShape(TypeShape.newRow());
        // outputType = new PointerType(outputType);

        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, viewName,
                new PointerType(outputType));

        // Get name of the tensor structure
        // MatrixAllocData mData = VariableTypeContent.getMatrixAlloc(tensorType);
        // String tensorStructureName = mData.getInstance().getCName();
        String tensorStructureName = DynamicMatrixUtils.getStructInstance(tensorType).getCName();

        // Build body
        // String cBody = IoUtils.getResource(TensorCreationResource.NEW_ARRAY_VIEW_BODY);
        Replacer cBody = new Replacer(SpecsIo.getResource(TensorCreationResource.NEW_ARRAY_POINTER_VIEW_BODY));

        cBody = cBody.replace("<TENSOR_STRUCT>", tensorStructureName);

        DynamicMatrixStruct.replaceFields(getData(), cBody);
        /*
        cBody = cBody.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
        cBody = cBody.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
        */
        cBody = cBody.replace("<DATA_TYPE>", CodeUtils.getType(elementType));

        String copyValue = "1";
        if (!copy) {
            copyValue = "0";
        }
        cBody = cBody.replace("<IS_COPY>", copyValue);

        LiteralInstance newEmptyTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody.toString());

        // Set includes
        Set<String> includes = SpecsFactory.newHashSet();
        includes.add(SystemInclude.Stdlib.getIncludeName());
        includes.add(SystemInclude.Stdio.getIncludeName());
        // includes.addAll(elementType.getIncludes());
        // newEmptyTensor.setCustomImplementationIncludes(stdLib, stdio);
        newEmptyTensor.setCustomImplementationIncludes(includes);

        return newEmptyTensor;
    }

    /**
     * @param elementType
     * @return
     */
    static FunctionInstance newFreeView(VariableType elementType) {
        // Name of the function
        String functionName = "tensor_free_view_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        List<String> inputNames = Arrays.asList(tensorName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        VariableType pointerToMatrix = ReferenceUtils.getType(tensorType, true);
        List<VariableType> inputTypes = Arrays.asList(pointerToMatrix);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        // Get body of function
        String cBody = SpecsIo.getResource(TensorCreationResource.FREE_VIEW_BODY);

        // Apply changes to default fields
        String tensorShapeTag = "<TENSOR_SHAPE>";
        cBody = cBody.replace(tensorShapeTag, DynamicMatrixStruct.TENSOR_SHAPE);

        // Create instance
        LiteralInstance setTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        return setTensor;
    }
}
