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

package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRFunctions.MatrixDec.DeclaredProvider;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

/**
 * @author Joao Bispo
 * 
 */
public class ArrayDecBuilders {

    /**
     * Builder for numeric types of function 'row', when using declared matrixes.
     * 
     * @return
     */
    public static InstanceProvider newRowNumeric() {
        return new MatlabInstanceProvider() {

            /**
             * Checks if all arguments are of type numeric.
             */
            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#checkRule(java.util.List)
             */
            @Override
            public boolean checkRule(ProviderData fSig) {

                // Check if declared matrices are allowed
                /*
                if (!fSig.getSetup().useStaticAllocation()) {
                    return false;
                }
                */

                // Get input types
                List<VariableType> argumentTypes = fSig.getInputTypes();

                // Check if it has at least one argument
                if (argumentTypes.isEmpty()) {
                    return false;
                }

                // if (!TypeVerification.areOfType(argumentTypes, CType.Numeric)) {
                if (!TypeVerification.areScalar(argumentTypes)) {
                    return false;
                }

                // Check if output type was already defined as being allocated
                if (MatrixUtils.usesDynamicAllocation(fSig.getOutputType())) {
                    return false;
                }

                return true;
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#create(org.specs.CIR.Function.FunctionPrototype, java.util.List)
             */
            @Override
            public FunctionInstance create(ProviderData iSig) {
                return RowDecNumericInstance.getProvider().newCInstance(iSig);
            }
        };
    }

    public static InstanceProvider newRowNumericCombine() {
        return new MatlabInstanceProvider() {

            /**
             * Checks if all arguments are of type numeric.
             */
            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#checkRule(java.util.List)
             */
            @Override
            public boolean checkRule(ProviderData fSig) {
                // Get input types
                List<VariableType> argumentTypes = fSig.getInputTypes();

                // Check if it has at least one argument
                if (argumentTypes.isEmpty()) {
                    return false;
                }

                for (VariableType argumentType : argumentTypes) {
                    if (ScalarUtils.isScalar(argumentType)) {
                        continue;
                    }

                    TypeShape shape = MatrixUtils.getShape(argumentType);
                    if (!shape.isKnownRow()) {
                        return false;
                    }
                }

                return true;
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#create(org.specs.CIR.Function.FunctionPrototype, java.util.List)
             */
            @Override
            public FunctionInstance create(ProviderData iSig) {
                return RowCombineInstance.getProvider().newCInstance(iSig);
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return RowCombineInstance.getProvider().getType(data);
            }
        };
    }

    /**
     * Builder for numeric types of function 'row', when using declared matrixes.
     * 
     * @return
     */
    public static InstanceProvider newColNumeric() {
        return new MatlabInstanceProvider() {
            /**
             * Checks if all arguments are of type numeric
             */
            @Override
            public boolean checkRule(ProviderData fSig) {

                // Get input types
                List<VariableType> argumentTypes = fSig.getInputTypes();

                // Check if all inputs are of type numeric
                for (VariableType variableType : argumentTypes) {
                    // if (variableType.getType() != CType.Numeric) {
                    if (!ScalarUtils.isScalar(variableType)) {
                        return false;
                    }
                }

                VariableType outputType = fSig.getOutputType();
                if (outputType != null) {
                    if (!(outputType instanceof StaticMatrixType)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData iSig) {
                return ColDecNumericInstance.newProvider().newCInstance(iSig);
            }
        };
    }

    /**
     * Creates a builder for empty declared matrices, of type double. The inputs have to be empty.
     * 
     * @return
     */
    public static InstanceProvider newEmptyMatrix() {
        return new MatlabInstanceProvider() {

            /**
             * Checks if does not have arguments.
             */
            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#checkRule(java.util.List)
             */
            @Override
            public boolean checkRule(ProviderData fSig) {

                // Check if declared matrices are allowed
                // if (!fSig.getFunctionSettings().useDeclaredArrays()) {
                // if (!fSig.getSetupTable().getMemoryType().useStatic()) {
                // return false;
                // }

                // Return if dynamic matrices are allowed
                if (fSig.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
                    return false;
                }

                // Get input types
                List<VariableType> argumentTypes = fSig.getInputTypes();

                // Check if it is empty
                if (!argumentTypes.isEmpty()) {
                    return false;
                }

                return true;
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#create(org.specs.CIR.Function.FunctionPrototype, java.util.List)
             */
            @Override
            public FunctionInstance create(ProviderData iSig) {
                return RowDecNumericInstance.getProvider().newCInstance(iSig);
            }

        };
    }

    /**
     * @return
     */
    public static InstanceProvider newColMatrix() {
        return new InstanceProvider() {

            /**
             * Checks if it has at least one argument, and if declared matrices are supported.
             */
            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#checkRule(java.util.List)
             */
            private boolean checkRule(ProviderData fSig) {

                // Get input types
                List<VariableType> argumentTypes = fSig.getInputTypes();

                // Check if it has at least one argument
                if (argumentTypes.isEmpty()) {
                    return false;
                }

                if (!TypeVerification.areDeclaredMatrixes(argumentTypes)) {
                    return false;
                }

                return true;
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.FunctionInstance.InstanceProvider#accepts(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public Optional<InstanceProvider> accepts(ProviderData data) {
                if (!checkRule(data)) {
                    return Optional.empty();
                }

                return Optional.of(DeclaredProvider.NEW_COL_MATRIX);
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.FunctionInstance.InstanceProvider#newCInstance(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return DeclaredProvider.NEW_COL_MATRIX.newCInstance(data);
            }
        };
    }
}
