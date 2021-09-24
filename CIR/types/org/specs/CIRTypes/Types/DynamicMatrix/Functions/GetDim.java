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

import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getFilename;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CodeGenerator.MatrixCode;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

public class GetDim extends AInstanceBuilder {

    public GetDim(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        CirInputsChecker checker = new CirInputsChecker()
                .numOfInputs(2)
                .ofType(DynamicMatrixType.class, 0)
                .isScalar(1);

        return new GenericInstanceProvider(checker, data -> new GetDim(data).create());
    }

    @Override
    public FunctionInstance create() {

        // First argument is the matrix type
        MatrixType matrixType = getTypeAtIndex(MatrixType.class, 0);
        ScalarType elementType = matrixType.matrix().getElementType();

        // Check which 'dim_size' version should be used (inlined or function)
        boolean inline = getSettings().get(CirKeys.INLINE).inline(MatrixFunctionName.DIM_SIZE);

        if (inline) {
            return newDimSizeInline(elementType);
        }

        return newDimSize(elementType);

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
    private FunctionInstance newDimSizeInline(ScalarType elementType) {

        // Name of the function. Element type is needed for function inputs, to receive a matrix of the given type
        String functionName = "dim_size_inline_" + elementType.getSmallId();

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
        getInline.setCallPrecedenceLevel(PrecedenceLevel.MemberAccess);

        // Add dependencies
        getInline.setCallInstances(dependentInstances);

        return getInline;

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

        Replacer cBody = new Replacer(SpecsIo.getResource(DynamicMatrixResource.DIM_SIZE_BODY.getResource()));

        DynamicMatrixStruct.replaceFields(getData(), cBody);
        // cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        // cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);

        LiteralInstance dimSizeInstance = new LiteralInstance(fTypes, functionName, getFilename(), cBody.toString());

        return dimSizeInstance;
    }
}
