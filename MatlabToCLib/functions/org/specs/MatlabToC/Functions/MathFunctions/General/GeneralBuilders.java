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

package org.specs.MatlabToC.Functions.MathFunctions.General;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Language.Operators.COperatorBuilder;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunction;
import org.specs.CIRFunctions.Utilities.UtilityProvider;
import org.specs.CIRTypes.Types.Logical.LogicalType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * @author Joao Bispo
 * 
 */
public class GeneralBuilders {

    /**
     * @return
     */
    public static MatlabInstanceProvider newSignNumericBuilder() {
        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                // Check if input is a single numeric type
                List<VariableType> inputTypes = data.getInputTypes();
                if (inputTypes.size() != 1) {
                    return false;
                }

                // if (!TypeVerification.areOfType(inputTypes, CType.Numeric)) {
                if (!TypeVerification.areScalar(inputTypes)) {
                    return false;
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                /*
                // Check if input is a single numeric type
                List<VariableType> inputTypes = builderData.getInputTypes();
                if (inputTypes.size() != 1) {
                    return null;
                }
                
                // if (!TypeVerification.areOfType(inputTypes, CType.Numeric)) {
                if (!TypeVerification.areScalar(inputTypes)) {
                    return null;
                }
                */
                return UtilityProvider.SIGN.newCInstance(builderData);
            }

        };
    }

    public static MatlabInstanceProvider newIntegerModBuilder() {
        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                // Check if input is two scalar integers
                List<VariableType> inputTypes = data.getInputTypes();
                if (inputTypes.size() != 2) {
                    return false;
                }

                if (!TypeVerification.areScalar(inputTypes)) {
                    return false;
                }

                if (!ScalarUtils.isInteger(inputTypes.get(0))) {
                    return false;
                }

                if (!ScalarUtils.isInteger(inputTypes.get(1))) {
                    return false;
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                /*
                // Check if input is two scalar integers
                List<VariableType> inputTypes = builderData.getInputTypes();
                if (inputTypes.size() != 2) {
                    return null;
                }
                
                // if (!TypeVerification.areOfType(inputTypes, CType.Numeric)) {
                if (!TypeVerification.areScalar(inputTypes)) {
                    return null;
                }
                
                if (!ScalarUtils.isInteger(inputTypes.get(0))) {
                    return null;
                }
                
                if (!ScalarUtils.isInteger(inputTypes.get(1))) {
                    return null;
                }
                */

                return new COperatorBuilder(builderData, COperator.Modulo).create();

            }

        };
    }

    /**
     * @return
     */
    public static MatlabInstanceProvider newCastNumericBuilder(final NumericClassName className) {
        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                // Check if input is a single numeric type
                List<VariableType> inputTypes = data.getInputTypes();
                if (inputTypes.size() != 1) {
                    return false;
                }

                VariableType inputType = inputTypes.get(0);

                if (!ScalarUtils.isScalar(inputType)) {
                    return false;
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                VariableType inputType = builderData.getInputTypes().get(0);

                return new GeneralFunctions(builderData.getSettings()).newCast(inputType, className);
            }

        };
    }

    public static MatlabInstanceProvider newCastLogicalBuilder() {
        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                // Check if input is a single numeric type
                List<VariableType> inputTypes = data.getInputTypes();
                if (inputTypes.size() != 1) {
                    return false;
                }

                VariableType inputType = inputTypes.get(0);

                if (!ScalarUtils.isScalar(inputType)) {
                    return false;
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                VariableType inputType = builderData.getInputTypes().get(0);

                FunctionType functionType = FunctionTypeBuilder
                        .newInline()
                        .addInput(inputType)
                        .returning(LogicalType.newInstance())
                        .build();

                InlineCode code = tokens -> {
                    return tokens.get(0).getCodeForLeftSideOf(PrecedenceLevel.Equality) + " != 0";
                };
                InlinedInstance instance = new InlinedInstance(
                        functionType,
                        "$cast_to_logical$" + inputType.getSmallId(),
                        code);
                instance.setCallPrecedenceLevel(PrecedenceLevel.Equality);
                return instance;
            }
        };
    }

    /**
     * @return
     */
    public static MatlabInstanceProvider newFprintfScalar() {
        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                List<VariableType> inputTypes = data.getInputTypes();

                // Check if it has at least one input
                if (inputTypes.isEmpty()) {
                    return false;
                }

                // Check if first argument is of type string
                if (!StringTypeUtils.isString(inputTypes.get(0))) {
                    return false;
                }

                // Check if remaining arguments are not matrices
                for (VariableType type : SpecsCollections.subList(inputTypes, 1)) {
                    if (MatrixUtils.isMatrix(type)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                /*
                		List<VariableType> inputTypes = builderData.getInputTypes();
                
                		// Check if it has at least one input
                		if (inputTypes.isEmpty()) {
                		    return null;
                		}
                
                		// Check if first argument is of type string
                		if (!StringTypeUtils.isString(inputTypes.get(0))) {
                		    return null;
                		}
                
                		// Check if remaining arguments are not matrices
                		for (VariableType type : CollectionUtils.subList(inputTypes, 1)) {
                		    if (MatrixUtilsV2.isMatrix(type)) {
                			return null;
                		    }
                		}
                */
                return CStdioFunction.PRINTF.newCInstance(builderData);
            }

        };
    }

    /**
     * @return
     */
    public static MatlabInstanceProvider newCLibraryBuilder(CLibraryFunction doubleVersion,
            CLibraryFunction singleVersion) {

        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                // Using rules of double version, however both versions should have same rules
                return doubleVersion.checkArgs(data);
                /*
                // Check if input is a single numeric type
                List<VariableType> inputTypes = data.getInputTypes();
                if (inputTypes.size() != 1) {
                    return false;
                }
                
                VariableType inputType = inputTypes.get(0);
                
                if (!ScalarUtils.isScalar(inputType)) {
                    return false;
                }
                
                return true;
                */

            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                VariableType inputType = builderData.getInputTypes().get(0);

                // If single, return sinf
                if (ScalarUtils.isSinglePrecision(inputType)) {
                    // return CMathFunction.COSF.newCInstance(builderData);
                    return singleVersion.newCInstance(builderData);
                }

                // return CMathFunction.COS.newCInstance(builderData);
                return doubleVersion.newCInstance(builderData);

            }

        };
    }

}
