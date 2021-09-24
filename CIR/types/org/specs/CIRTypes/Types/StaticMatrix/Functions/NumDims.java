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
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

/**
 * 
 * @author JoaoBispo
 *
 */
public class NumDims extends AInstanceBuilder {

    private static final Checker CHECKER = new CirInputsChecker()
	    .numOfInputs(1)
	    .ofType(StaticMatrixType.class);

    public NumDims(ProviderData data) {
	super(data);
    }

    public static InstanceProvider getProvider() {
	return new GenericInstanceProvider(NumDims.CHECKER, data -> new NumDims(data).create());
    }

    /**
     * Creates a new instance of the function 'numDims', which returns an integer representing the number of dimensions
     * in the matrix.
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
	StaticMatrixType matrixType = getTypeAtIndex(StaticMatrixType.class, 0);

	int numDims = matrixType.getShape().getNumDims();

	String functionName = "numdims_dec_" + numDims;

	List<VariableType> inputTypes = Arrays.asList(matrixType);
	// List<VariableType> inputTypes = CollectionUtils.asListSame(matrixType);
	VariableType returnType = getNumerics().newInt(numDims);
	FunctionType functionTypes = FunctionType.newInstanceNotImplementable(inputTypes, returnType);

	InlineCode inlineCode = (args) -> Integer.toString(numDims);
	/*
		InlineCode inlineCode = new InlineCode() {
	
		    @Override
		    public String getInlineCode(List<CNode> arguments) {
			return Integer.toString(numDims);
		    }
		};
	*/
	return new InlinedInstance(functionTypes, functionName, inlineCode);

    }

    @Override
    protected Checker getCheckerPrivate() {
	return NumDims.CHECKER;
    }

    /*
    private StaticMatrixType getMatrixType() {
    List<VariableType> inputs = getData().getInputTypes();
    
    // Must have only one argument, of type matrix
    Preconditions.checkArgument(inputs.size() == 1, "Must have one input");
    VariableType matrixType = inputs.get(0);
    
    // First argument must be a matrix
    Preconditions.checkArgument(MatrixUtilsV2.isMatrix(matrixType), "First input must be a matrix");
    
    // Matrix must be of type DynamicMatrixType
    Preconditions.checkArgument(matrixType instanceof StaticMatrixType, "Matrix must be of type '"
    	+ StaticMatrixType.class + "'");
    
    return (StaticMatrixType) matrixType;
    
    }
    */

}
