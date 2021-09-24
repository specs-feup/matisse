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

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIRFunctions.CirFunctionsUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Free;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * @deprecated Move to DynamicMatrixFunctions
 */
@Deprecated
public enum TensorProvider implements InstanceProvider {

    /**
     * Creates a new instance of the function 'get_data_ptr', which returns a pointer representing the address of the
     * first element of the matrix.
     * 
     */
    GET_DATA_PTR {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            return new TensorFunctions(data).newGetDataPointerInlined();
        }
    },

    /**
     * Creates a new instance of the function 'numel', which returns an integer representing the number of elements in
     * the matrix.
     * 
     * <p>
     * getInstance receives:<br>
     * - The matrix type, to determine the element type;
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;
     */
    /*
    NUMEL {
    @Override
    public FunctionInstance newInstance(ProviderData data) {
        boolean inlined = true;
        if (inlined) {
    	return new TensorFunctions(data).newNumelInlined(data);
        } else {
    	return new TensorFunctions(data).newNumel(data);
        }
    
    }
    
    },
    */
    /**
     * Creates a new instance of the function 'print_dim_alloc', which prints the contents of the matrix (returns
     * nothing).
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;
     */
    PRINT_DIM {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // Should have only one argument, of type matrix
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return TensorFunctions.newPrintDim(elementType);
        }
    },

    /**
     * Creates an inlined version of the function 'get_tensor', which returns the contents of an allocated matrix in the
     * specified index. The index can be represented with a variable number of integers.
     * 
     * <p>
     * getInstance receives:<br>
     * - Matrix from which the element type is extracted;<br>
     * - As many arguments as the number of dimensions;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be read;<br>
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
        // VariableType elementType = VariableTypeContent.getMatrixAlloc(matrixType).getType();
        VariableType elementType = MatrixUtilsV2.getElementType(matrixType);
    
        // Number of dimensions is size of inputs minus 1
        int numDims = inputTypes.size() - 1;
    
        // Check which 'set' version should be used (inlined or function)
        // Boolean inline = OptionUtils.getBoolean(CirOptionGlobal.INLINE_ALLOCATED_ARRAY_OPERATIONS);
        Boolean inline = data.getSetup().getBoolean(CirOption.INLINE_DYNAMIC_FUNCTIONS);
    
        if (inline) {
    	return new TensorFunctions(data).newGetInline(elementType, numDims);
        } else {
    	return new TensorFunctions(data).newGet(elementType, numDims);
        }
    
    }
    },
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
     */
    GET_ROW_VIEW {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix to access
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            // VariableType elementType = VariableTypeContent.getMatrixAlloc(matrixType).getType();
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            // As default, use copy version
            boolean copy = true;

            // If matrix where we are retrieving the view from is marked as constant, use view pointer

            if (matrixType.isImmutable()) {
                copy = false;
            }

            // return TensorCreationFunctions.newGetRowView(elementType, copy);
            return new TensorCreationFunctions(data).newGetRowView(elementType, copy);
        }
    },

    GET_ROW_VIEW_POINTER {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix to access
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);

            VariableType elementType = MatrixUtils.getElementType(matrixType);

            // As default, use copy version
            boolean copy = false;

            return new TensorCreationFunctions(data).newGetRowView(elementType, copy);
        }
    },

    GET_ROW_VIEW_POINTER_OUT_POINTER {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix to access
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);

            VariableType elementType = MatrixUtils.getElementType(matrixType);

            // As default, use copy version
            boolean copy = false;

            // return new TensorCreationFunctions(data).newGetRowView(elementType, copy);
            return new TensorCreationFunctions(data).newGetRowViewPointer(elementType, copy);
        }
    },

    /**
     * Creates a new instance of the function 'free', which frees the heap memory of an allocated matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be freed;<br>
     */
    FREE {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            return new Free(data).create();
        }
    },

    /**
     * Creates a new instance of the function 'free_view', which frees the heap memory of a matrix view.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix view, which will be freed;<br>
     */
    FREE_VIEW {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // Should have only one argument, of type matrix
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return TensorCreationFunctions.newFreeView(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'new_empty_tensor', which creates a new tensor structure. Returns an
     * allocated matrix with the size specified by the inputs and with data elements of the given 'tensorType'.
     * 
     * <p>
     * getInstance receives:<br>
     * - A variable number of integers, representing the size of each dimension of the matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of integers, representing the size of each dimension of the matrix;<br>
     * - An allocated matrix, which will be used to store the new matrix;<br>
     */
    NEW_ARRAY {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // Last argument is a pointer to the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, inputTypes.size() - 1);
            // VariableType elementType = VariableTypeContent.getMatrixAlloc(matrixType).getType();
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            // Number of dimensions is size of inputs minus 1
            int numDims = inputTypes.size() - 1;

            return new TensorCreationFunctions(data).newArray(elementType, numDims);
        }
    },

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
     */
    NEW_ARRAY_HELPER {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            VariableType matrixType = null;
            // If input types has size one, examine first element for matrix type
            if (inputTypes.size() == 1) {
                matrixType = inputTypes.get(0);
                // matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            } else {
                matrixType = inputTypes.get(2);
            }

            if (!MatrixUtils.isMatrix(matrixType)) {
                SpecsLogs.warn("Given input type is not a matrix");
            }

            // VariableType elementType = VariableTypeContent.getMatrixAlloc(matrixType).getType();
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return new TensorCreationFunctions(data).newArrayHelper(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'new_array_with_reuse', which creates a new tensor structure, minding that
     * if it already exists, might be pointing to an array that will also be read.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of integers, representing the size of each dimension of the matrix;<br>
     * - A value of the matrix type, representing the value that will be set in the matrix positions;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     */
    NEW_CONSTANT_ARRAY {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // Last argument is a pointer to the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, inputTypes.size() - 1);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            // Number of dimensions is size of inputs minus 2
            int numDims = inputTypes.size() - 2;

            return new TensorCreationFunctions(data).newConstantArray(elementType, numDims, data.newInstance());
        }
    },

    /**
     * New instance of a function which creates an 2D empty allocated matrix of type double. Returns the created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * (nothing)
     * 
     */
    NEW_EMPTY {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            return new TensorCreationFunctions(data).newEmptyArrayAlloc();
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
     * - A variable number of matrices of element type, representing the values of the matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     */
    NEW_COL_MATRIX {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // Number of elements
            int numMatrices = inputTypes.size();

            VariableType elementType = MatrixUtils.getElementType(inputTypes.get(0));

            return new TensorCreationFunctions(data).newColWithMatrices(elementType, numMatrices);
        }
    },

    /**
     * Creates a new instance of the function 'tensor_set_inline', which sets a single value in an allocated matrix.
     * 
     * <p>
     * FunctionInstance receives:<br>
     * - An allocated matrix;<br>
     * 
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be set;<br>
     * - A variable number of integers with the indexes of the matrix. Assumes zero-based indexing;<br>
     * - A value of type 'elementType', which will be used to set the allocated matrix at the specified index;<br>
     */
    /*
    SET {
    @Override
    public FunctionInstance newCInstance(ProviderData data) {
    
        List<VariableType> inputTypes = data.getInputTypes();
    
        // First argument is an allocated matrix
        VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
        VariableType elementType = MatrixUtilsV2.getElementType(matrixType);
    
        // Number of dimensions is size of inputs minus 2
        int numDims = inputTypes.size() - 2;
    
        // Check which 'set' version should be used (inlined or function)
        // Boolean inline = data.getSetup().getBoolean(CirOption.INLINE_DYNAMIC_FUNCTIONS);
        boolean inline = data.getSetupTable().getInlining().inline(MatrixFunctionName.SET);
    
        if (inline) {
    	return new TensorFunctions(data).newSetInline(elementType, numDims);
        } else {
    	return new TensorFunctions(data).newSet(elementType, numDims);
        }
    
    }
    },
    */

    /**
     * Creates a new instance of the function 'set_matrix_values', which sets all the positions of the matrix to the
     * given value (returns nothing).
     * 
     * FunctionInstance receives:<br>
     * - An allocated matrix;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * - A value of the matrix type, representing the value that will be set in the matrix positions;<br>
     * 
     */
    SET_MATRIX_VALUES {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return TensorFunctions.newSetMatrixValues(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'new_array', which creates a row matrix with the given values. Returns the
     * created matrix.
     * 
     * <p>
     * FunctionInstance receives:<br>
     * - A variable number of values of element type, representing the values of the row matrix;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of values of element type, representing the values of the row matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * 
     */
    NEW_ROW_NUMERIC {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // Number of elements
            int numElements = inputTypes.size();

            // Check if last argument is a pointer to matrix
            VariableType matrixType = inputTypes.get(inputTypes.size() - 1);
            if (MatrixUtils.isMatrix(matrixType)) {
                numElements -= 1;
            }

            // TODO: Element type needs to consider more things:
            // - if output is defined in ProviderData;
            // - all the types of the elements
            VariableType elementType = inputTypes.get(0);

            // return TensorCreationFunctions.newRowArrayNumeric(elementType, numElements);
            List<Integer> shape = Arrays.asList(1, numElements);
            return new TensorCreationFunctions(data).newArrayWithValues(elementType, shape);
        }
    },

    /**
     * Creates a new instance of the function 'new_array', which creates a column matrix with the given values. Returns
     * the created matrix.
     * 
     * <p>
     * FunctionInstance receives:<br>
     * - A variable number of values of element type, representing the values of the row matrix;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of values of element type, representing the values of the row matrix;<br>
     * - A pointer to an allocated matrix, which will be used to store the new matrix;<br>
     * 
     * 
     */
    NEW_COL_NUMERIC {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
            List<VariableType> inputTypes = data.getInputTypes();

            // Number of elements
            int numElements = inputTypes.size();

            // Check if last argument is a pointer to matrix
            VariableType matrixType = inputTypes.get(inputTypes.size() - 1);
            if (MatrixUtils.isMatrix(matrixType)) {
                numElements -= 1;
            }

            // TODO: Element type needs to consider more things:
            // - if output is defined in ProviderData;
            // - all the types of the elements
            VariableType elementType = inputTypes.get(0);

            List<Integer> shape = Arrays.asList(numElements, 1);
            return new TensorCreationFunctions(data).newArrayWithValues(elementType, shape);
        }
    },

    /**
     * Creates a new instance of a function which creates a diagonal matrix with the values set to 1. Returns the
     * created matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of integers (1 or 2), representing the size of each dimension of the matrix;<br>
     * - A variable type indicating the type of the matrix
     * 
     * 
     */
    EYE {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // Number of elements
            int numDims = inputTypes.size() - 1;

            // Check if last argument is a pointer to matrix
            /*
            VariableType matrixType = inputTypes.get(inputTypes.size() - 1);
            	    if (MatrixUtils.isMatrix(matrixType)) {
            		numDims -= 1;
            	    }
            */
            // Element type is derived from the string argument
            // VariableType numericClassname = inputTypes.get(inputTypes.size() - 2);

            // VariableType elementType = inputTypes.get(0);
            VariableType elementType = inputTypes.get(2);

            return new TensorCreationFunctions(data).newEyeArray(elementType, numDims, data.newInstance());
        }
    },

    /**
     * Creates a new instance of the function 'copy', which copies the contents of a matrix to another.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - An allocated matrix with the same shape, which will be the sink;
     * 
     */
    COPY {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return new TensorFunctions(data).newCopy(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'dimSize', which returns the size of the indicated dimension. Uses
     * zero-based indexing.
     * 
     * <p>
     * getInstance receives:<br>
     * - A matrix whose element type will define the instance;
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - The index of the dimension;
     * 
     */
    DIM_SIZE {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            // Check which 'dim_size' version should be used (inlined or function)
            // Boolean inline = data.getSetup().getBoolean(CirOption.INLINE_DYNAMIC_FUNCTIONS);
            boolean inline = data.getSettings().get(CirKeys.INLINE).inline(MatrixFunctionName.DIM_SIZE);
            if (inline) {
                return new TensorFunctions(data).newDimSizeInline(elementType);
            }

            return new TensorFunctions(data).newDimSize(elementType);

            // return TensorFunctions.newDimSize(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'length_alloc'. Returns the maximum dimension of the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * 
     */
    LENGTH {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return new TensorFunctions(data).newLength(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'is_same_shape_alloc'. Returns 1 (true) if the given shape is the same as
     * the input matrix, or false otherwise.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix; <br>
     * - A pointer to integer, representing an array with the shape;<br>
     * - An integer with the number of dimensions of the shape;<br>
     * 
     * <p>
     * A new Instance needs: - An allocated matrix; <br>
     * 
     */
    IS_SAME_SHAPE {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix type
            VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            VariableType elementType = MatrixUtils.getElementType(matrixType);

            return new TensorFunctions(data).newIsSameShape(elementType);
        }
    },

    /**
     * Creates a new instance of the function 'size', which returns a row vector with the size of given matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix, which will be the source;<br>
     */
    SIZE {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            // If input matrix is one dimensional, use numel instead
            /*
            MatrixType matrix = data.getInputType(MatrixType.class, 0);
            if (matrix.matrix().getShape().getNumDims() == 1 && data.getInputTypes().size() == 1) {
            return matrix.matrix().functions().numel().newCInstance(data);
            }
            */

            return new Size(data).create();
        }
    },

    /**
     * Creates a new instance of the function 'transpose', which returns a matrix which is the transpose of the input.
     * 
     * <p>
     * getInstance:<br>
     * - The matrix type from which the element type is determined;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - A pointer to an allocated matrix, which will be the output;<br>
     * 
     */
    TRANSPOSE {
        @Override
        public FunctionInstance newCInstance(ProviderData data) {

            List<VariableType> inputTypes = data.getInputTypes();

            // First argument is the matrix type
            MatrixType matrixType = (MatrixType) CirFunctionsUtils.getMatrixTypeByIndex(this, inputTypes, 0);
            // VariableType elementType = MatrixUtilsV2.getElementType(matrixType);

            return new TensorFunctions(data).newTranspose(matrixType);
        }
    };

    /**
     * Creates a function instance of a matrix function based on the given list of input types.
     * 
     * @param variableType
     * @return
     */
    // public abstract FunctionInstance getInstance(List<VariableType> inputTypes);

    /**
     * Helper method with variadic inputs.
     * 
     * @param inputTypes
     * @return
     */
    /*
    public FunctionInstance getInstance(VariableType... inputTypes) {
    
    return getInstance(data);
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
