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

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRFunctions.MatrixAlloc.TensorCreationResource;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * 
 * @author JoaoBispo
 *
 */
public class Free extends AInstanceBuilder {

    private static ThreadSafeLazy<String> freeBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TensorCreationResource.FREE_BODY));
    private static ThreadSafeLazy<String> freeCommentsResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TensorCreationResource.FREE_COMMENTS));

    public Free(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        CirInputsChecker checker = new CirInputsChecker()
                // One input
                .numOfInputs(1)
                // of type Dynamic Matrix
                .ofType(DynamicMatrixType.class, 0);

        return new GenericInstanceProvider(checker, data -> new Free(data).create());
    }

    @Override
    public FunctionInstance create() {

        MatrixType inputType = getTypeAtIndex(MatrixType.class, 0);
        ScalarType elementType = inputType.matrix().getElementType();

        return getFreeInstance(elementType);
    }

    public FunctionInstance getFreeInstance(VariableType elementType) {
        // Name of the function
        String functionName = "tensor_free_" + elementType.getSmallId();

        // Input names
        String tensorName = "t";
        List<String> inputNames = Arrays.asList(tensorName);

        // Input types
        VariableType tensorType = DynamicMatrixType.newInstance(elementType);
        VariableType pointerToMatrix = ReferenceUtils.getType(tensorType, true);
        List<VariableType> inputTypes = Arrays.asList(pointerToMatrix);

        // FunctionTypes
        FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, VoidType.newInstance());

        // Get body of function
        Replacer body = new Replacer(freeBodyResource.getValue());

        // Apply changes to default fields
        DynamicMatrixStruct.replaceFields(getData(), body);
        // replacer.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
        // replacer.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);

        // Create instance
        LiteralInstance setTensor = new LiteralInstance(fTypes, functionName, getFilename(), body.toString());
        String customAllocationHeader = getData().getSettings().get(CirKeys.CUSTOM_ALLOCATION_HEADER);
        if (!customAllocationHeader.isEmpty()) {
            setTensor.setCustomImplementationIncludes(customAllocationHeader);
        }

        // Set comments
        String comments = freeCommentsResource.getValue();
        setTensor.setComments(comments);

        return setTensor;
    }
}
