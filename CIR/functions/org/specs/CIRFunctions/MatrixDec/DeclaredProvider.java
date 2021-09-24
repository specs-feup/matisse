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

package org.specs.CIRFunctions.MatrixDec;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

/**
 * @author Joao Bispo
 * 
 */
public enum DeclaredProvider implements InstanceProvider {

    /**
     * Function Call receives a matrix as input.
     */
    /*
    NUMEL {
    @Override
    public FunctionInstance newInstance(ProviderData data) {

        List<VariableType> inputTypes = data.getInputTypes();

        // Should have only one argument, of type matrix
        VariableType matrixType = getMatrixTypeAtIndex(this, inputTypes, 0);

        return new DeclaredFunctions(data).newNumel(matrixType);
    }

    },
    */
    /**
     * Function Call receives a matrix as input.
     */
    PRINT_DIM {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    List<VariableType> inputTypes = data.getInputTypes();

	    // Should have only one argument, of type matrix
	    VariableType matrixType = getMatrixTypeAtIndex(this, inputTypes, 0);

	    return DeclaredFunctions.newPrintDim(matrixType);
	}
    },

    /**
     * Returns a row matrix which represents a linear view of the input matrix, according to the given offset and
     * length.
     * 
     * <p>
     * getInstance receives:<br>
     * - Matrix from which the view will be built;<br>
     * - An integer representing the offset. Assumes zero-based indexing;<br>
     * - An integer the length of the view. Must have a constant value;<br>
     * 
     */
    GET_ROW_VIEW {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {

	    List<VariableType> inputTypes = data.getInputTypes();

	    // First argument should be the input matrix
	    // VariableType matrixType = getMatrixTypeAtIndex(this, inputTypes, 0);
	    MatrixType matrixType = data.getInputType(MatrixType.class, 0);

	    // Third argument must reduce to a constant
	    // NumericData numericData = VariableTypeContent.getNumeric(inputTypes.get(2));
	    // NumericType numeric = VariableTypeUtils.getConstantString(type)getNumericType(inputTypes.get(2));
	    // int length = numericData.getIntValue();
	    int length = ScalarUtils.getConstant(inputTypes.get(2)).intValue();

	    return new DeclaredFunctions(data).newGetRowView(matrixType, length);
	}
    },

    /**
     * Creates an inlined version of the function 'get_dec', which returns the contents of a declared matrix in the
     * specified index. The index can be represented with a variable number of integers.
     * 
     * <p>
     * getInstance receives:<br>
     * - Matrix from which the element type is extracted;<br>
     * - As many arguments as the number of dimensions;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, whose values will be read;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     */
    /*
    GET {
    @Override
    public FunctionInstance create(ProviderData data) {

        List<VariableType> inputTypes = data.getInputTypes();

        // First argument is the matrix to access
        VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
        // VariableType elementType = VariableTypeContent.getMatrixDec(matrixType).getType();

        // Number of dimensions is size of inputs minus 1
        int numDims = inputTypes.size() - 1;

        return new DeclaredFunctions(data).newGetInline(matrixType, numDims);
    }
    },
    */

    /**
     * Creates a new instance of the function 'set', which sets a single value in a static matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A matrix, whose values will be set;<br>
     * - A variable number of integers with the indexes of the matrix that will be set. Function assumes zero-based
     * indexing;<br>
     * - A scalar, whose value will be set in the matrix at the specified index;<br>
     */
    /*
    SET {
    @Override
    public FunctionInstance newCInstance(ProviderData data) {

        // First argument is the matrix to access
        // VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);

        // Number of dimensions is size of inputs minus 2
        // int numDims = inputTypes.size() - 2;
        int numDims = data.getInputTypes().size() - 2;

        // return DeclaredFunctions.newSetInline(matrixType, numDims);

        // return new DeclaredFunctions(data).newSetInline();
        return DeclaredFunctions.newSetInline(numDims).newCInstance(data);
    }
    },
    */

    /**
     * Creates a new instance of the function 'copy', which copies the contents of one matrix to other.
     * 
     * <p>
     * FunctionInstance receives:<br>
     * - The type of the matrix;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source;<br>
     * - A declared matrix with the same shape, which will be the sink;<br>
     */
    COPY {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    // List<VariableType> inputTypes = data.getInputTypes();

	    // First argument is the matrix to access
	    // VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);

	    // return DeclaredFunctions.newCopy(matrixType);
	    return DeclaredFunctions.newCopyV2(data.getInputType(MatrixType.class, 0));
	}
    },

    /**
     * Creates a new instance of the function 'dimSize', which returns the size of the indicated dimension.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source;<br>
     * - The index of the dimension;
     */
    DIM_SIZE {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    List<VariableType> inputTypes = data.getInputTypes();

	    return new DeclaredFunctions(data).newDimSize(inputTypes.get(0));
	}
    },

    /**
     * Creates a new instance of the function 'new_array', which creates a column matrix with the given matrices.
     * Returns the created matrix.
     * 
     * <p>
     * FunctionInstance receives:<br>
     * - A variable number of matrices of elementType, representing the values of the matrix;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of declared matrices of element type, representing the values of the matrix;<br>
     * - A declared matrix, which will be used to store the new matrix;<br>
     * 
     */
    NEW_COL_MATRIX {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    return new MatrixColFunction(data).create();
	}

	@Override
	public FunctionType getType(ProviderData data) {
	    return new MatrixColFunction(data).getFunctionType();
	}
    },

    /**
     * Creates a new instance of the function 'size', which returns a row vector with the size of given matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source;<br>
     * - Optionally, a scalar indicating the index to return; <br>
     */
    SIZE {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {

	    return new SizeStatic(data).create();
	}
    },

    /**
     * Creates a new instance that returns the ( 0-based ) index of the first non-singleton dimensions of the input
     * matrix. <br/>
     * FunctionInstance receives a declared matrix.
     * 
     */
    FIRST_NON_SINGLETON_DIMENSION {

	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    return new DeclaredFunctions(data).newFirstNonSingletonDim(data.getInputType(MatrixType.class, 0));
	}

    },

    /**
     * Creates a new instance that returns the size of the first non-singleton dimensions of the input matrix. <br/>
     * FunctionInstance receives a declared matrix.
     * 
     */
    FIRST_NON_SINGLETON_DIMENSION_SIZE {

	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    return new DeclaredFunctions(data).newFirstNonSingletonDimSize(data.getInputType(MatrixType.class, 0));
	}

    },

    /**
     * Creates a new instance that returns the pointer to the first element of the matrix.
     */
    GET_DATA_PTR {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {
	    return new DeclaredFunctions(data).newGetDataPointerInlined(data.getInputTypes());
	}
    };

    /**
     * Creates a function instance of a matrix function based on the given list of input types..
     * 
     * @param variableType
     * @return
     */
    // public abstract FunctionInstance getInstance(List<VariableType> inputTypes);

    /**
     * Checks if input types has size one, and if the first input is a declared matrix.
     * 
     * @param function
     * 
     * @param inputTypes
     * @return
     */
    private static VariableType getMatrixTypeAtIndex(DeclaredProvider function, List<VariableType> inputTypes, int index) {

	if (inputTypes.size() <= index) {
	    String indexPlural = "";
	    if (index > 0) {
		indexPlural = "s";
	    }

	    String inputsPlural = "";
	    if (inputTypes.size() > 0) {
		inputsPlural = "s";
	    }

	    throw new RuntimeException("Function '" + function + "' receives at least " + (index + 1) + " argument"
		    + indexPlural + ", gave " + inputTypes.size() + " input" + inputsPlural);
	}

	VariableType matrixType = inputTypes.get(index);
	if (!MatrixUtils.isStaticMatrix(matrixType)) {
	    throw new RuntimeException("Argument " + (index + 1) + "of function '" + function
		    + "' needs to be a declared matrix.");
	}

	return matrixType;
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param inputTokens
     * @return
     */
    /*
    public FunctionInstance getInstance(VariableType... inputTypes) {
    return getInstance(data);
    }
    */

    /*
    public CToken getFunctionCall(List<CToken> inputTokens) {
    List<VariableType> inputTypes = CTokenUtils.getVariableTypes(inputTokens);

    FunctionInstance instance = getInstance(inputTypes);
    return FunctionUtils.getFunctionCall(instance, inputTokens);
    }
    */

    /*
    public CToken getFunctionCall(CToken... arguments) {
    return CirFunctionsUtils.getFunctionCall(this, arguments);
    }
    */

    /**
     * Creates a FunctionCall token which this function for the given arguments.
     * 
     * @param arguments
     * @return
     */
    /*
    public CToken getFunctionCall(List<CToken> arguments) {
    return CirFunctionsUtils.getFunctionCall(this, arguments);
    }
    */
}
