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

import static com.google.common.base.Preconditions.checkState;
import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getFilename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.CodeGenerator.MatrixCode;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.Utilities.AssignmentUtils;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.LowLevel.IsSameShape;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.Void.VoidType;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

/**
 * Contains functions to manipulate and create allocated matrixes.
 * 
 * @author Joao Bispo
 * 
 */
public class TensorFunctions extends CirBuilder {

    private final ProviderData data;

    /**
     * @param data
     */
    public TensorFunctions(ProviderData data) {
        super(data);

        this.data = data;
    }

    private ProviderData getData() {
        return data;
    }

    /**
     * Creates a new instance of the function 'numel', which returns an integer representing the number of elements in
     * the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;
     * 
     * @return
     */
    /*
    // static FunctionInstance newNumel(VariableType elementType) {
    FunctionInstance newNumel(ProviderData data) {
    List<VariableType> inputs = data.getInputTypes();
    
    // Should have only one argument, of type matrix
    VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex("numel", inputs, 0);
    VariableType elementType = MatrixUtilsV2.getElementType(matrixType);
    
    // Name of the function
    String functionName = "numel_alloc_" + elementType.getSmallId();
    
    // Input names
    String tensorName = "t";
    String valueName = "value";
    
    List<String> inputNames = Arrays.asList(tensorName);
    
    // Input types
    VariableType tensorType = DynamicMatrixType.newInstance(elementType);
    List<VariableType> inputTypes = Arrays.asList(tensorType);
    
    // FunctionTypes
    FunctionTypes fTypes = FunctionTypes.newInstance(inputNames, inputTypes, valueName, numerics().newInt());
    
    String cBody = IoUtils.getResourceString(TensorResource.NUMEL_BODY.getResource());
    
    String tensorLengthTag = "<TENSOR_LENGTH>";
    cBody = cBody.replace(tensorLengthTag, DynamicMatrixStruct.TENSOR_LENGTH);
    
    LiteralInstance length = new LiteralInstance(fTypes, functionName, getFilename(), cBody);
    
    // Add comments
    String comments = IoUtils.getResourceString(TensorResource.NUMEL_COMMENTS.getResource());
    comments = TensorTemplate.parseTemplate(comments);
    length.setComments(comments);
    
    return length;
    }
    */

    /**
     * Creates a new instance of the function 'print_dim_alloc', which prints the contents of the matrix (returns
     * nothing).
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;
     * 
     * @return
     */
    static FunctionInstance newPrintDim(VariableType elementType) {

        // Name of the function
        String functionName = "print_dim_alloc_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        List<String> inputNames = Arrays.asList(tensorName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        List<VariableType> inputTypes = Arrays.asList(tensorType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        String cBody = SpecsIo.getResource(DynamicMatrixResource.PRINT_BODY.getResource());

        String tensorShapeTag = "<TENSOR_SHAPE>";
        cBody = cBody.replace(tensorShapeTag, DynamicMatrixStruct.TENSOR_SHAPE);

        String tensorDimsTag = "<TENSOR_DIMS>";
        cBody = cBody.replace(tensorDimsTag, DynamicMatrixStruct.TENSOR_DIMS);

        LiteralInstance print = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        // print.setCustomImplementationIncludes(SystemInclude.Stdio.getIncludeName());
        print.setCustomImplementationIncludes(SystemInclude.Stdio);

        return print;
    }

