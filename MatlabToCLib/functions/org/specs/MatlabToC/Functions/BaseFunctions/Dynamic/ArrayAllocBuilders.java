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

package org.specs.MatlabToC.Functions.BaseFunctions.Dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Builders for ArrayCreator functions which use declared matrices.
 * 
 * @author Joao Bispo
 * 
 */
public class ArrayAllocBuilders {

    /**
     * Distinguishes between Row and Col methods.
     * 
     * @author Joao Bispo
     * 
     */
    public enum RowCol {
        ROW,
        COL;
    }

    private static final MatisseChecker EYE_CHECKER = new MatisseChecker()
            // Needs dynamic allocation
            .dynamicAllocationEnabled()
            // Between one and three (last one might be a string with the type)
            .range(1, 3)
            // All inputs must be scalar, except the last one if it is string
            .addCheck(data -> {
                VariableType lastType = SpecsCollections.last(data.getInputTypes());
                boolean lastIsString = StringTypeUtils.isString(lastType);

                List<VariableType> scalarTypes = data.getInputTypes();
                if (lastIsString) {
                    scalarTypes = scalarTypes.subList(0, scalarTypes.size() - 1);
                }

                return TypeVerification.areScalar(scalarTypes);
            });

    /**
     * Creates a builder for empty allocated matrices, of type double. The inputs have to be empty.
     * 
     * @return
     */
    public static InstanceProvider newEmptyMatrixBuilder() {
        return new InstanceProvider() {
            /* (non-Javadoc)
             * @see org.specs.CIR.FunctionInstance.InstanceProvider#accepts(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public Optional<InstanceProvider> accepts(ProviderData data) {
                if (!check(data)) {
                    return Optional.empty();
                }

                return Optional.of(TensorProvider.NEW_EMPTY);
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.FunctionInstance.InstanceProvider#newCInstance(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return TensorProvider.NEW_EMPTY.newCInstance(data);
            }

            private boolean check(ProviderData builderData) {

                // Check if allocated matrices are allowed
                if (!builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
                    return false;
                }

                // Check if there are no inputs
                if (!builderData.getInputTypes().isEmpty()) {
                    return false;
                }

                return true;
            }
        };
    }

    /**
     * Builder for numeric types of function 'row', when using allocated matrixes.
     * 
     * @return
     */
    public static InstanceProvider newRowColNumeric(final RowCol rowCol) {
        return new InstanceProvider() {

            /**
             * Checks if all arguments are of type numeric.
             */
            /* (non-Javadoc)
             * @see org.specs.CIR.Function.FunctionBuilder#checkRule(java.util.List)
             */
            private boolean checkRule(ProviderData fSig) {

                // Check if declared matrices are allowed
                if (!fSig.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
                    return false;
                }

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

                if (rowCol == RowCol.ROW) {
                    return Optional.of(TensorProvider.NEW_ROW_NUMERIC);
                }

                if (rowCol == RowCol.COL) {
                    return Optional.of(TensorProvider.NEW_COL_NUMERIC);
                }

                return Optional.empty();
            }

            /* (non-Javadoc)
             * @see org.specs.CIR.FunctionInstance.InstanceProvider#newCInstance(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                // Get input types
                // List<VariableType> argumentTypes = iSig.getInputTypes();

                return getCheckedInstance(data);
            }
        };
    }

    /**
     * @return
     */
    public static InstanceProvider newEye() {
        return new MatlabInstanceProvider() {

            @Override
            public boolean checkRule(ProviderData builderData) {
                return ArrayAllocBuilders.EYE_CHECKER.create(builderData).check();

                /*
                		List<VariableType> inputTypes = builderData.getInputTypes();
                
                		// Check if we can use allocated arrays
                		if (!builderData.getSetupTable().isDynamicAllocationAllowed()) {
                		    return false;
                		}
                
                		// We need two or three inputs ( if only two, the third one will be 'double' by default)
                		if (inputTypes.size() != 3) {
                		    return false;
                		}
                
                		// The last input must be a string
                		VariableType lastInput = inputTypes.get(inputTypes.size() - 1);
                		if (!StringTypeUtils.isString(lastInput)) {
                		    return false;
                		}
                
                		// Create a copy of the inputs without the last argument
                		List<VariableType> newInputTypes = inputTypes.subList(0, inputTypes.size() - 1);
                
                		// All inputs ( but the last ) must be scalar
                		if (!TypeVerification.areScalar(newInputTypes)) {
                		    return false;
                		}
                		return true;
                		*/
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {

                List<VariableType> inTypes = parseInputTypes(builderData);
                /*
                		List<VariableType> inTypes = FactoryUtils.newArrayList(builderData.getInputTypes());
                		NumericFactory numerics = builderData.getSetupTable().getNumerics();
                
                		// Replace the last input type, which is a string, with the corresponding element type.
                		VariableType eleType = MatlabToCUtils.getElementTypeFromString(inTypes, numerics);
                
                		inTypes.set(2, eleType);
                */
                ProviderData newData = ProviderData.newInstance(builderData, inTypes);
                return TensorProvider.EYE.newCInstance(newData);
            }

            private List<VariableType> parseInputTypes(ProviderData builderData) {
                VariableType lastType = SpecsCollections.last(builderData.getInputTypes());
                boolean lastIsString = StringTypeUtils.isString(lastType);

                int scalarInputs = builderData.getInputTypes().size();
                if (lastIsString) {
                    scalarInputs--;
                }

                List<VariableType> newTypes = new ArrayList<>(builderData.getInputTypes());

                // If only one scalar input, add a copy so it becomes a square matrix
                if (scalarInputs == 1) {
                    newTypes.add(1, newTypes.get(0));
                }

                // Get element type
                VariableType elementType = MatlabToCTypesUtils.getElementType(builderData);
                // Set element type at the third position
                if (newTypes.size() == 2) {
                    newTypes.add(elementType);
                } else {
                    newTypes.set(2, elementType);
                }

                return newTypes;
            }

            @Override
            public InputsFilter getInputsFilter() {
                return new InputsFilter() {

                    /**
                     * Discards last element if it is a string with the type.
                     */
                    @Override
                    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {

                        // Remove last argument if type is string
                        CNode lastToken = SpecsCollections.last(originalArguments);
                        if (StringTypeUtils.isString(lastToken.getVariableType())) {
                            return originalArguments.subList(0, originalArguments.size() - 1);
                        }

                        return originalArguments;

                        // Remove last argument
                        // return originalArguments.subList(0, originalArguments.size() - 1);
                    }
                };
            }
        };
    }

    /**
     * @return
     */
    public static InstanceProvider newColMatrix() {
        return new InstanceProvider() {

            /**
             * Checks if all arguments are of type numeric.
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

                if (!TypeVerification.areAllocatedMatrices(argumentTypes)) {
                    return false;
                }

                return true;
            }

            @Override
            public Optional<InstanceProvider> accepts(ProviderData data) {
                if (!checkRule(data)) {
                    return Optional.empty();
                }

                // Get input types
                // List<VariableType> argumentTypes = iSig.getInputTypes();

                return Optional.of(TensorProvider.NEW_COL_MATRIX);
            }

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return TensorProvider.NEW_COL_MATRIX.newCInstance(data);
            }
        };
    }

}
