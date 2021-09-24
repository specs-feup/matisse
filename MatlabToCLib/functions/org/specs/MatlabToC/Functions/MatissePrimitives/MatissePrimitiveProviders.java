/**
 * Copyright 2013 SPeCS Research Group.
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

import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRFunctions.MatrixAlloc.TensorCreationFunctions;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.ChangeShape;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * @author Joao Bispo
 * 
 *         TODO: newArrayBuilder needs to be divided, into dynamic builder and static builder TODO: Each function should
 *         have it's own class?
 */
public class MatissePrimitiveProviders {

    private static final MatisseChecker CHECKER_DYNAMIC_BUILDER = new MatisseChecker()
            // Check if 1 or 2 inputs
            .numOfInputsRange(1, 2)
            // Check if input 1 is a matrix
            .isMatrix(0)
            // Check if input 2 is a String
            .isString(1);

    /**
     * <code>new_array(shape, [class])</code><br/>
     * <p>
     * Dynamically allocates a new matrix, with the specified shape and type.
     * </p>
     * <strong>Inputs:</strong>
     * <ul>
     * <li><code>shape: MatrixType</code>: The shape of the matrix to allocate.
     * <li><code>class: StringType (<em>optional</em>)</code>: The type of the matrix to allocate.<br/>
     * If the class is specified, then the output types are ignored.<br/>
     * If no class is specified, but the ProviderData has an output type, then that is used as the type to
     * allocate.<br/>
     * If no class is specified and the output type is not provided, then the allocated matrix has type DEFAULT_REAL.
     * </ul>
     * <strong>Outputs:</strong>
     * <p>
     * This function has one single output-as-input, which is the allocated matrix.
     * </p>
     * 
     * @return The InstanceProvider for the new_array function.
     */
    public static InstanceProvider newArrayDynamic() {

        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                return MatissePrimitiveProviders.CHECKER_DYNAMIC_BUILDER.create(data).check();
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {

                VariableType elementType = MatlabToCTypesUtils.getElementType(builderData);

                return new TensorCreationFunctions(builderData).newArrayFromMatrix(elementType)
                        .newCInstance(builderData);

            }

            /* (non-Javadoc)
             * @see org.specs.MatlabToC.CirInterface.MatlabToCProvider#getInputsParser(org.specs.CIR.Functions.FunctionTypes)
             */
            @Override
            public InputsFilter getInputsFilter() {
                // Removes the input with the type information. If should be last input.
                return new InputsFilter() {

                    @Override
                    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {

                        // Remove last argument if type is string
                        CNode lastToken = SpecsCollections.last(originalArguments);
                        if (StringTypeUtils.isString(lastToken.getVariableType())) {
                            return originalArguments.subList(0, originalArguments.size() - 1);
                        }

                        return originalArguments;
                    }
                };
            }

        };

    }

    /**
     * @return
     */
    public static InstanceProvider newArrayFromMatrix() {

        MatisseChecker checker = new MatisseChecker()
                // Check if 1 input
                .numOfInputs(1)
                // Check if first input is a matrix
                .isMatrix(0);

        InstanceProvider provider = new InstanceProvider() {
            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                MatrixType matrixType = getMatrixType(data);

                // Use matrix createFromMatrix
                return matrixType.matrix().functions().createFromMatrix().newCInstance(data);
            }

            private MatrixType getMatrixType(ProviderData data) {
                // Get matrix input
                MatrixType matrixType = (MatrixType) data.getInputTypes().get(0);

                if (data.getOutputTypes().size() == 1) {
                    VariableType outputType = data.getOutputType();
                    if (outputType instanceof MatrixType) {
                        matrixType = (MatrixType) outputType;
                    }
                }
                return matrixType;
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return getMatrixType(data).matrix().functions().createFromMatrix().getType(data);
            }
        };

        return new MatlabInstanceProviderHelper(checker, provider);

    }

    public static InstanceProvider newCastToReal() {

        CirInputsChecker check = new CirInputsChecker()
                .numOfInputs(1);

        return new GenericInstanceProvider(check, data -> UtilityInstances.newCastToScalar(data.getInputTypes().get(0),
                data.getSettings().get(CirKeys.DEFAULT_REAL)));

    }

    public static InstanceProvider newChangeShape() {
        CirInputsChecker check = new CirInputsChecker()
                .numOfInputs(2)
                .areMatrices();

        return new GenericInstanceProvider(check, data -> new ChangeShape(data).create());
    }

    public static InstanceProvider newIDivide() {
        MatisseChecker checker = new MatisseChecker()
                // Check if 2 inputs
                .numOfInputs(2);

        InstanceProvider provider = data -> {
            // Set output type as integer
            data.setOutputType(data.getNumerics().newInt());
            // This is a temporary solution, it might work now because ScalarDivision has a rule which implements
            // integer division if the output is integer
            return MatlabOp.RightDivision.getMatlabFunction().getCheckedInstance(data);
            // Get matrix input
            // MatrixType matrixType = (MatrixType) data.getInputTypes().get(0);
            // Use matrix createFromMatrix
            // return matrixType.matrix().functions().createFromMatrix().newCInstance(data);
        };

        return new MatlabInstanceProviderHelper(checker, provider);
    }

    public static InstanceProvider newMaxOrZero() {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1)
                .isMatrix(0);

        // STUB: See if we can optimize this.
        InstanceProvider provider = MFileProvider.getProvider(CompatibilityPackageResource.MAX_OR_ZERO);

        return new MatlabInstanceProviderHelper(checker, provider);
    }
}
