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

package org.specs.CIRTypes.Types.StaticMatrix.Functions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

import com.google.common.base.Preconditions;

public class Numel extends AInstanceBuilder {

    public Numel(ProviderData data) {
	super(data);
    }

    /**
     * Creates a new instance of the function 'numel', which returns an integer representing the number of elements in
     * the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A static matrix;
     * 
     * @return
     */
    @Override
    public FunctionInstance create() {

	// Should have only one argument, of type static matrix
	StaticMatrixType matrixType = getMatrixType();

	final int numElements = matrixType.getShape().getNumElements();

	String functionName = "numel_dec_" + numElements;

	List<VariableType> inputTypes = Arrays.asList(matrixType);
	// List<VariableType> inputTypes = CollectionUtils.asListSame(matrixType);
	VariableType returnType = getNumerics().newInt(numElements);
	FunctionType functionTypes = FunctionType.newInstanceNotImplementable(inputTypes, returnType);

	InlineCode inlineCode = new InlineCode() {

	    @Override
	    public String getInlineCode(List<CNode> arguments) {
		return Integer.toString(numElements);
	    }
	};

	return new InlinedInstance(functionTypes, functionName, inlineCode);

    }

    private StaticMatrixType getMatrixType() {
	List<VariableType> inputs = getData().getInputTypes();

	// Must have only one argument, of type matrix
	Preconditions.checkArgument(inputs.size() == 1, "Must have one input");
	VariableType matrixType = inputs.get(0);

	// First argument must be a matrix
	Preconditions.checkArgument(MatrixUtils.isMatrix(matrixType), "First input must be a matrix");

	// Matrix must be of type DynamicMatrixType
	Preconditions.checkArgument(matrixType instanceof StaticMatrixType, "Matrix must be of type '"
		+ StaticMatrixType.class + "'");

	return (StaticMatrixType) matrixType;

    }

}
