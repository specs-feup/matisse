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
import java.util.Optional;
import java.util.function.Supplier;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.providers.MatlabFunctionProvider;

import pt.up.fe.specs.util.SpecsFactory;

public class ElementWiseBuilder implements InstanceProvider {

    private final Supplier<InstanceProvider> providerSource;
    private final int arity;

    private ElementWiseBuilder(Supplier<InstanceProvider> providerSource, int arity) {

        if (arity < 1) {
            throw new IllegalArgumentException("Arity should have the value of 1 or more.");
        }

        this.providerSource = providerSource;
        this.arity = arity;

    }

    public ElementWiseBuilder(InstanceProvider provider, int arity) {
        this(() -> provider, arity);
    }

    public ElementWiseBuilder(MatlabFunctionProvider function, int arity) {
        this(() -> (InstanceProvider) function.getMatlabFunction(), arity);
    }

    private InstanceProvider checkRule(ProviderData builderData) {

        // Get the input types
        List<VariableType> inputTypes = builderData.getInputTypes();

        // Check the arity of the operator / function
        if (inputTypes.size() != arity) {
            return null;
        }

        // All inputs must be matrices
        for (VariableType inputType : inputTypes) {

            if (!MatrixUtils.isMatrix(inputType)) {
                return null;
            }

        }

        return providerSource.get();
        /*
        	// All matrices must be of a numeric type
        	List<VariableType> normalizedInputs = FactoryUtils.newArrayList();
        	for (VariableType inputType : inputTypes) {
        
        	    VariableType numericInput = ScalarUtils.toScalar(inputType);
        	    if (numericInput == null) {
        		return null;
        	    }
        
        	    normalizedInputs.add(numericInput);
        	}
        
        	// Get the operator implementation for the inner type of the matrices
        	MatlabFunction fProto = function.getMatlabFunction();
        */
    }

    /*
        @Override
        public FunctionInstance create(ProviderData builderData) {
    
    	FunctionInstance baseFunction = checkRule(builderData);
    	if (baseFunction == null) {
    	    return null;
    	}
    
    	return ElementWiseInstance.newProvider(baseFunction).newCInstance(builderData);
        }
    */
    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
        InstanceProvider baseFunction = checkRule(data);
        if (baseFunction == null) {
            return Optional.empty();
        }

        return Optional.of(this);
        // return Optional.of(ElementWiseInstance.newProvider(baseFunction));
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        List<VariableType> normalizedInputs = SpecsFactory.newArrayList();
        for (VariableType inputType : data.getInputTypes()) {

            VariableType numericInput = ScalarUtils.toScalar(inputType);
            normalizedInputs.add(numericInput);
        }
        FunctionInstance baseInstance = checkRule(data).getCheckedInstance(data.create(normalizedInputs));

        return ElementWiseInstance.newProvider(baseInstance).newCInstance(data);
    }

}
