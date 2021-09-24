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

package org.specs.MatlabToC.Functions.MatlabOps;

import static org.specs.CIR.TypesOld.TypeVerification.*;

import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabOperatorBuilders {

    /**
     * Builder for the allocated version of the Matlab colon operator (:) when it has scalar operands.
     * 
     * Its <code>create()</code> uses <i>MatlabOperatorsAlloc.newScalarColonInstance()</i> to create and return a
     * correct instance.
     * 
     * <br />
     * <br />
     * This builder will only work if:
     * <ol>
     * <li>It is possible to use allocated matrices</li>
     * <li>There are 2 or 3 inputs</li>
     * <li>All the inputs are scalar numerics</li>
     * </ol>
     * 
     * When the colon operators is used with only 2 inputs, the boundaries, a third one is added when the instance is
     * created. It is the default step value, 1, and it is passed as the second input.
     * 
     * <br>
     * <br>
     * <code>colonop(start,end)</code> <br>
     * <br>
     * becomes <br>
     * <br>
     * <code>colonop(start,step,end)</code>
     * 
     */
    public static MatlabInstanceProvider newColonAllocBuilder() {
        return new MatlabInstanceProvider() {

            /**
             * Checks if there are two input arguments, and if they are of the type numeric.
             */
            @Override
            public boolean checkRule(ProviderData builderData) {

                List<VariableType> originalTypes = builderData.getInputTypes();
                // FunctionSettings settings = builderData.getFunctionSettings();

                // Check if we can use allocated matrices
                if (!builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
                    return false;
                }

                // We need 2 or 3 inputs
                if (!isSizeValid(originalTypes, 2, 3)) {
                    return false;
                }

                // All the inputs need to be scalars
                for (VariableType input : originalTypes) {
                    // if (input.getType() != CType.Numeric) {
                    if (!ScalarUtils.isScalar(input)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {

                if (!checkRule(builderData)) {
                    return null;
                }

                List<VariableType> inputTypes = builderData.getInputTypes();
                List<VariableType> newInputTypes = SpecsFactory.newArrayList(inputTypes);

                /*
                 * If the operator was called with only two inputs add the third one,
                 * the step, as the second input. The default value of this input is 1.
                 */
                if (inputTypes.size() == 2) {
                    // NumericData data = NumericDataFactory.newInstance(1);
                    // VariableType constantOne = VariableTypeFactoryOld.newNumeric(data);
                    // NumericFactoryG numerics = new NumericFactoryG(builderData.getSetup().getCBitSizes());
                    NumericFactory numerics = builderData.getNumerics();
                    VariableType constantOne = numerics.newInt(1);
                    newInputTypes.add(1, constantOne);
                }

                ProviderData newData = ProviderData.newInstance(builderData, newInputTypes);

                return new MatlabOperatorsAlloc(newData).create();
            }

            @Override
            public InputsFilter getInputsFilter() {
                return new InputsFilter() {

                    @Override
                    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {

                        /*
                         * If there are only 2 arguments ( start, end ), add a third one, the step value.
                         * This defaults to 1 and is the second argument so we end up having:
                         * ( start, step, end )
                         * 
                         * The outputs as inputs will be updated afterwards.
                         * 
                         */
                        if (originalArguments.size() == 2) {
                            List<CNode> newArguments = SpecsFactory.newArrayList(originalArguments);

                            CNumber cNumber = CLiteral.newInteger(1);
                            newArguments.add(1, CNodeFactory.newCNumber(cNumber));
                            return newArguments;
                        }

                        return originalArguments;
                    }
                };
            }
        };
    }

    /**
     * Builder for the declared version of the Matlab colon operator (:) when it has scalar operands.
     * 
     * Its <code>create()</code> uses from three different instances:
     * <ol>
     * <li><i>MatlabOperatorsDec.newScalarColonConsecIntsInstance()</i></li>
     * <li><i>MatlabOperatorsDec.newScalarColonSpacedIntsInstance()</i></li>
     * <li><i>MatlabOperatorsDecnewScalarColonGeneralInstance()</i></li>
     * </ol>
     * 
     * <br />
     * This builder will only work if:
     * <ol>
     * <li>It is possible to use declared matrices</li>
     * <li>There are 2 or 3 inputs</li>
     * <li>All the inputs are scalar numeric <b>constants</b></li>
     * </ol>
     * 
     * When the colon operators is used with only 2 inputs, the boundaries, a third one is added when the instance is
     * created. It is the default step value, 1, and it is passed as the second input.
     * 
     * <br>
     * <br>
     * <code>colonop(2,10)</code> <br>
     * <br>
     * becomes <br>
     * <br>
     * <code>colonop(2,1,10)</code>
     * 
     */
    public static MatlabInstanceProvider newColonDecBuilder() {
        MatlabInstanceProvider builder = new MatlabInstanceProvider() {

            @Override
            public FunctionInstance create(ProviderData builderData) {

                if (!checkRule(builderData)) {
                    return null;
                }

                List<VariableType> inputTypes = builderData.getInputTypes();
                List<VariableType> newInputTypes = SpecsFactory.newArrayList(inputTypes);

                /*
                 * If the operator was called with only two inputs add the third one,
                 * the step, as the second input. The default value of this input is 1.
                 */
                if (inputTypes.size() == 2) {
                    // NumericData data = NumericDataFactory.newInstance(1);
                    // VariableType constantOne = VariableTypeFactoryOld.newNumeric(data);
                    // NumericFactoryG numerics = new NumericFactoryG(builderData.getSetup().getCBitSizes());
                    NumericFactory numerics = builderData.getNumerics();
                    VariableType constantOne = numerics.newInt(1);
                    newInputTypes.add(1, constantOne);
                }

                VariableType start = newInputTypes.get(0);
                VariableType step = newInputTypes.get(1);
                // NumericData start = VariableTypeContent.getNumeric(newInputTypes.get(0));
                // NumericData step = VariableTypeContent.getNumeric(newInputTypes.get(1));

                // if (start.getType() == NumericType.Cint) {
                if (ScalarUtils.isInteger(start)) {
                    // if (step.getType() == NumericType.Cint) {
                    if (ScalarUtils.isInteger(step)) {

                        // int stepValue = step.getIntValue();
                        int stepValue = ScalarUtils.getConstant(step).intValue();

                        if (stepValue == 1) {
                            // Consecutive integers if start is integer and step = 1
                            return new MatlabOperatorsDec(builderData).newScalarColonConsecIntsInstance(newInputTypes);
                        }

                        // Integers with spacing > 1 if both start and step are integers
                        return new MatlabOperatorsDec(builderData).newScalarColonSpacedIntsInstance(newInputTypes);
                    }
                }

                // General case
                return new MatlabOperatorsDec(builderData).newScalarColonGeneralInstance(newInputTypes);
            }

            @Override
            public boolean checkRule(ProviderData builderData) {

                List<VariableType> originalTypes = builderData.getInputTypes();
                // FunctionSettings settings = builderData.getFunctionSettings();

                // Check if we can use allocated matrices
                // if (!builderData.getSetupTable().useStaticAllocation()) {
                if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
                    return false;
                }

                // We need 2 or 3 inputs
                if (!isSizeValid(originalTypes, 2, 3)) {
                    return false;
                }

                // All the inputs need to be scalars
                for (VariableType input : originalTypes) {
                    // if (input.getType() != CType.Numeric) {
                    if (!ScalarUtils.isScalar(input)) {
                        return false;
                    }
                }

                // All the inputs need to be constants
                // boolean allInputsConstant = true;
                for (VariableType input : originalTypes) {
                    // if (!VariableTypeContent.getNumeric(input).hasConstant()) {
                    if (!ScalarUtils.hasConstant(input)) {
                        // allInputsConstant = false;
                        return false;
                    }
                }

                // Alternatively, if all inputs are not constant,

                /*
                		// If any of the inputs is not a constant, check if it has two inputs, and they have constant length
                		if (!allInputsConstant) {
                		    if(originalTypes.size() != 2) {
                			return false;
                		    }
                		    
                		    // Get length
                		    CToken length = MatlabToCRulesUtils.getLengthToken(startIndex, endIndex)
                		    return false;
                		}
                */

                return true;
            }

            @Override
            public InputsFilter getInputsFilter() {
                return new InputsFilter() {

                    @Override
                    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {

                        /*
                         * If there are only 2 arguments ( start, end ), add a third one, the step value.
                         * This defaults to 1 and is the second argument so we end up having:
                         * ( start, step, end )
                         * 
                         * The outputs as inputs will be updated afterwards.
                         * 
                         */
                        if (originalArguments.size() == 2) {
                            List<CNode> newArguments = SpecsFactory.newArrayList(originalArguments);

                            // CNumber cNumber = CNumber.newReal(1.0);
                            CNumber cNumber = CLiteral.newInteger(1);

                            newArguments.add(1, CNodeFactory.newCNumber(cNumber));
                            return newArguments;
                        }

                        return originalArguments;
                    }
                };
            }

        };

        return builder;
    }

    /**
     * Builder for cases of a:b:c, where b != 0 && a == c. Returns a scalar value.
     */
    public static InstanceProvider newScalarBuilder() {
        return new MatlabInstanceProvider() {

            @Override
            public boolean checkRule(ProviderData data) {
                if (!new MatisseChecker(data)
                        .numOfInputsRange(2, 3)
                        .areScalar()
                        .areConstant()
                        .check()) {

                    return false;
                }

                if (data.getNumInputs() == 3) {
                    if (data.getInputType(ScalarType.class, 1).scalar().getConstant().doubleValue() == 0) {
                        return false;
                    }
                }

                double c1 = ScalarUtils.getConstant(data.getInputTypes().get(0)).doubleValue();
                double c2 = ScalarUtils.getConstant(SpecsCollections.last(data.getInputTypes())).doubleValue();

                return c1 == c2;
            }

            @Override
            public FunctionInstance create(ProviderData providerData) {
                FunctionType functionType = FunctionTypeBuilder.newInline()
                        .addInputs(providerData.getInputTypes())
                        .returning(providerData.getInputType(ScalarType.class, 0))
                        .build();
                return new InlinedInstance(functionType, "$colon_scalar", (tokens) -> tokens.get(0).getCode());
            }

        };
    }
}
