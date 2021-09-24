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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

public class MinMaxVectorDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxVectorDecBuilder(MinMax minOrMax) {
	this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData data) {

	if (!checkRule(data)) {
	    return null;
	}

	// Call the correct instance based on the number of outputs
	if (data.getNargouts().orElse(1) <= 1) {
	    return new MinMaxFunctions(data, minOrMax).newMinMaxVectorDecInstance();
	}

	return new MinMaxFunctions(data, minOrMax).newMinMaxVectorIndexDecInstance();

    }

    @Override
    public boolean checkRule(ProviderData builderData) {

	return new MatisseChecker(builderData)
		.numOfInputs(1)
		.isMatrix(0)
		.is1DMatrix(0)
		.numOfOutputsAtMost(2)
		.check();

    }

    @Override
    public InputsFilter getInputsFilter() {
	return new InputsFilter() {

	    @Override
	    public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

		// We only need the first input, the vector
		inputArguments = Arrays.asList(inputArguments.get(0));

		return inputArguments;
	    }
	};
    }
}
