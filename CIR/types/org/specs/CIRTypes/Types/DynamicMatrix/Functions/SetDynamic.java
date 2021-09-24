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

import static org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixFunctions.getFilename;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.CodeGenerator.MatrixCode;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Utilities.AssignmentUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Void.VoidType;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;

/**
 * Creates a new instance of the function 'set', which sets a single value in an allocated matrix. Uses zero-based
 * indexing.
 * 
 * <p>
 * FunctionCall receives:<br>
 * - A matrix, whose values will be set;<br>
 * - A variable number of integers with the indexes of the matrix that will be set. Index assumes zero-based indexing;
 * <br>
 * - A value of type 'tensorType', which will be used to set the allocated matrix at the specified index;<br>
 * 
 * @author JoaoBispo
 *
 */
public class SetDynamic extends AInstanceBuilder {

    public SetDynamic(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        List<VariableType> inputTypes = getData().getInputTypes();

        // First argument is an allocated matrix
        MatrixType matrixType = getTypeAtIndex(MatrixType.class, 0);
        VariableType elementType = MatrixUtils.getElementType(matrixType);

        // Number of dimensions is size of inputs minus 2
        int numDims = inputTypes.size() - 2;

        // Check which 'set' version should be used (inlined or function)
        boolean inline = getSettings().get(CirKeys.INLINE).inline(MatrixFunctionName.SET);

        if (inline) {
            return newSetInline(elementType, numDims);
        }
        return newSet(elementType, numDims);

    }

    /**
     * Creates a new instance of the function 'set', which sets a single value in an allocated matrix. Uses zero-based
     * indexing.
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

        // Value
        inputTypes.add(elementType);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        // Prepare dependencies set
        Set<FunctionInstance> dependentInstances = SpecsFactory.newHashSet();

        // Instance for sub2ind. Initialize it here if necessary.
        final FunctionInstance sub2indInst = new Sub2Ind(getData()).create();
        if (sub2indInst != null) {
            dependentInstances.add(sub2indInst);
        }

        // Get template
        String body = SpecsIo.getResource(DynamicMatrixResource.SET_VALUE_BODY);

        // Sub2Idx call code
        List<CNode> indexArgs = SpecsFactory.newArrayList();
        // Add tensor
        VariableNode tensorNode = CNodeFactory.newVariable(tensorName, tensorType);
        indexArgs.add(tensorNode);
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
        body = body.replace("<ASSIGNMENT>",
                MatrixCode.getAllocStore(tensorNode, "index", CNodeFactory.newVariable("value", elementType),
                        getData()));

        // Create instance
        LiteralInstance setInstance = new LiteralInstance(fTypes, functionName, getFilename(), body);

        // Add dependencies
        setInstance.getCustomImplementationInstances().add(dependentInstances);

        setInstance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib);

        return setInstance;

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
        Preconditions.checkArgument(numIndexes > 0, "Matrix set must have at least one index");

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
        final FunctionInstance sub2indInst = new Sub2Ind(getData()).create();
        dependentInstances.add(sub2indInst);
        dependentInstances.addAll(AssignmentUtils.getAssignmentInstances(elementType, elementType, getData()));
        /*
        	if (sub2indInst != null) {
        	    dependentInstances.add(sub2indInst);
        	}
        */
        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                // Second arg is index

                // First argument is the tensor variable
                CNode tensorArg = arguments.get(0);

                String index = null;
                // If only one index, return code of the subscript itself
                if (arguments.size() == 3) {
                    List<CNode> arg = CodeGeneratorUtils.getSimplifiedIndexes(Arrays.asList(arguments.get(1)));
                    // index = arguments.get(1).getCode();
                    index = arg.get(0).getCode();
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
                    index = sub2indInst.getCallCode(newArgs);
                }

                // Third argument is value
                CNode valueToken = arguments.get(arguments.size() - 1);

                // Build code for array access
                return MatrixCode.getAllocStore(tensorArg, index, valueToken, getData());
            }
        };

        InlinedInstance getInline = new InlinedInstance(fTypes, functionName, inlineCode);

        // Add dependencies
        getInline.setCallInstances(dependentInstances);

        return getInline;

    }
}