    /**
     * Creates a new instance of the function 'tensor_set_inline', which sets a single value in an allocated matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be set;<br>
     * - A variable number of integers with the indexes of the matrix that will be set. Index assumes zero-based
     * indexing;<br>
     * - A value of type 'tensorType', which will be used to set the allocated matrix at the specified index;<br>
     * 
     * @param tensorType
     * @param useLinearArrays
     * @return
     */
    FunctionInstance newSetInline(VariableType elementType, int numIndexes) {

        // Name of the function
        String functionName = "set_tensor_inline_" + elementType.getSmallId() + "_" + numIndexes;

        // Input names
        String tensorName = "t";
        String indexPrefix = "index_";
        String valueName = "value";

        List<String> inputNames = SpecsFactory.newArrayList();
        inputNames.add(tensorName);
        inputNames.addAll(FunctionInstanceUtils.createNameList(indexPrefix, numIndexes));
        inputNames.add(valueName);

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // Matrix
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(tensorType);

        // Indexes
        for (int i = 0; i < numIndexes; i++) {
            inputTypes.add(getNumerics().newInt());
        }

        // Value
        inputTypes.add(elementType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        // Prepare dependencies set
        Set<FunctionInstance> dependentInstances = SpecsFactory.newHashSet();

        // Instance for sub2ind. Initialize it here if necessary.
        final FunctionInstance sub2indInst = getSub2IndInstance(elementType, numIndexes);
        if (sub2indInst != null) {
            dependentInstances.add(sub2indInst);
        }
        dependentInstances
                .addAll(AssignmentUtils.getAssignmentInstances(tensorType, tensorType, getData()));

        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                // Second arg is index

                // First argument is the tensor variable
                CNode tensorArg = arguments.get(0);

                String index = null;
                // If only one index, return code of the subscript itself
                if (arguments.size() == 3) {
                    index = arguments.get(1).getCode();
                }
                // Call function that transforms subscripts into indexes
                else {

                    List<CNode> newArgs = SpecsFactory.newArrayList();

                    // Add tensor
                    newArgs.add(arguments.get(0));

                    // Get indexes. Remaining arguments except last are the index subscripts
                    List<CNode> indexes = arguments.subList(1, arguments.size() - 1);

                    // Add simplified indexes
                    newArgs.addAll(CodeGeneratorUtils.getSimplifiedIndexes(indexes));

                    // Build call
                    if (sub2indInst == null) {
                        throw new RuntimeException("sub2ind instance is null!");
                    }
                    index = sub2indInst.getCallCode(newArgs);
                }

                // Third argument is value
                CNode valueToken = arguments.get(arguments.size() - 1);
                // String valueCode = CodeGeneratorUtils.tokenCode(valueToken);

                // Build code for array access
                // return MatrixCode.getAllocStore(tensorArgName, tensorArgType, index, valueCode);
                return MatrixCode.getAllocStore(tensorArg, index, valueToken, getData());
            }
        };

        InlinedInstance getInline = new InlinedInstance(fTypes, functionName, inlineCode);

        // Add dependencies
        getInline.setCallInstances(dependentInstances);

