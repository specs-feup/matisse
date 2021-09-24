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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class Data extends AInstanceBuilder {

    /**
     * @param data
     */
    public Data(ProviderData data) {
	super(data);
    }

    /**
     * 
     * Creates a new instance of the function 'data', which returns a pointer to the first element of the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix;
     * 
     * 
     * @return
     */
    @Override
    public FunctionInstance create() {
	return newInlinedInstance();
    }

    /**
     * Creates a new instance of the function 'data', which returns a pointer to the first element of the matrix.
     * 
     * <p>
     * A vector/column matrix has 2 dimensions.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix;
     * 
     * @param data
     * @return
     */
    private FunctionInstance newInlinedInstance() {

	// Should have only one argument, of type matrix
	DynamicMatrixType matrixType = getTypeAtIndex(DynamicMatrixType.class, 0);
	ScalarType elementType = matrixType.getElementType();

	// Name of the function
	String functionName = "data_alloc_" + elementType.getSmallId();

	// Input types
	final DynamicMatrixType tensorType = DynamicMatrixType.newInstance(elementType);
	List<VariableType> inputTypes = SpecsCollections.asListT(VariableType.class, tensorType);

	VariableType outputType = elementType.pointer().getType(true);

	// FunctionTypes
	FunctionType fTypes = FunctionType.newInstanceNotImplementable(inputTypes, outputType);

	InlineCode inlineCode = newInlineCode();
	InlinedInstance instance = new InlinedInstance(fTypes, functionName, inlineCode);
	instance.setCallPrecedenceLevel(PrecedenceLevel.MemberAccess);

	return instance;

    }

    private static InlineCode newInlineCode() {
	return new InlineCode() {

	    @Override
	    public String getInlineCode(List<CNode> arguments) {
		if (arguments.size() != 1) {
		    SpecsLogs
			    .msgWarn("Calling this version of 'numel' with a number of arguments different than one. ");
		}

		VariableNode strut = (VariableNode) arguments.get(0);
		return DynamicMatrixStruct.getDataCode(strut.getVariable().getName(), strut.getVariableType());
		// VariableType type = strut.getVariableType();

		// If Variable, just pass its name
		/*
			String structCode = strut.getCode();
		if (strut.getType() != CNodeType.Variable) {
		    structCode = "(" + structCode + ")";
		}
		


		return MatrixCode.getStructField(structCode, type, DynamicMatrixStruct.TENSOR_DATA, null);
		*/

	    }
	};
    }
}
