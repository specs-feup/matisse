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

package org.specs.MatlabToC.Functions.MatissePrimitives;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

public class NewArrayStatic extends AInstanceBuilder {

    public NewArrayStatic(ProviderData data) {
        super(data);
    }

    private static final MatisseChecker CHECKER_STATIC_CRATE = new MatisseChecker()
            // Check if dynamic allocation is not allowed
            .not().dynamicAllocationEnabled()
            // Check if 1 or 2 inputs
            .numOfInputsRange(1, 2)
            // Check if input 1 is a matrix
            .isMatrix(0)
            // Check if input 2 is a String
            .isString(1);

    @Override
    public FunctionInstance create() {
        // Get matrix type and dims
        MatrixType matrixType = getMatrixType(getData());
        List<Integer> matrixDims = getMatrixDims(getData());

        // At this point, if no dynamic matrices are allowed and matrix dims where not found, throw
        // exception
        if (matrixDims.isEmpty()) {
            throw new RuntimeException(
                    "Could not determine statically the elements in the shape matrix given as input.");
        }

        List<VariableType> inputTypes = new ArrayList<>();

        matrixDims.forEach(dim -> inputTypes.add(getData().getNumerics().newInt(dim)));

        ProviderData data = getData().createWithContext(inputTypes);

        // Set output matrix dims
        matrixType = matrixType.matrix().setShape(TypeShape.newInstance(matrixDims));

        // Set as ouput type
        data.setOutputType(matrixType);

        return matrixType.matrix().functions().create().newCInstance(data);
    }

    private static MatrixType getMatrixType(ProviderData builderData) {
        // If output type is defined, return it
        if (builderData.getOutputType() != null) {
            return (MatrixType) builderData.getOutputType();
        }

        VariableType elementType = MatlabToCTypesUtils.getElementType(builderData);

        // If dynamic allocation not allowed, use a static matrix with a dummy shape for now
        if (!builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return StaticMatrixType.newInstance(elementType, 1, 1);
        }

        // As default, use dynamic matrix
        return DynamicMatrixType.newInstance(elementType);
    }

    private static List<Integer> getMatrixDims(ProviderData builderData) {
        // If output type is not null, use the dimensions of the output matrix shape
        if (builderData.getOutputType() != null) {

            MatrixType outputType = ((MatrixType) builderData.getOutputType());

            // Check if output type is fully defined
            if (outputType.getTypeShape().isFullyDefined()) {
                return outputType.getTypeShape().getDims();
            }

        }

        // If shape matrix has defined values, use those dims
        MatrixType inputMatrix = builderData.getInputType(MatrixType.class, 0);
        if (inputMatrix.getTypeShape().hasValues()) {
            List<Integer> values = new ArrayList<>();
            inputMatrix.getTypeShape().getValues().forEach(value -> values.add(value.intValue()));
            return values;
        }

        // Could not get statically the dims of the matrix, return empty list
        throw new RuntimeException(
                "Could not determine statically the elements in the shape matrix given as input.");
    }

    private static InputsFilter getInputsFilter() {
        // Since we are using matrix.functions.create, inputs have to be totally recreated.
        return (data, args) -> {

            List<Integer> matrixDims = getMatrixDims(data);
            List<CNode> newArgs = new ArrayList<>();

            matrixDims.forEach(dim -> newArgs.add(CNodeFactory.newCNumber(dim, data.getNumerics().newInt(dim))));

            return newArgs;

        };
    }

    public static InstanceProvider getProvider() {

        InstanceProvider provider = data -> new NewArrayStatic(data).create();

        return new MatlabInstanceProviderHelper(NewArrayStatic.CHECKER_STATIC_CRATE, provider, getInputsFilter());

    }
}
