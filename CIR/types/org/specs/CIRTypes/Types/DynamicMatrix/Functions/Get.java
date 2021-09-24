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

import static org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixFunctions.*;

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
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;

public class Get extends AInstanceBuilder {

    public Get(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {

        List<VariableType> inputTypes = getData().getInputTypes();

        // First argument is the matrix to access
        DynamicMatrixType matrixType = getTypeAtIndex(DynamicMatrixType.class, 0);
        VariableType elementType = matrixType.getElementType();

        // Number of dimensions is size of inputs minus 1
        int numDims = inputTypes.size() - 1;

        // Check which 'set' version should be used (inlined or function)

        boolean inline = getSettings().get(CirKeys.INLINE).inline(MatrixFunctionName.GET);

        if (inline) {
            return newGetInline(elementType, numDims);
        }

        return newGetLiteral(elementType, numDims);

    }

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
    private FunctionInstance newGetLiteral(VariableType elementType, int numArgs) {

        // Name of the function
        String functionName = "get_tensor_" + elementType.getSmallId() + "_" + numArgs;

        // Input names
        String tensorName = "t";
        String indexPrefix = "index_";

        // Indexes
        List<String> indexNames = FunctionInstanceUtils.createNameList(indexPrefix, numArgs);
        List<VariableType> indexTypes = SpecsFactory.newArrayList();
        for (int i = 0; i < numArgs; i++) {
            indexTypes.add(getNumerics().newInt());
        }

        List<String> inputNames = SpecsFactory.newArrayList();
        inputNames.add(tensorName);
        inputNames.addAll(indexNames);

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();

        // Matrix
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(tensorType);
        inputTypes.addAll(indexTypes);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, elementType);

        // Prepare dependencies set
        Set<FunctionInstance> dependentInstances = SpecsFactory.newHashSet();

        // Instance for sub2ind. Initialize it here if necessary.
        FunctionInstance sub2indInst = null;
        if (numArgs > 1) {
            // Inputs for 'sub2ind' are the same as the get function
            sub2indInst = new Sub2Ind(getData()).create();
            dependentInstances.add(sub2indInst);
        }

        // Get template
        String body = SpecsIo.getResource(DynamicMatrixResource.GET_VALUE_BODY);

        // Sub2Idx call code
        List<CNode> indexArgs = SpecsFactory.newArrayList();
        // Add tensor
        indexArgs.add(CNodeFactory.newVariable(tensorName, tensorType));
        // Add indexes
        for (int i = 0; i < numArgs; i++) {
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
     * Creates an inlined version of the function 'get_tensor_inline', which returns the contents of an allocated matrix
     * in the specified index. The index can be represented with a variable number of integers.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - An allocated matrix, whose values will be read;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     * 
     * <p>
     * TODO: indexes should be CNative?
     * 
     * @param tensorType
     * @param useLinearArrays
     * @return
     */
    private FunctionInstance newGetInline(VariableType elementType, int numArgs) {

        // Name of the function
        String functionName = "get_tensor_inline_" + elementType.getSmallId() + "_" + numArgs;

        // Input names
        List<String> inputNames = SpecsFactory.newArrayList();
        String tensorName = "t";
        inputNames.add(tensorName);

        final String indexPrefix = "index_";
        inputNames.addAll(FunctionInstanceUtils.createNameList(indexPrefix, numArgs));

        // Input types
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        inputTypes.add(tensorType);

        VariableType intType = getNumerics().newInt();
        for (int i = 0; i < numArgs; i++) {
            inputTypes.add(intType);
        }

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, elementType);

        // Prepare dependencies set
        Set<FunctionInstance> dependentInstances = SpecsFactory.newHashSet();

        // Instance for sub2ind. Initialize it here if necessary.
        FunctionInstance sub2indInst = null;
        if (numArgs > 1) {
            // Inputs for 'sub2ind' are the same as the get function
            sub2indInst = new Sub2Ind(getData()).create();
            dependentInstances.add(sub2indInst);
        }

        InlineCode inlineCode = createInlineCode(sub2indInst);

        InlinedInstance getInline = new InlinedInstance(fTypes, functionName, inlineCode);
        getInline.setCallPrecedenceLevel(PrecedenceLevel.MemberAccess);

        // Add dependencies
        getInline.setCallInstances(dependentInstances);

        return getInline;

    }

    private static InlineCode createInlineCode(FunctionInstance sub2ind) {
        return (List<CNode> arguments) -> {
            // VariableType intType = getNumerics().newInt();

            // First argument is the tensor variable
            CNode tensorArg = arguments.get(0);

            // For all index arguments, add cast to int if they are not integer
            /*
            for (int i = 1; i < arguments.size(); i++) {
            CNode arg = arguments.get(i);
            
            // If integer, there is not problem
            if (ScalarUtils.isInteger(arg.getVariableType())) {
                continue;
            }
            
            // If not integer, cast to int
            arg = getFunctionCall(UtilityInstances.getCastToScalarProvider(intType), arg);
            
            // Replace node
            arguments.set(i, arg);
            }
            */

            String index = null;
            // If only one index, return code of the subscript itself
            if (arguments.size() == 2) {
                index = arguments.get(1).getCode();
            }

            // Call function that transforms subscripts into indexes
            else {

                List<CNode> newArgs = SpecsFactory.newArrayList();

                // Add tensor
                newArgs.add(arguments.get(0));

                // Get indexes. Remaining arguments except last are the index subscripts
                List<CNode> indexes = arguments.subList(1, arguments.size());

                // Add simplified indexes
                newArgs.addAll(CodeGeneratorUtils.getSimplifiedIndexes(indexes));

                // Simplify arguments
                // List<CToken> newArgs = CodeGeneratorUtils.getSimplifiedLiterals(arguments);

                // Build call
                index = sub2ind.getCallCode(newArgs);
            }

            // Build code for array access
            return MatrixCode.getAllocAccess(tensorArg, index);
            /*
            String code = MatrixCode.getAllocAccess(tensorArgName, tensorArgType, index);
            if (code.startsWith("X->data[((int) ((double)")) {
            System.out.println("CODE:" + code);
            System.out.println("CNODES:\n" + arguments);
            throw new RuntimeException("HEHEH");
            }
            
            return code;
            */
        };

    }
}
