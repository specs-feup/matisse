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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * @author Joao Bispo
 * 
 */
public class DynamicMatrixBuilderUtils {

    /**
     * @param value
     *            Initialization value for each element of the matrix
     * @return
     */
    public static InstanceProvider newDynamicBuilder(final Number number) {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputsRange(1, 2)
                .isMatrix(0)
                .isString(1);

        return new MatlabInstanceProvider() {

            @Override
            public boolean checkRule(ProviderData data) {

                if (!data.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
                    return false;
                }

                return checker.create(data).check();
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {

                List<VariableType> inputTypes = builderData.getInputTypes();

                ProviderData pInput = ProviderData.newInstance(builderData, Arrays.asList(inputTypes.get(0)));

                NewFromMatrixTemplate template = getTemplate(number, builderData);

                return MFileProvider.getInstance(template, pInput);
            }

            private NewFromMatrixTemplate getTemplate(final Number number, ProviderData builderData) {
                VariableType elementType = MatlabToCTypesUtils.getElementType(builderData);

                NewFromMatrixTemplate template = new NewFromMatrixTemplate(number, elementType);
                return template;
            }

            @Override
            public FunctionType getType(ProviderData builderData) {
                List<VariableType> inputTypes = builderData.getInputTypes();

                ProviderData pInput = ProviderData.newInstance(builderData, Arrays.asList(inputTypes.get(0)));

                NewFromMatrixTemplate template = getTemplate(number, builderData);

                return MFileProvider.getFunctionType(template, pInput);
            }

            /* (non-Javadoc)
             * @see org.specs.MatlabToC.CirInterface.MatlabToCProvider#getInputsParser(org.specs.CIR.Functions.FunctionTypes)
             */
            @Override
            public InputsFilter getInputsFilter() {
                // Add value token
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
}
