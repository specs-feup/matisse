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

package org.specs.CIRFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.TypesOld.MatrixUtils.MatrixImplementation;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRFunctions.MatrixDec.DeclaredProvider;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixFunctions;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixFunctions;

/**
 * Provider for matrix-related functions which receive the same inputs either with declared or allocated
 * implementations.
 * 
 * TODO: where possible, replace usages with calls to MatrixType.matrix().functions()
 * 
 * @author Joao Bispo
 * 
 */
public enum MatrixFunction implements InstanceProvider {

    /**
     * Gets the address for the first element of the matrix.
     */
    GET_DATA_PTR {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.GET_DATA_PTR;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.GET_DATA_PTR;
	}
    },

    /**
     * Function Call receives a matrix as input.
     */
    /*
    NUMEL {
    
    @Override
    protected InstanceProvider getTensorProvider() {
        return new DynamicMatrixFunctions().numel();
        // return TensorProvider.NUMEL;
    }
    
    @Override
    protected InstanceProvider getDeclaredProvider() {
        return new StaticMatrixFunctions().numel();
        // return DeclaredProvider.NUMEL;
    }
    
    },
    */

    /**
     * Function Call receives a matrix as input.
     */
    PRINT_DIM {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.PRINT_DIM;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.PRINT_DIM;
	}
    },

    /**
     * Creates an inlined version of the function 'get', which returns the contents of a matrix in the specified index.
     * The index can be represented with a variable number of integers.
     * 
     * <p>
     * getInstance receives:<br>
     * - Matrix from which the element type is extracted;<br>
     * - As many arguments as the number of dimensions;<br>
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A matrix, whose values will be read;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     */
    GET {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return new DynamicMatrixFunctions().get();
	    // return TensorProvider.GET;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return new StaticMatrixFunctions().get();
	    // return DeclaredProvider.GET;
	}
    },

    /**
     * Creates a new instance of the function 'set', which sets a single value in a matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A matrix, whose values will be set;<br>
     * - A variable number of integers with the indexes of the matrix. Assumes zero-based indexing;<br>
     * - A value of the same type as the matrix 'elementType', which will be used to set the matrix at the specified
     * index;<br>
     * 
     * @deprecated Use MatrixType.matrix().functions().set() instead
     */
    @Deprecated SET {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return new DynamicMatrixFunctions().set();
	    // return TensorProvider.SET;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    // return DeclaredProvider.SET;
	    return new StaticMatrixFunctions().set();
	}
    },

    /**
     * 
     */
    DIM_SIZE {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.DIM_SIZE;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.DIM_SIZE;
	}
    },

    /**
     * 
     */
    GET_ROW_VIEW {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.GET_ROW_VIEW;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.GET_ROW_VIEW;
	}
    },

    /**
     * 
     */
    GET_ROW_VIEW_POINTER {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.GET_ROW_VIEW_POINTER;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.GET_ROW_VIEW;
	}
    },

    /**
     * 
     */
    GET_ROW_VIEW_POINTER_OUT_POINTER {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.GET_ROW_VIEW_POINTER_OUT_POINTER;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    throw new RuntimeException("Not implemented");
	}
    },

    /**
     * 
     */
    /*
    SET_ROW {
    @Override
    protected InstanceProvider getTensorProvider() {
        LoggingUtils.msgWarn("Check if correct");
        return TensorProvider.GET_ROW_VIEW;
    }
    
    @Override
    protected InstanceProvider getDeclaredProvider() {
        LoggingUtils.msgWarn("Check if correct");
        return DeclaredProvider.GET_ROW_VIEW;
    }
    },
    */
    /**
     * 
     */
    COPY {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.COPY;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.COPY;
	}
    },

    /**
     * TODO: DOES NOT WORK FOR TWO ARGUMENTS
     */
    SIZE {
	@Override
	protected InstanceProvider getTensorProvider() {
	    return TensorProvider.SIZE;
	}

	@Override
	protected InstanceProvider getDeclaredProvider() {
	    return DeclaredProvider.SIZE;
	}
    };

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
    /*
    NEW_COL_MATRIX {
    @Override
    protected InstanceProvider getTensorProvider() {
        return MatrixCol.getColWithMatricesProvider();
    }
    
    @Override
    protected InstanceProvider getDeclaredProvider() {
        return MatrixCol.getColWithMatricesProvider();
    }
    
    };
    */

    // protected abstract TensorProvider getTensorProvider();

    // protected abstract DeclaredProvider getDeclaredProvider();
    /**
     * @return the InstanceProvider for Tensor implementation
     */
    protected abstract InstanceProvider getTensorProvider();

    /**
     * 
     * @return the InstanceProvider for Declared implementation
     */
    protected abstract InstanceProvider getDeclaredProvider();

    /**
     * Helper method with variadic inputs.
     * 
     * @param impl
     * @param inputTypes
     * @return
     */
    /*
    public FunctionInstance getInstance(MatrixImplementation impl, VariableType... inputTypes) {
    return getInstance(impl, data);
    }
    */

    /**
     * Creates a function instance of a matrix function based on the given matrix type.
     * 
     * @param impl
     * @param data
     * 
     * @return
     */
    public FunctionInstance getInstance(MatrixImplementation impl, ProviderData data) {
	return getProvider(impl).newCInstance(data);
    }

    public InstanceProvider getProvider() {
	return new MatrixProviderReal(this);
    }

    public InstanceProvider getProvider(MatrixImplementation impl) {
	return new MatrixProviderReal(this, impl);
    }

    /**
     * Infers the matrix implementation from the first matrix found in the input types.
     * 
     */
    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	return getProvider().newCInstance(data);
    }

    /*
    public CToken getFunctionCall(CToken... arguments) {
    return CirFunctionsUtils.getFunctionCall(this, arguments);
    // return getFunctionCall(Arrays.asList(arguments));
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
