/**
 * Copyright 2016 SPeCS.
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
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.PassMessage;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

public class SizedMatrixDim implements InstanceProvider {

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	VariableType matrixType = data.getInputTypes().get(0);
	ScalarType dimType = data.getInputType(ScalarType.class, 1);
	int dim = dimType.scalar().getConstant().intValue();

	String functionName = "$sized_matrix_dim$" + matrixType.getSmallId() + "," + dim;
	InlinedInstance instance = new InlinedInstance(getType(data), functionName, tokens -> {
	    CNode matrixNode = tokens.get(0);

	    return matrixNode.getCodeForLeftSideOf(PrecedenceLevel.MemberAccessThroughPointer) + ".dim" + (dim + 1);
	});
	instance.setCallPrecedenceLevel(PrecedenceLevel.MemberAccessThroughPointer);

	return instance;
    }

    @Override
    public FunctionType getType(ProviderData data) {
	SizedMatrixType inputType = data.getInputType(SizedMatrixType.class, 0);
	ScalarType dimType = data.getInputType(ScalarType.class, 1);

	if (!dimType.scalar().hasConstant()) {
	    throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
		    "Matrix type only supports fixed dimensions, got " + dimType);
	}

	Number constant = dimType.scalar().getConstant();
	if (constant.doubleValue() != constant.intValue()) {
	    throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
		    "Non-integer constant");
	}

	int dim = constant.intValue();

	if (dim < 0) {
	    throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
		    "Negative dimension");
	}

	if (inputType.containedDims() <= dim) {
	    throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
		    "Matrix has too few known dimensions, needed " + (dim + 1) + ", had " + inputType.containedDims());
	}

	return FunctionTypeBuilder.newInline()
		.addInput(inputType)
		.addInput(dimType)
		.returning(CLNativeType.SIZE_T)
		.build();
    }

}
