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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions.LowLevel;

import static org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils.getFilename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIRFunctions.MatrixAlloc.TensorCreationResource;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Pointer.PointerType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * * Creates a new instance of the function 'new_array', which creates a new tensor structure. Returns an allocated
 * matrix with the size specified by the inputs and with data elements of the given 'tensorType'.
 * 
 * <p>
 * Inputs (only for function call):<br>
 * - A pointer to integer, representing the shape of the matrix;<br>
 * - An integer, with the number of dimensions of the matrix; <br>
 * 
 * @author JoaoBispo
 *
 */
public class CreateHelper extends AInstanceBuilder {

    private final ScalarType elementType;
    private static final ThreadSafeLazy<String> newArrayHelperBodyResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(TensorCreationResource.NEW_ARRAY_HELPER_BODY));

    public CreateHelper(ProviderData data, ScalarType elementType) {
        super(data);

        this.elementType = elementType;
    }

    public static InstanceProvider getProvider(ScalarType elementType) {
        // InputsChecker checker = new InputsChecker()
        // return new InstanceProviderHelper(checker, data -> new CreateHelper(data, elementType).create());

        // Inputs types are not used, there is no need for checker
        return data -> new CreateHelper(data, elementType).create();
    }

    @Override
    public FunctionInstance create() {
        String functionName = "new_array_helper_" + elementType.getSmallId();

        // Input names
        String shapeName = "shape";
        String dimsName = "dims";
        List<String> inputNames = Arrays.asList(shapeName, dimsName);

        // Input types
        // VariableType intType = VariableTypeFactoryOld.newNumeric(NumericType.Cint);
        VariableType intType = getNumerics().newInt();
        // TODO: Replace with 'PointerType'
        // VariableType pointerToIntType = PointerUtils.getType(intType, true);
        VariableType pointerToIntType = new PointerType(intType);

        List<VariableType> inputTypes = Arrays.asList(pointerToIntType, intType);

        // FunctionTypes
        String tensorName = "t";
        DynamicMatrixType tensorType = DynamicMatrixType.newInstance(elementType);
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, tensorName,
                tensorType);

        // Get name of the tensor structure
        String tensorStructureName = DynamicMatrixUtils.getStructInstance(tensorType).getCName();

        // Instance for is_same_shape
        FunctionInstance isSameShapeInstance = new IsSameShape(getData(), elementType).create();

        // Instance for free
        // FunctionInstance freeInstance = TensorCreationFunctions.newFree(elementType);
        ProviderData freeData = getData().create(tensorType);
        FunctionInstance freeInstance = tensorType.matrix().functions().free().newCInstance(freeData);

        // Build body
        // String cBody = IoUtils.getResource(TensorCreationResource.NEW_ARRAY_HELPER_BODY);
        Replacer cBody = new Replacer(newArrayHelperBodyResource.getValue());

        cBody.replace("<TENSOR_STRUCT>", tensorStructureName);

        DynamicMatrixStruct.replaceFields(getData(), cBody);
        /*
        cBody.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
        cBody = cBody.replace("<TENSOR_LENGTH>", DynamicMatrixStruct.TENSOR_LENGTH);
        cBody = cBody.replace("<TENSOR_SHAPE>", DynamicMatrixStruct.TENSOR_SHAPE);
        cBody = cBody.replace("<TENSOR_DIMS>", DynamicMatrixStruct.TENSOR_DIMS);
        */

        cBody.replace("<DATA_TYPE>", CodeUtils.getType(elementType));
        cBody.replace("<CALL_IS_SAME_SHAPE>", isSameShapeInstance.getCName());
        cBody.replace("<CALL_FREE>", freeInstance.getCName());

        List<CNode> dependentInstances = new ArrayList<>();

        if (elementType.code().requiresExplicitInitialization()) {
            CInstructionList instructions = elementType.code().getSafeDefaultDeclaration(
                    CNodeFactory.newLiteral("(*t)->data[i]", elementType, PrecedenceLevel.ArrayAccess), getData());

            StringBuilder code = new StringBuilder();
            code.append("for (int i = 0; i < (*t)->length; ++i) {\n");
            for (CNode instruction : instructions.get()) {
                for (FunctionCallNode function : instruction.getDescendants(FunctionCallNode.class)) {
                    dependentInstances.add(function);
                }

                code.append(instruction.getCode() + ";\n");
            }
            code.append("}");
            cBody.replace("<INITIALIZE_DATA>", code);
        } else {
            // No action needed
            cBody.replace("<INITIALIZE_DATA>", "");
        }

        LiteralInstance newEmptyTensor = new LiteralInstance(fTypes, functionName, getFilename(), cBody.toString());

        // Set includes
        List<String> includes = new ArrayList<>();
        includes.add(SystemInclude.Stdlib.getKey());
        includes.add(SystemInclude.Stdio.getKey());
        String customAllocationHeader = getData().getSettings().get(CirKeys.CUSTOM_ALLOCATION_HEADER);
        if (!customAllocationHeader.isEmpty()) {
            includes.add(customAllocationHeader);
        }
        newEmptyTensor.setCustomImplementationIncludes(includes);

        // Set instance
        newEmptyTensor.getCustomImplementationInstances().add(isSameShapeInstance, freeInstance);
        newEmptyTensor.getCustomImplementationInstances().add(dependentInstances);

        return newEmptyTensor;
    }

}
