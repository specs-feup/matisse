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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions.LowLevel;

import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

/**
 * Creates a new instance of the function 'is_same_shape', which returns 1 (true) if the given shape is the same as the
 * input matrix, or false otherwise.
 * 
 * <p>
 * FunctionCall receives:<br>
 * - A dynamic matrix;<br>
 * - A pointer to integer, representing an array with the shape;<br>
 * - An integer with the number of dimensions of the shape;<br>
 * 
 * @author JoaoBispo
 *
 */
public class IsSameShape extends AInstanceBuilder {

    private final ScalarType elementType;

    public IsSameShape(ProviderData data, ScalarType elementType) {
	super(data);

	this.elementType = elementType;
    }

    private static final ThreadSafeLazy<String> isSameShapeBodyResource =
	    new ThreadSafeLazy<>(() -> SpecsIo.getResource(DynamicMatrixResource.IS_SAME_SHAPE_BODY.getResource()));

    @Override
    public FunctionInstance create() {
	// Name of the function
	String functionName = "is_same_shape_alloc_" + elementType.getSmallId();

	// Input names
	String tensorName = "t";
	String shapeName = "shape";
	String dimsName = "dims";
	List<String> inputNames = Arrays.asList(tensorName, shapeName, dimsName);

	// Input types
	VariableType tensorType = DynamicMatrixType.newInstance(elementType);
	VariableType intType = getNumerics().newInt();
	VariableType intPointerType = ReferenceUtils.getType(intType, true);
	List<VariableType> inputTypes = Arrays.asList(tensorType, intPointerType, intType);

	// FunctionTypes
	FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, "value", getNumerics().newInt());

	String cBody = isSameShapeBodyResource.getValue();

	cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
	cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);

	LiteralInstance isSameShape = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

	return isSameShape;
    }

}
