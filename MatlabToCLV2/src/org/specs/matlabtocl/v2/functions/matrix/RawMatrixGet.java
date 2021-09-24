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

package org.specs.matlabtocl.v2.functions.matrix;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.PassMessage;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;

public class RawMatrixGet implements InstanceProvider {

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	InlineCode code = tokens -> tokens.get(0).getCodeForLeftSideOf(PrecedenceLevel.ArrayAccess) + "["
		+ tokens.get(1).getCode() + "]";
	InlinedInstance instance = new InlinedInstance(getType(data),
		data.getInputTypes().get(0).getSmallId() + "$get", code);
	instance.setCallPrecedenceLevel(PrecedenceLevel.ArrayAccess);
	return instance;
    }

    @Override
    public FunctionType getType(ProviderData data) {
	if (data.getNumInputs() != 2) {
	    throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
		    "In call to raw matrix get, function must get exactly one index, got " + (data.getNumInputs() - 1));
	}

	RawBufferMatrixType matrixType = data.getInputType(RawBufferMatrixType.class, 0);
	VariableType indexType = data.getInputType(ScalarType.class, 1);
	ScalarType returnType = matrixType.getElementType();

	return FunctionTypeBuilder
		.newInline()
		.addInput(matrixType)
		.addInput(indexType)
		.returning(returnType)
		.build();
    }
}
