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

package org.specs.MatlabToC.Functions.MatlabOps.ElementWise;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.Scalar.ScalarFunctions;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.matisselib.providers.MatlabFunction;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class ElementBuilders {

    /**
     * @param rightdivision
     * @return
     */
    public static InstanceProvider newScalarMatrix(final MatlabOp op) {
	return new InstanceProvider() {
	    /*
	    	    @Override
	    	    public FunctionInstance create(ProviderData builderData) {
	    		FunctionInstance baseFunction = checkScalarMatrixRule(builderData, op);
	    		if (baseFunction == null) {
	    		    return null;
	    		}
	    
	    		// return ScalarFunctions.newScalarMatrixOp(builderData.getInputTypes(), baseFunction);
	    		return new ScalarFunctions(builderData).newScalarMatrixOp(baseFunction);
	    	    }
	    */
	    /* (non-Javadoc)
	     * @see org.specs.CIR.FunctionInstance.InstanceProvider#accepts(org.specs.CIR.FunctionInstance.ProviderData)
	     */
	    @Override
	    public Optional<InstanceProvider> accepts(ProviderData data) {
		FunctionInstance baseFunction = checkScalarMatrixRule(data, op);
		if (baseFunction == null) {
		    return Optional.empty();
		}

		return Optional.of(this);
	    }

	    /* (non-Javadoc)
	     * @see org.specs.CIR.FunctionInstance.InstanceProvider#newCInstance(org.specs.CIR.FunctionInstance.ProviderData)
	     */
	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		FunctionInstance baseFunction = checkScalarMatrixRule(data, op);
		if (baseFunction == null) {
		    throw new RuntimeException("Could not create FunctionInstance for data:\n" + data);
		}

		return new ScalarFunctions(data, baseFunction).create();
	    }

	};

    }

    private static FunctionInstance checkScalarMatrixRule(ProviderData builderData, MatlabOp op) {

	// Get the input types
	List<VariableType> inputTypes = builderData.getInputTypes();

	boolean success = new MatisseChecker(builderData).
		// Check if has two inputs
		numOfInputs(2).
		// First input should be scalar
		isScalar(0).
		// Second a matrix
		isMatrix(1).check();

	if (!success) {
	    return null;
	}

	// All inputs must be of a numeric type
	List<VariableType> normalizedInputs = SpecsFactory.newArrayList();
	for (VariableType inputType : inputTypes) {
	    VariableType normalizedInput = ScalarUtils.toScalar(inputType);
	    if (normalizedInput == null) {
		return null;
	    }

	    normalizedInputs.add(normalizedInput);
	}

	// Get implementation
	MatlabFunction fProto = op.getMatlabFunction();

	// FunctionInstance fImpl = fProto.getImplementationVar(builderData.create(normalizedInputs),
	// normalizedInputs);
	FunctionInstance fImpl = fProto.getCheckedInstance(builderData.create(normalizedInputs));

	if (fImpl == null) {
	    return null;
	}

	return fImpl;
    }

}
