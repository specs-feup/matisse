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

package org.specs.MatlabToC.Functions.MatlabOps.ElementWise;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Free;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsAllocResource;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;

/**
 * Contains extra code needed to implement element-wise functions with allocated matrices.
 * 
 * <p>
 * For instance, if the output matrix is not initialized, it has to be created with the same size as the inputs; the
 * size of the inputs need to be checked.
 * 
 * @author Joao Bispo
 * 
 */
public class ElementWiseAllocCode {

    private final String checkCode;
    private final Set<FunctionInstance> instances;
    private final Set<String> includes;

    private ElementWiseAllocCode(String checkCode, Set<FunctionInstance> instances,
	    Set<String> includes) {

	this.checkCode = checkCode;
	this.instances = instances;
	this.includes = includes;
    }

    /**
     * Builds the extra code necessary when using the element wise operation with allocated matrices.
     * 
     * <p>
     * If the given matrix type is not an allocated matrix, returns null.
     * 
     * @param matrixType
     * @param input1Name
     * @param input2Name
     * @param outputName
     * @param outputType
     * @param pdata
     * @return
     */
    // public static ElementWiseAllocCode newInstance(VariableType matrixType, String input1Name,
    // String input2Name, String outputName) {
    public static ElementWiseAllocCode newInstance(List<VariableType> inputTypes,
	    List<String> inputNames, String outputName, int arity, VariableType outputType,
	    ProviderData pdata) {

	int firstMatrixIndex = SpecsCollections.getFirstIndex(inputTypes, MatrixType.class);
	// int firstMatrixIndex = MatrixUtils.getFirstMatrixIndex(inputTypes);

	// VariableType firstMatrixType = inputTypes.get(firstMatrixIndex);
	// MatrixType firstMatrixType = CollectionUtils.get(MatrixType.class, inputTypes, firstMatrixIndex);
	MatrixType firstMatrixType = (MatrixType) inputTypes.get(firstMatrixIndex);

	// if (!MatrixUtilsV2.isDynamicMatrix(firstMatrixType)) {
	if (!firstMatrixType.usesDynamicAllocation()) {
	    return null;
	}

	int numInputs = inputTypes.size();
	if (numInputs > 2 && numInputs < 0) {
	    throw new RuntimeException("Method not defined for arity '" + numInputs + "'");
	}

	// Initialize
	StringBuilder builder = new StringBuilder();
	Set<FunctionInstance> instances = SpecsFactory.newHashSet();
	Set<String> includes = SpecsFactory.newHashSet();

	// if (numInputs == 2 && MatrixUtils.isMatrix(inputTypes)) {
	if (numInputs == 2 && SpecsCollections.areOfType(MatrixType.class, inputTypes)) {
	    String checkCode = SpecsIo
		    .getResource(MatlabOperatorsAllocResource.ELEMENT_WISE_ALLOC_CHECK1);

	    // Tensor fields
	    checkCode = checkCode.replace("<MATRIX_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
	    checkCode = checkCode.replace("<MATRIX_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);

	    // Input/Output names
	    String matrix1 = getMatrixName(inputNames.get(0), inputTypes.get(0));
	    String matrix2 = getMatrixName(inputNames.get(1), inputTypes.get(1));
	    checkCode = checkCode.replace("<MATRIX1>", matrix1);
	    checkCode = checkCode.replace("<MATRIX2>", matrix2);

	    // Call to functions
	    // FunctionInstance isSameShapeFunction = TensorProvider.IS_SAME_SHAPE
	    // .getInstance(firstMatrixType);
	    ProviderData isSameData = ProviderData.newInstance(pdata, firstMatrixType);
	    FunctionInstance isSameShapeFunction = TensorProvider.IS_SAME_SHAPE
		    .newCInstance(isSameData);
	    checkCode = checkCode.replace("<CALL_IS_SAME_SHAPE>", isSameShapeFunction.getCName());
	    instances.add(isSameShapeFunction);

	    // Macro for EXIT_FAILURE
	    includes.add(SystemInclude.Stdlib.getIncludeName());

	    builder.append(checkCode);
	}

	String checkCode = SpecsIo
		.getResource(MatlabOperatorsAllocResource.ELEMENT_WISE_ALLOC_CHECK2);

	// Tensor fields
	checkCode = checkCode.replace("<MATRIX_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
	checkCode = checkCode.replace("<MATRIX_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);

	// Input/Output names
	// Input/Output names
	String matrix1 = getMatrixName(inputNames.get(firstMatrixIndex),
		inputTypes.get(firstMatrixIndex));
	checkCode = checkCode.replace("<MATRIX1>", matrix1);
	checkCode = checkCode.replace("<OUTPUT_MATRIX>", outputName);

	ProviderData newArrayData = ProviderData.newInstance(pdata, outputType);
	// FunctionInstance newArrayFunction = TensorProvider.NEW_ARRAY_HELPER.newCInstance(newArrayData);
	FunctionInstance newArrayFunction = firstMatrixType.matrix().functions().createFromMatrix()
		.getCheckedInstance(newArrayData);

	CNode matrixVar = CNodeFactory.newVariable(matrix1, firstMatrixType);
	CNode outputVar = CNodeFactory.newVariable(outputName, new PointerType(outputType));

	String newArrayCallCode = newArrayFunction.getCallCode(Arrays.asList(matrixVar, outputVar));
	// checkCode = checkCode.replace("<CALL_NEW_ARRAY_HELPER>", newArrayFunction.getCName());
	checkCode = checkCode.replace("<CALL_NEW_ARRAY_HELPER>", newArrayCallCode);
	instances.add(newArrayFunction);
	instances.addAll(newArrayFunction.getCallInstances());

	FunctionInstance newFreeFunction = new Free(newArrayData).create();
	checkCode = checkCode.replace("<CALL_FREE>", newFreeFunction.getCName());
	instances.add(newFreeFunction);

	// Macro for NULL
	includes.add(SystemInclude.Stdlib.getIncludeName());
	// For printf
	// includes.add(SystemInclude.Stdio.getIncludeName());

	return new ElementWiseAllocCode(checkCode, instances, includes);
    }

    /**
     * @param string
     * @param variableType
     * @return
     */
    private static String getMatrixName(String name, VariableType type) {
	if (!ReferenceUtils.isPointer(type)) {
	    return name;
	}

	return "(*" + name + ")";
    }

    /**
     * @return the checkCode
     */
    public String getCheckCode() {
	return this.checkCode;
    }

    /**
     * @return the includes
     */
    public Set<String> getIncludes() {
	return Collections.unmodifiableSet(this.includes);
    }

    /**
     * @return the instances
     */
    public Set<FunctionInstance> getInstances() {
	return Collections.unmodifiableSet(this.instances);
    }
}
