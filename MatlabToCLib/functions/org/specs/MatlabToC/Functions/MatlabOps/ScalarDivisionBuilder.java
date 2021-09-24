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

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

/**
 * @author Joao Bispo
 * 
 */
public class ScalarDivisionBuilder implements MatlabInstanceProvider {

    private final boolean invertArguments;

    /**
     * @param invertArguments
     */
    public ScalarDivisionBuilder(boolean invertArguments) {
	this.invertArguments = invertArguments;
    }

    /**
     * Checks if there are two input arguments, and if they are of the type numeric.
     */
    @Override
    public boolean checkRule(ProviderData fSig) {

	// Get input types
	List<VariableType> argumentTypes = fSig.getInputTypes();

	// Check number of inputs
	if (argumentTypes.size() != 2) {
	    // if (argumentTypes.size() != 3) {
	    return false;
	}

	// Check if first two inputs are of type numeric
	for (VariableType type : argumentTypes) {
	    // if (type.getType() != CType.Numeric) {
	    if (!ScalarUtils.isScalar(type)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Inputs and outputs are always double.
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionBuilder#create(org.specs.CIR.Function.FunctionPrototype, java.util.List)
     */
    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	// List<VariableType> givenTypes = builderData.getInputTypes();
	// return ScalarDivisionInstance.newInstance(givenTypes, invertArguments,
	// builderData.getFunctionSettings().getDefaultFloat());
	// return ScalarDivisionInstance.newProvider(invertArguments).create(builderData);
	FunctionInstance inst = ScalarDivisionInstance.newProvider(invertArguments).newCInstance(builderData);

	return inst;

    }

}
