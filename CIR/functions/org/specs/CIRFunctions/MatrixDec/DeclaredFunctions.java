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

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.SpecsIo;

/**
 * Contains functions to manipulate and create declared matrixes.
 * 
 * @author Joao Bispo
 * 
 */
public class DeclaredFunctions {

    private final NumericFactory numerics;

    /**
     * 
     */
    public DeclaredFunctions(NumericFactory numerics) {
        this.numerics = numerics;
    }

    /**
     * Helper method which uses the setup inside a ProviderData.
     * 
     * @param data
     */
    public DeclaredFunctions(ProviderData data) {
        this(data.getNumerics());
    }

    private NumericFactory getNumerics() {
        return numerics;
    }

    /**
     * Creates an instance that returns the number of dimensions in a declared matrix.
     * 
     * @param declaredMatrix
     * @return
     */
    public FunctionInstance newNdimsDec(MatrixType declaredMatrix) {

        // final int numDims = MatrixUtils.getNumDims(declaredMatrix);
        // final int numDims = MatrixUtils.getNumDims(declaredMatrix);

        // Use the size of the dimension list as the number of dimensions, so that a vector returns 2.
        final int numDims = declaredMatrix.getTypeShape().getDims().size();

        String functionName = "ndims_dec_" + numDims;
        // String functionName = "ndims_dec";

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(declaredMatrix),
                getNumerics().newInt());

        InlineCode inlineCode = arguments -> Integer.toString(numDims);
        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    /**
     * Creates an instance that returns the length in a declared matrix.
     * 
     * @param declaredMatrix
     * @return
     */
    public FunctionInstance newLengthDec(VariableType declaredMatrix) {

        // final Integer length = DeclaredFunctionsUtils.getMaxDim(declaredMatrix);
        final Integer length = MatrixUtils.getShape(declaredMatrix).getMaxDim();

        // Function name is specialized to it can return the constant with the return type
        String functionName = "length_dec_" + length;

        // Build return type with constant
        VariableType returnType = getNumerics().newInt(length);

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(declaredMatrix),
                returnType);

        InlineCode inlineCode = arguments -> Integer.toString(length);

        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    public FunctionInstance newGetRowView(MatrixType declaredMatrix, int length) {

        // Get the matrix data
        // MatrixDecData matrixData = VariableTypeContent.getMatrixDec(declaredMatrix);

        // The output dimensions
        final List<Integer> rowShape = Arrays.asList(1, length);
        VariableType elementType = MatrixUtils.getElementType(declaredMatrix);
        VariableType outputType = StaticMatrixType.newInstance(elementType, rowShape);

        // Name of the function
        String functionName = "get_row_view_dec_" + elementType.getSmallId() + "_"
                + declaredMatrix.getTypeShape().getString();

        VariableType intType = getNumerics().newInt();

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(
                Arrays.asList(declaredMatrix, intType, intType), outputType);

        InlineCode inlineCode = arguments -> {
            StringBuilder builder = new StringBuilder();

            CNode inputMatrix = arguments.get(0);
            builder.append(inputMatrix.getCode());
            builder.append(" + (");

            // Get offset
            CNode offset = arguments.get(1);

            // Check if offset is integer
            /*
            		VariableType type = DiscoveryUtils.getVarType(offset);
            
            		if(!VariableTypeUtils.isInteger(type)) {
            		    builder.append("(int) ");
            		}
            */
            builder.append(offset.getCode());
            builder.append(")");

            return builder.toString();
        };

        InlinedInstance instance = new InlinedInstance(functionTypes, functionName, inlineCode);

        // instance.setCustomImplementationIncludes(SystemInclude.Stdio.getIncludeName());

        return instance;
    }

    /**
     * Creates a new instance of the function 'print_dim_dec'.
     * 
     * <p>
     * FunctionCall receives one declared matrix as input and has void return type. Prints the contents of the matrix.
     * 
     * @return
     */
    public static FunctionInstance newPrintDim(VariableType declaredMatrix) {

        // Get the matrix data
        // MatrixDecData matrixData = VariableTypeContent.getMatrixDec(declaredMatrix);

        // The dimensions
        // final List<Integer> dimensions = matrixData.getShape();
        final List<Integer> dimensions = MatrixUtils.getShapeDims(declaredMatrix);

        String functionName = "print_dim_dec";

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(declaredMatrix),
                VoidType.newInstance());

