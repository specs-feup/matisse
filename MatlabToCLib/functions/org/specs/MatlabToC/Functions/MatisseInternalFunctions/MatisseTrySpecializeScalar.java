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
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.PassMessage;

public class MatisseTrySpecializeScalar implements InstanceProvider {

    private MatisseTrySpecializeScalar() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	validate(data);

	FunctionType functionType = getType(data);
	ScalarType inputType = data.getInputType(ScalarType.class, 0);

	Number constant = inputType.scalar().getConstant();

	InlineCode inlineCode = new InlineCode() {
	    @Override
	    public String getInlineCode(List<CNode> arguments) {
		if (constant == null) {
		    return arguments.get(0).getCode();
		}

		return CNodeFactory.newCNumber(constant, inputType).getCode();
	    }
	};
	return new InlinedInstance(functionType, "MATISSE_try_specialize_scalar$"
		+ constant, inlineCode);
    }

    @Override
    public FunctionType getType(ProviderData data) {
	validate(data);

	ScalarType inputType = data.getInputType(ScalarType.class, 0);
	List<VariableType> inputTypes = Arrays.asList(inputType);

	return FunctionType.newInstanceNotImplementable(inputTypes, inputType);
    }

    private static void validate(ProviderData data) {
	if (data.getInputTypes().size() != 1) {
	    throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
		    "MATISSE_try_specialize_scalar requires exactly 1 input.");
	}
	VariableType type = data.getInputTypes().get(0);
	if (!ScalarUtils.isScalar(type)) {
	    throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
		    "MATISSE_try_specialize_scalar input should be a scalar, instead got " + type);
	}
    }

    public static InstanceProvider create() {
	return new MatisseTrySpecializeScalar();
    }
}
