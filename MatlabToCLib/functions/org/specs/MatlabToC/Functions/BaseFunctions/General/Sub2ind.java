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

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * @author Joao Bispo
 * 
 */
public class Sub2ind {

    /**
     * @return
     */
    public static MatlabInstanceProvider newScalarIndexesBuilder() {
        MatisseChecker checker = new MatisseChecker().
        // Check if at least 2 inputs
                numOfInputsAtLeast(2).
                // Check if input 1 is matrix
                isMatrix(0).
                // Check if remaining inputs are scalar
                range(1).areScalar();

        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                return checker.create(data).check();
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {

                List<VariableType> inputTypes = builderData.getInputTypes();
                /*
                		// Init boolean var
                		boolean success = true;
                
                		// Check if at least 2 inputs
                		success = CheckUtils.numOfInputsAtLeast(inputTypes, 2, success);
                		// Check if input 1 is matrix
                		success = CheckUtils.isMatrix(inputTypes, 0, success);
                		// Check if remaining inputs are scalar
                		for (int i = 1; i < inputTypes.size(); i++) {
                		    success = CheckUtils.isScalar(inputTypes, i, success);
                		}
                
                		if (!success) {
                		    return null;
                		}
                */
                MatlabTemplate sub2ind = new Sub2indScalarIndexes(inputTypes.size() - 1,
                        builderData.getMemoryLayout());
                return MFileProvider.getInstance(sub2ind, builderData);
            }

        };

    }
}