        return getInline;

    }

    /**
     * Creates a new instance of the function 'tensor_set', which sets a single value in an allocated matrix. Uses
     * zero-based indexing.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be set;<br>
     * - A variable number of integers with the indexes of the matrix that will be set. Index assumes zero-based
     * indexing;<br>
     * - A value of type 'tensorType', which will be used to set the allocated matrix at the specified index;<br>
     * 
     * @param tensorType
     * @param useLinearArrays
     * @return
     */
    FunctionInstance newSet(VariableType elementType, int numIndexes) {

        // Name of the function
        String functionName = "set_tensor_" + elementType.getSmallId() + "_" + numIndexes;

        // Input names
        String tensorName = "t";
        String indexPrefix = "index_";
        String valueName = "value";

        // Indexes
        List<String> indexNames = FunctionInstanceUtils.createNameList(indexPrefix, numIndexes);
        List<VariableType> indexTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numIndexes; i++) {
            indexTypes.add(getNumerics().newInt());
        }

        List<String> inputNames = SpecsFactory.newArrayList();
        inputNames.add(tensorName);
        inputNames.addAll(indexNames);
        inputNames.add(valueName);

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // Matrix
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(tensorType);
        inputTypes.addAll(indexTypes);
        /*
        // Indexes
        for (int i = 0; i < numIndexes; i++) {
            inputTypes.add(numerics().newInt());
        }
        */

        // Value
        inputTypes.add(elementType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        // Prepare dependencies set
        Set<FunctionInstance> dependentInstances = SpecsFactory.newHashSet();

        // Instance for sub2ind. Initialize it here if necessary.
        final FunctionInstance sub2indInst = getSub2IndInstance(elementType, numIndexes);
        if (sub2indInst != null) {
            dependentInstances.add(sub2indInst);
        }

        // Get template
        String body = SpecsIo.getResource(DynamicMatrixResource.SET_VALUE_BODY);

        // Sub2Idx call code
        List<CNode> indexArgs = SpecsFactory.newArrayList();
        // Add tensor
        indexArgs.add(CNodeFactory.newVariable(tensorName, tensorType));
        // Add indexes
        for (int i = 0; i < numIndexes; i++) {
            indexArgs.add(CNodeFactory.newVariable(indexNames.get(i), indexTypes.get(i)));
        }

        // Get code
        String sub2indCode = null;
        if (sub2indInst != null) {
            sub2indCode = sub2indInst.getCallCode(indexArgs);
        } else {
            sub2indCode = indexNames.get(0);
        }

        // Replace tags
        body = body.replace("<SUB2IDX_CALL>", sub2indCode);

        body = body.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
        body = body.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
        body = body.replace("<FUNCTION_NAME>", functionName);

        // Create instance
        LiteralInstance getInstance = new LiteralInstance(fTypes, functionName, getFilename(), body);

        // Add dependencies
        getInstance.getCustomImplementationInstances().add(dependentInstances);

        getInstance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);

        return getInstance;

    }

    /**
     * Creates a new instance of the function 'ndims_alloc', which returns the number of dimensions of an allocated
     * matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * 
     * @param tensorType
     * @param useLinearArrays
     * @return
     */
    public FunctionInstance newNdims(MatrixType matrixType) {
        VariableType elementType = MatrixUtils.getElementType(matrixType);

        Optional<Integer> constant = getDims(matrixType);

        // Name of the function
        String functionName = "ndims_alloc_" + elementType.getSmallId() + constant.map(dim -> "$" + dim).orElse("");

        // Input names
        String tensorName = "t";
        List<String> inputNames = Arrays.asList(tensorName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        List<VariableType> inputTypes = Arrays.asList(tensorType);

        // FunctionTypes
        NumericTypeV2 outputType = getNumerics().newInt(constant);
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, "value", outputType);

        // Apply changes to default fields
        if (constant.isPresent()) {
            int dim = constant.get();
            return new InlinedInstance(fTypes, functionName, tokens -> Integer.toString(dim));
        }

        String body = "   return " + tensorName + "->dims;\n";
        LiteralInstance ndimsTensor = new LiteralInstance(fTypes, functionName, getFilename(), body);

        return ndimsTensor;

    }

    private static Optional<Integer> getDims(MatrixType matrixType) {
        TypeShape shape = matrixType.getTypeShape();
        int rawNumDims = shape.getRawNumDims();
        if (rawNumDims <= 0) {
            return Optional.empty();
        }

        if (rawNumDims <= 2) {
            return Optional.of(2);
        }
        if (shape.getDim(rawNumDims - 1) >= 0) {
            return Optional.of(rawNumDims);
        }

        return Optional.empty();
    }

    /**
     * Creates an inlined version of the function 'get_tensor_inline', which returns the contents of an allocated matrix
     * in the specified index. The index can be represented with a variable number of integers.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be read;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     * 
     * @param tensorType
     * @param useLinearArrays
     * @return
     */
    /*
    FunctionInstance newGetInline(VariableType elementType, int numArgs) {
    
    // Name of the function
    String functionName = "get_tensor_inline_" + elementType.getSmallId() + "_" + numArgs;
    
    // Input names
    List<String> inputNames = FactoryUtils.newArrayList();
    String tensorName = "t";
    inputNames.add(tensorName);
    
    final String indexPrefix = "index_";
    inputNames.addAll(InstanceUtils.getNameList(indexPrefix, numArgs));
    
    // Input types
    List<VariableType> inputTypes = FactoryUtils.newArrayList();
    VariableType tensorType = DynamicMatrixType.newInstance(elementType);
    inputTypes.add(tensorType);
    
    VariableType intType = numerics().newInt();
    for (int i = 0; i < numArgs; i++) {
        inputTypes.add(intType);
    }
    
    // FunctionTypes
    FunctionTypes fTypes = FunctionTypes.newInstance(inputNames, inputTypes, null, elementType);
    
    // Prepare dependencies set
    Set<FunctionInstance> dependentInstances = FactoryUtils.newHashSet();
    
    // Instance for sub2ind. Initialize it here if necessary.
    final FunctionInstance sub2indInst = getSub2IndInstance(elementType, numArgs);
    if (sub2indInst != null) {
        dependentInstances.add(sub2indInst);
    }
    
    InlineCode inlineCode = new InlineCode() {
    
        @Override
        public String getInlineCode(List<CToken> arguments) {
    
    	// First argument is the tensor variable
    	String tensorArgName = CodeGeneratorUtils.tokenCode(arguments.get(0));
    	VariableType tensorArgType = DiscoveryUtils.getVarType(arguments.get(0));
    
    	String index = null;
    	// If only one index, return code of the subscript itself
    	if (arguments.size() == 2) {
    	    index = CodeGeneratorUtils.tokenCode(arguments.get(1));
    	}
    	// Call function that transforms subscripts into indexes
    	else {
    
    	    List<CToken> newArgs = FactoryUtils.newArrayList();
    
    	    // Add tensor
    	    newArgs.add(arguments.get(0));
    
    	    // Get indexes. Remaining arguments except last are the index subscripts
    	    List<CToken> indexes = arguments.subList(1, arguments.size());
    
    	    // Add simplified indexes
    	    newArgs.addAll(CodeGeneratorUtils.getSimplifiedLiterals(indexes));
    
    	    // Simplify arguments
    	    // List<CToken> newArgs = CodeGeneratorUtils.getSimplifiedLiterals(arguments);
    
    	    // Build call
    	    index = sub2indInst.getCallCode(newArgs);
    	}
    
    	// Build code for array access
    	return MatrixCode.getAllocAccess(tensorArgName, tensorArgType, index);
    
        }
    
    };
    
    InlinedInstance getInline = new InlinedInstance(fTypes, functionName, inlineCode);
    
    // Add dependencies
    getInline.setCallInstances(dependentInstances);
    
    return getInline;
    
    }
    */

    /**
     * Creates the function 'get_tensor', which returns the contents of an allocated matrix in the specified index. The
     * index can be represented with a variable number of integers.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be read;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     * 
     * @param tensorType
     * @param useLinearArrays
     * @return
     */
    /*
    FunctionInstance newGet(VariableType elementType, int numArgs) {
    
    // Name of the function
    String functionName = "get_tensor_" + elementType.getSmallId() + "_" + numArgs;
    
    // Input names
    String tensorName = "t";
    String indexPrefix = "index_";
    
    // Indexes
    List<String> indexNames = InstanceUtils.getNameList(indexPrefix, numArgs);
    List<VariableType> indexTypes = FactoryUtils.newArrayList();
    for (int i = 0; i < numArgs; i++) {
        indexTypes.add(numerics().newInt());
    }
    
    List<String> inputNames = FactoryUtils.newArrayList();
    inputNames.add(tensorName);
    inputNames.addAll(indexNames);
    
    // Input types
    List<VariableType> inputTypes = FactoryUtils.newArrayList();
    
    // Matrix
    VariableType tensorType = DynamicMatrixType.newInstance(elementType);
    inputTypes.add(tensorType);
    inputTypes.addAll(indexTypes);
    
    // FunctionTypes
    FunctionTypes fTypes = FunctionTypes.newInstance(inputNames, inputTypes, null, elementType);
    
    // Prepare dependencies set
    Set<FunctionInstance> dependentInstances = FactoryUtils.newHashSet();
    
    // Instance for sub2ind. Initialize it here if necessary.
    final FunctionInstance sub2indInst = getSub2IndInstance(elementType, numArgs);
    if (sub2indInst != null) {
        dependentInstances.add(sub2indInst);
    }
    
    // Get template
    String body = IoUtils.getResource(TensorResource.GET_VALUE_BODY);
    
    // Sub2Idx call code
    List<CToken> indexArgs = FactoryUtils.newArrayList();
    // Add tensor
    indexArgs.add(CTokenFactory.newVariable(tensorName, tensorType));
    // Add indexes
    for (int i = 0; i < numArgs; i++) {
        indexArgs.add(CTokenFactory.newVariable(indexNames.get(i), indexTypes.get(i)));
    }
    // Get code
    String sub2indCode = null;
    if (sub2indInst != null) {
        sub2indCode = sub2indInst.getCallCode(indexArgs);
    } else {
        sub2indCode = indexNames.get(0);
    }
    
    // Replace tags
    body = body.replace("<SUB2IDX_CALL>", sub2indCode);
    
    body = body.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
    body = body.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
    body = body.replace("<FUNCTION_NAME>", functionName);
    
    // Create instance
    LiteralInstance getInstance = new LiteralInstance(fTypes, functionName, getFilename(), body);
    
    // Add dependencies
    getInstance.setCustomImplementationInstances(dependentInstances);
    
    getInstance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);
    
    return getInstance;
    
    }
    */

    /**
     * 
     * 
     * Assumes zero-based indexing.
     * 
     * @param elementType
     * @param numArgs
     * @return
     */
    private FunctionInstance getSub2IndInstance(VariableType elementType, int numArgs) {
        if (numArgs == 1) {
            return null;
        }

        return newSub2IndInline(elementType, numArgs);

        // return newSub2Ind(elementType, numArgs);
    }

    /**
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * - A variable number of integers representing the indexes;<br>
     * 
     * @param elementType
     * @param numArgs
     * @return
     */
    private FunctionInstance newSub2IndInline(VariableType elementType, int numArgs) {

        // Name of the function
        String functionName = "tensor_sub2ind_inline_" + elementType.getSmallId() + "_" + numArgs;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();

        String arrayName = "array";
        inputNames.add(arrayName);

        String indexesPrefix = "index_";
        inputNames.addAll(FunctionInstanceUtils.createNameList(indexesPrefix, numArgs));

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // matrix
        VariableType matrixType = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(matrixType);

        // indexes
        for (int i = 0; i < numArgs; i++) {
            inputTypes.add(getNumerics().newInt());
        }

        // FunctionTypes
        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, null, elementType);

        final MemoryLayout memoryLayout = getData().getMemoryLayout();

        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {

                // First argument is the matrix
                CNode matrix = arguments.get(0);
                List<CNode> indexes = arguments.subList(1, arguments.size());

                CNode sub2ind = TensorFunctionsUtils.newSub2Ind(matrix, indexes, memoryLayout);

                return sub2ind.getCode();

            }
        };

        return new InlinedInstance(functionTypes, functionName, inlineCode);
    }

    /**
     * Creates a new instance of the function 'length_alloc'. Returns the maximum dimension of the matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * 
     * @return
     */
    public FunctionInstance newLength(VariableType elementType) {

        // Name of the function
        String functionName = "length_alloc_" + elementType.getSmallId();

        // Input names
        List<String> inputNames = Arrays.asList("t");

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        List<VariableType> inputTypes = Arrays.asList(tensorType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, "maxDim", getNumerics().newInt());

        String cBody = SpecsIo.getResource(DynamicMatrixResource.LENGTH_BODY.getResource());

        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);

        LiteralInstance length = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        return length;
    }

    /**
     * Creates a new instance of the function 'is_same_shape', which returns 1 (true) if the given shape is the same as
     * the input matrix, or false otherwise.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * - A pointer to integer, representing an array with the shape;<br>
     * - An integer with the number of dimensions of the shape;<br>
     * 
     * @return
     */
    FunctionInstance newIsSameShape(VariableType elementType) {
        return new IsSameShape(getData(), (ScalarType) elementType).create();
    }

    private static final ThreadSafeLazy<String> setMatrixValuesBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(DynamicMatrixResource.SET_MATRIX_VALUES_BODY.getResource()));

    /**
     * Creates a new instance of the function 'set_matrix_values', which sets all the posititons of the matrix to the
     * given value (returns nothing).
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix;<br>
     * - A value of the matrix type, representing the value that will be set in the matrix positions;<br>
     * 
     * @return
     */
    static FunctionInstance newSetMatrixValues(VariableType elementType) {

        // Name of the function
        String functionName = "set_matrix_values_alloc_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        String valueName = "value";
        List<String> inputNames = Arrays.asList(tensorName, valueName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        // MatrixAllocData matrixData = VariableTypeContent.getMatrixAlloc(tensorType);
        // List<VariableType> inputTypes = Arrays.asList(tensorType, matrixData.getType());
        // List<VariableType> inputTypes = Arrays.asList(tensorType, MatrixUtils.getElementType(tensorType));
        List<VariableType> inputTypes = Arrays.asList(tensorType, elementType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        String cBody = TensorFunctions.setMatrixValuesBodyResource.getValue();

        cBody = cBody.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
        cBody = cBody.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);

        LiteralInstance isSameShape = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        return isSameShape;
    }

    /**
     * Creates a new instance of the function 'copy', which copies the contents of a matrix to another.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - An allocated matrix with the same shape, which will be the sink;
     * 
     * @return
     */
    FunctionInstance newCopy(VariableType elementType) {

        // Name of the function
        String functionName = "copy_alloc_" + elementType.getSmallId();

        // Input names
        String inputName = "input";
        String outputName = "output";
        List<String> inputNames = Arrays.asList(inputName, outputName);

        // Input types
        DynamicMatrixType tensorType = DynamicMatrixType.newInstance(elementType);
        VariableType outputType = ReferenceUtils.getType(tensorType, true);

        List<VariableType> inputTypes = Arrays.asList(tensorType, outputType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        String cBody = SpecsIo.getResource(DynamicMatrixResource.COPY_BODY.getResource());

        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
        cBody = cBody.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);

        FunctionInstance newHelperFunction = new TensorCreationFunctions(getData()).newArrayHelper(elementType);
        cBody = cBody.replace("<CALL_NEW_HELPER>", newHelperFunction.getCName());

        // FunctionInstance getFunction = newGetSubscriptsV2(elementType, 1);
        // cBody = cBody.replace("<CALL_GET>", getFunction.getCName());

        // Build token for call get(input, i)
        Variable inputMatrix = new Variable(inputName, tensorType);
        Variable index = new Variable("i", getNumerics().newInt());

        List<CNode> getArgs = CNodeFactory.newVariableList(inputMatrix, index);
        // FunctionInstance getFunction = newGetInline(elementType, 1);
        // CToken getCall = getFunction.newFunctionCall(getArgs);
        CNode getCall = getFunctionCall(tensorType.matrix().functions().get(), getArgs);

        // Build token for call set(output, i, get())
        Variable outputMatrix = new Variable(outputName, outputType);
        List<CNode> setArgs = CNodeFactory.newVariableList(outputMatrix, index);
        setArgs.add(getCall);

        // FunctionInstance setFunction = newSet(tensorType, 1);
        FunctionInstance setFunction = newSetInline(elementType, 1);
        CNode setCall = setFunction.newFunctionCall(setArgs);
        // cBody = cBody.replace("<CALL_SET>", setFunction.getCName());
        cBody = cBody.replace("<FULLCALL_SET>", setCall.getCode());

        LiteralInstance copyInstance = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        // copyInstance.setCustomImplementationInstances(newHelperFunction, getFunction, setFunction);
        copyInstance.getCustomImplementationInstances().add(getCall, setCall);
        copyInstance.getCustomImplementationInstances().add(newHelperFunction);

        return copyInstance;
    }

    /**
     * Creates a new instance of the function 'dimSize', which returns the size of the indicated dimension. Uses
     * zero-based indexing.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - The index of the dimension;
     * 
     * @return
     */
    FunctionInstance newDimSize(VariableType elementType) {

        // VariableType elementType = MatrixUtils.getElementType(tensorType);

        // Name of the function
        String functionName = "dim_size_alloc_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        String indexName = "index";
        List<String> inputNames = Arrays.asList(tensorName, indexName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        List<VariableType> inputTypes = Arrays.asList(tensorType, getNumerics().newInt());

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, "size", getNumerics().newInt());

        String cBody = SpecsIo.getResource(DynamicMatrixResource.DIM_SIZE_BODY.getResource());

        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);

        LiteralInstance dimSizeInstance = new LiteralInstance(fTypes, functionName, getFilename(), cBody);

        return dimSizeInstance;
    }

    /**
     * Creates an inlined version of the function 'dimSize', which returns the size of the indicated dimension. Uses
     * zero-based indexing.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - The index of the dimension;
     * 
     * 
     * @return
     */
    FunctionInstance newDimSizeInline(VariableType elementType) {

        // Name of the function. Element type is needed for function inputs, to receive a matrix of the given type
        String functionName = "dim_size_inline_" + elementType.getSmallId();

        // Input names
        // List<String> inputNames = FactoryUtils.newArrayList();
        // String tensorName = "t";
        // inputNames.add(tensorName);

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(tensorType);

        // Add index
        inputTypes.add(getNumerics().newInt());

        // Output type is always an integer
        FunctionType fTypes = FunctionType.newInstanceNotImplementable(inputTypes, getNumerics().newInt());

        // Prepare dependencies set
        Set<FunctionInstance> dependentInstances = SpecsFactory.newHashSet();

        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                // There should be two arguments
                Preconditions.checkArgument(arguments.size() == 2, "Should have exactly two arguments:\n" + arguments);

                // First argument is the tensor variable
                CNode tensorArg = arguments.get(0);

                String index = arguments.get(1).getCode();

                // Build code for array access
                return MatrixCode.getStructField(tensorArg, DynamicMatrixStruct.TENSOR_SHAPE, index);

            }

        };

        InlinedInstance getInline = new InlinedInstance(fTypes, functionName, inlineCode);

        // Add dependencies
        getInline.setCallInstances(dependentInstances);

        return getInline;

    }

    /**
     * Creates a new instance of the function 'transpose', which returns a matrix which is the transpose of the input.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, which will be the source;<br>
     * - A pointer to an allocated matrix, which will be the output;<br>
     * 
     * @return
     */
    public FunctionInstance newTranspose(MatrixType matrixType) {
        // public FunctionInstance newTranspose(VariableType elementType) {

        VariableType elementType = MatrixUtils.getElementType(matrixType);

        // Name of the function
        String functionName = "transpose_alloc_" + elementType.getSmallId();

        // Input names
        String inputName = "input_matrix";
        List<String> inputNames = Arrays.asList(inputName);

        // Input types
        // List<VariableType> inputTypes = Arrays.asList(tensorType);
        List<VariableType> inputTypes = Arrays.asList(matrixType);

        // This Transpose only works for 2-D matrices, so shape of output will always have two dimensions
        TypeShape shape = TypeShape.newDimsShape(2);
        // If fully defined, invert shape
        if (matrixType.matrix().getShape().getRawNumDims() == 2) {
            List<Integer> transDims = new ArrayList<>(matrixType.matrix().getShape().getDims());
            Collections.reverse(transDims);
            shape = TypeShape.newInstance(transDims);
        }
        DynamicMatrixType outputType = DynamicMatrixType.newInstance(elementType, shape);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes,
                "transposed_matrix", outputType);

        // We can specialize the output type (in fTypes), but the actual code should not be specialized for the shape.
        // See transpose_specialization_pitfall.m (from AutomatedTests)
        matrixType = matrixType.matrix().setShape(TypeShape.newDimsShape(2));

        String cBody = SpecsIo.getResource(DynamicMatrixResource.TRANSPOSE_BODY.getResource());

        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);

        // Get name of the tensor structure
        // MatrixAllocData mData = VariableTypeContent.getMatrixAlloc(tensorType);
        // String tensorStructureName = mData.getInstance().getCName();
        String tensorStructureName = DynamicMatrixUtils.getStructInstance(matrixType).getCName();
        cBody = cBody.replace("<TENSOR_STRUCT>", tensorStructureName);

        FunctionInstance callNewArray = new TensorCreationFunctions(getData()).newArray(elementType, 2);
        cBody = cBody.replace("<CALL_NEW_ARRAY>", callNewArray.getCName());

        FunctionInstance callDimSize = newDimSize(elementType);
        cBody = cBody.replace("<CALL_DIM_SIZE>", callDimSize.getCName());

        FunctionInstance callCopy = newCopy(elementType);
        cBody = cBody.replace("<CALL_COPY>", callCopy.getCName());

        // Build call to get(input_matrix, i, j)

        Variable inputMatrixVar = new Variable(inputName, matrixType);
        Variable iIndex = new Variable("i", getNumerics().newInt());
        Variable jIndex = new Variable("j", getNumerics().newInt());

        List<CNode> getArgs = CNodeFactory.newVariableList(inputMatrixVar, iIndex, jIndex);
        // FunctionInstance callGet = newGetInline(elementType, 2);
        // CToken callGetToken = callGet.newFunctionCall(getArgs);
        CNode callGetToken = getFunctionCall(matrixType.matrix().functions().get(), getArgs);

        // Build call to set(temp, j, i, get())
        FunctionInstance callSet = newSetInline(elementType, 2);

        Variable tempMatrix = new Variable("temp", matrixType);
        List<CNode> setArgs = CNodeFactory.newVariableList(tempMatrix, jIndex, iIndex);
        setArgs.add(callGetToken);

        CNode callSetToken = callSet.newFunctionCall(setArgs);

        // cBody = cBody.replace("<CALL_SET>", callSet.getCName());
        cBody = cBody.replace("<FULLCALL_SET>", callSetToken.getCode());

        // LiteralInstance dimSizeInstance = new LiteralInstance(fTypes, functionName, getFilename(),
        String filename = CirFilename.GENERAL.getFilename();
        LiteralInstance dimSizeInstance = new LiteralInstance(fTypes, functionName, filename, cBody);

        // Set<FunctionInstance> implInstances = CTokenUtils.getCallInstances(callNewArray);
        dimSizeInstance.getCustomImplementationInstances().add(callNewArray, callDimSize, callSet, callCopy);
        dimSizeInstance.getCustomImplementationInstances().add(callGetToken);

        dimSizeInstance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);

        return dimSizeInstance;
    }

    public FunctionInstance newGetDataPointerInlined() {
        List<VariableType> inputTypes = getData().getInputTypes();
        checkState(inputTypes.size() == 1);
        final DynamicMatrixType matrixType = (DynamicMatrixType) inputTypes.get(0);
        VariableType elementBaseType = matrixType.getElementType();
        VariableType outputType = ReferenceUtils.getType(elementBaseType, true);

        FunctionType functionType = FunctionType.newInstanceNotImplementable(inputTypes, outputType);
        String functionName = "get_data_ptr$" + matrixType.getSmallId();

        InlineCode code = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                StringBuilder builder = new StringBuilder();

                // Get first (and only) argument
                CNode arg = arguments.get(0);

                builder.append(arg.getCodeForContent(PrecedenceLevel.MemberAccess));
                builder.append("->data");

                return builder.toString();
                // boolean isPointer = arguments.get(0).getVariableType().pointer().isPointer();
                // return (isPointer ? "(*" : "") + arguments.get(0).getCode()
                // + (isPointer ? ")" : "") + "->data";
            }
        };

        return new InlinedInstance(functionType, functionName, code);
    }
}
