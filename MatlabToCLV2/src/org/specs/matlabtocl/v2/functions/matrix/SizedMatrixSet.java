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
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.matisselib.PassMessage;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

import pt.up.fe.specs.util.SpecsCollections;

public class SizedMatrixSet implements InstanceProvider {

    // TODO: sets with more than 1 index.

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	SizedMatrixType matrixType = data.getInputType(SizedMatrixType.class, 0);

	InlineCode code = tokens -> {
	    CNode matrixNode = tokens.get(0);

	    CNode indexNode = tokens.get(tokens.size() - 2);
	    for (int i = tokens.size() - 3; i >= 1; --i) {
		CNode token = tokens.get(i);

		CNode dimIndexNode = CNodeFactory.newCNumber(i - 1);
		ProviderData dimData = data.createFromNodes(matrixNode, dimIndexNode);
		CNode dimNode = matrixType.matrix().functions().getDim().getCheckedInstance(dimData)
			.newFunctionCall(matrixNode, dimIndexNode);

		ProviderData multiplicationData = data.createFromNodes(indexNode, dimNode);
		FunctionInstance multiplicationInstance = CLBinaryOperator.MULTIPLICATION
			.getCheckedInstance(multiplicationData);
		CNode multipliedNode = multiplicationInstance.newFunctionCall(indexNode, dimNode);

		ProviderData additionData = data.createFromNodes(token, multipliedNode);
		FunctionInstance additionInstance = CLBinaryOperator.ADDITION
			.getCheckedInstance(additionData);
		indexNode = additionInstance.newFunctionCall(token, multipliedNode);
	    }

	    CNode valueNode = SpecsCollections.last(tokens);

	    return matrixNode.getCodeForLeftSideOf(PrecedenceLevel.MemberAccessThroughPointer)
		    + ".data[" + indexNode.getCode() + "] = "
		    + valueNode.getCodeForRightSideOf(PrecedenceLevel.Assignment);
	};
	InlinedInstance instance = new InlinedInstance(getType(data),
		data.getInputTypes().get(0).getSmallId() + "$set", code);
	instance.setCallPrecedenceLevel(PrecedenceLevel.Assignment);
	return instance;
    }

    @Override
    public FunctionType getType(ProviderData data) {
	SizedMatrixType matrixType = data.getInputType(SizedMatrixType.class, 0);

	int numInputs = data.getNumInputs();
	int numDims = numInputs - 2;

	// In a 2D (numDims==2) set, we need shape[0], so we need containedDims to be at least 1
	// (containedDims >= numDims - 1).

	if (matrixType.containedDims() < numDims - 1) {
	    throw data.getReportService().emitError(PassMessage.SPECIALIZATION_FAILURE,
		    "Could not build index for sized matrix get.");
	}

	return FunctionTypeBuilder
		.newInline()
		.addInputs(data.getInputTypes())
		.returningVoid()
		.build();
    }
}