        InlineCode inlineCode = arguments -> "printf(\"" + dimensions.toString() + "\")";

        InlinedInstance instance = new InlinedInstance(functionTypes, functionName, inlineCode);

        // TODO: Check if bug, if it should be CustomCall
        // LoggingUtils.msgWarn("CHECK");
        // instance.setCustomImplementationIncludes(SystemInclude.Stdio.getIncludeName());
        instance.setCustomCallIncludes(SystemInclude.Stdio.getIncludeName());

        return instance;
    }

    /**
     * Creates a new instance of the function 'dec_set_inline', which sets a single value in a declared matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, whose values will be set;<br>
     * - A variable number of integers with the indexes of the matrix that will be set. Function assumes zero-based
     * indexing;<br>
     * - A value of type 'elementType', which will be used to set the matrix at the specified index;<br>
     * 
     * @param string
     * 
     * @param matrixType
     * @param useLinearArrays
     * @return
     */
    /*
    static InstanceProvider newSetInline(final int numIndexes) {
    return new InstanceProvider() {
    
        @Override
        public FunctionInstance newCInstance(ProviderData data) {
    	return new DeclaredFunctions(data.getSetup()).newSetInlineInstance(data.getInputTypes(), numIndexes);
        }
    };
    }
    */

    // static FunctionInstance newSetInline(VariableType matrixType, int numIndexes) {
    /*
      private FunctionInstance newSetInlineInstance(List<VariableType> types, int numIndexes) {
    
    	// First argument is the matrix to access
    	// VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex("SetInline", data.getInputTypes(), 0);
    	VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex("SetInline", types, 0);
    
    	VariableType elementType = MatrixUtilsV2.getElementType(matrixType);
    
    	// Name of the function
    	String functionName = "dec_set_inline_" + elementType.getSmallId();
    
    	// Input names
    	String tensorName = "t";
    	String indexPrefix = "index_";
    	String valueName = "value";
    
    	// List<String> inputNames = Arrays.asList(tensorName, indexName, valueName);
    	List<String> inputNames = FactoryUtils.newArrayList();
    	inputNames.add(tensorName);
    	inputNames.addAll(FunctionInstanceUtils.createNameList(indexPrefix, numIndexes));
    	inputNames.add(valueName);
    
    	// Input types
    	List<VariableType> inputTypes = FactoryUtils.newArrayList();
    
    	// Matrix
    	inputTypes.add(matrixType);
    
    	// Indexes
    	for (int i = 0; i < numIndexes; i++) {
    	    inputTypes.add(getNumerics().newInt());
    	}
    
    	// value
    	inputTypes.add(elementType);
    
    	// FunctionTypes
    	FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());
    
    	InlineCode inlineCode = new InlineCode() {
    
    	    @Override
    	    public String getInlineCode(List<CNode> arguments) {
    		StringBuilder builder = new StringBuilder();
    
    		// First argument is a variable with the matrix, get matrix shape
    		CNode varToken = arguments.get(0);
    		Variable var = ((VariableNode) varToken).getVariable();
    		List<Integer> dims = MatrixUtilsV2.getShape(var.getType()).getDims();
    
    		// Second argument to before last are indexes
    		List<CNode> indexes = arguments.subList(1, arguments.size() - 1);
    
    		CNode linearIndex = new IndexUtils(setup).getLinearCIndex(dims, indexes);
    		// CToken linearIndex = new DeclaredFunctionsUtils(data).getLinearCIndex(var.getType(), indexes);
    
    		// Last argument is the value
    		CNode value = arguments.get(arguments.size() - 1);
    
    		builder.append(var.getName());
    		builder.append("[");
    		builder.append(linearIndex.getCode());
    		builder.append("] = ");
    		builder.append(value.getCode());
    
    		return builder.toString();
    	    }
    	};
    
    	return new InlinedInstance(functionTypes, functionName, inlineCode);
    
        }
    */
    /**
     * Creates a new instance of the function 'copy', which copies the contents of one matrix to other.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source;<br>
     * - A declared matrix with the same shape, which will be the sink; - An integer with the number of elements in the
     * matrix;
     * 
     * @return
     */
    static FunctionInstance newCopyV2(MatrixType matrixType) {
        // Integer numElements = MatrixUtils.getMatrixLength(matrixType);
        Integer numElements = matrixType.getTypeShape().getNumElements();

        VariableType elementType = MatrixUtils.getElementType(matrixType);

        // Name of the function
        String functionName = "copy_dec_" + elementType.getSmallId() + "_" + numElements;

        // Input names
        String inputName = "input";
        String outputName = "output";

        List<String> inputNames = Arrays.asList(inputName, outputName);

        // Input types
        List<VariableType> inputTypes = Arrays.asList(matrixType, matrixType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        String cBody = SpecsIo.getResource(DeclaredResource.COPY_BODY.getResource());

        cBody = cBody.replace("<NUM_ELEMENTS>", numElements.toString());

        LiteralInstance copy = new LiteralInstance(fTypes, functionName, CirFilename.DECLARED.getFilename(), cBody);

        return copy;
    }

    /**
     * Creates a new instance of the function 'dimSize', which returns the size of the indicated dimension.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source;<br>
     * - The index of the dimension;
     * 
     * @param matrixType
     * @param useLinearArrays
     * @return
     */
    FunctionInstance newDimSize(VariableType matrixType) {

        // List<Integer> shape = MatrixUtils.getShape(matrixType);

        // Name of the function
        String functionName = "dim_size_dec";

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(
                Arrays.asList(matrixType, getNumerics().newInt()), getNumerics().newInt());

        InlineCode inlineCode = arguments -> {
            // First argument is a variable with the matrix
            CNode varToken = arguments.get(0);
            assert varToken instanceof VariableNode;
            Variable var = ((VariableNode) varToken).getVariable();

            List<Integer> shape = MatrixUtils.getShapeDims(var.getType());

            // Second argument is the index
            CNode index = arguments.get(1);
            List<VariableType> indexTypes = CNodeUtils.getVariableTypes(index);
            // int dim = VariableTypeContent.getNumeric(indexTypes.get(0)).getIntValue();
            int dim = ScalarUtils.getConstant(indexTypes.get(0)).intValue();

            if (shape.size() <= dim) {
                return "1";
            }

            return shape.get(dim).toString();
        };

        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    /**
     * Creates a new instance that returns the ( 0-based ) index of the first non-singleton dimensions of the input
     * matrix.
     * 
     * @param matrix
     *            - the input matrix
     * @return a {@link FunctionInstance} with the correct implementation
     */
    FunctionInstance newFirstNonSingletonDim(MatrixType matrix) {

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(matrix), getNumerics()
                .newInt());

        String functionName = "new_first_non_singleton_dimension";

        // final Integer dim = MatrixUtils.firstNonSingletonDimension(matrix);
        final Integer dim = matrix.getTypeShape().getFirstNonSingletonDimension();

        InlineCode inlineCode = arguments -> dim.toString();

        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    /**
     * Creates a new instance that returns the size of the first non-singleton dimension of the input matrix.
     * 
     * @param matrix
     *            - the input matrix
     * @return a {@link FunctionInstance} with the correct implementation
     */
    FunctionInstance newFirstNonSingletonDimSize(final MatrixType matrix) {

        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(matrix), getNumerics()
                .newInt());

        String functionName = "new_first_non_singleton_dimension_size";

        // Get the index of the first non-singleton dimension
        final Integer dim = matrix.getTypeShape().getFirstNonSingletonDimension();

        List<Integer> dims = matrix.getTypeShape().getDims();

        // Get the size of the first non-singleton dimension
        final Integer dimSize = dims.get(dim);

        InlineCode inlineCode = arguments -> dimSize.toString();

        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    public FunctionInstance newGetDataPointerInlined(List<VariableType> inputTypes) {
        // List<VariableType> inputTypes = getData().getInputTypes();
        checkState(inputTypes.size() == 1);
        StaticMatrixType matrixType = (StaticMatrixType) inputTypes.get(0);
        VariableType outputBaseType = matrixType.getElementType();
        VariableType outputType = ReferenceUtils.getType(outputBaseType, true);

        FunctionType functionType = FunctionType.newInstanceNotImplementable(inputTypes, outputType);
        String functionName = "get_data_ptr$" + matrixType.getSmallId();

        InlineCode code = arguments -> arguments.get(0).getCode();

        return new InlinedInstance(functionType, functionName, code);
    }

}
