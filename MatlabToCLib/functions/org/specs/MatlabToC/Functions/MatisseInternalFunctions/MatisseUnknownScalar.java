/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabToC.Functions.MatisseInternalFunctions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.MatlabToC.Utilities.MatisseChecker;

public class MatisseUnknownScalar implements InstanceProvider {

    private MatisseUnknownScalar() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	FunctionType functionType = getType(data);
	InlineCode inlineCode = new InlineCode() {
	    @Override
	    public String getInlineCode(List<CNode> arguments) {
		return arguments.get(0).getCode();
	    }
	};
	return new InlinedInstance(functionType, "MATISSE_unknown_scalar$"
		+ data.getInputType(ScalarType.class, 0).getSmallId(), inlineCode);
    }

    @Override
    public FunctionType getType(ProviderData data) {
	ScalarType inputType = data.getInputType(ScalarType.class, 0);
	List<VariableType> inputTypes = Arrays.asList(inputType);
	VariableType returnType = inputType.scalar().removeConstant();

	return FunctionType.newInstanceNotImplementable(inputTypes, returnType);
    }

    public static InstanceProvider create() {
	Checker checker = new MatisseChecker()
		.numOfInputs(1)
		.numOfOutputsAtMost(1)
		.isScalar(0);

	return new GenericInstanceProvider(checker, new MatisseUnknownScalar());
    }
}
