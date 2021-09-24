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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.MakeEmptyMatrixInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MakeEmptyMatrixProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
	return instruction instanceof MakeEmptyMatrixInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
	MakeEmptyMatrixInstruction makeEmpty = (MakeEmptyMatrixInstruction) instruction;

	String output = makeEmpty.getOutput();

	VariableNode outputNode = builder.generateVariableNodeForSsaName(output);
	if (outputNode.getVariableType() instanceof DynamicMatrixType) {

	    DynamicMatrixType dynamicMatrixType = (DynamicMatrixType) outputNode.getVariableType();
	    InstanceProvider createProvider = dynamicMatrixType.matrix().functions()
		    .create();
	    CNumberNode zeroNode = CNodeFactory.newCNumber(0);
	    VariableType zeroType = zeroNode.getVariableType();

	    List<VariableType> inputTypes = new ArrayList<>();
	    inputTypes.add(zeroType);
	    inputTypes.add(zeroType);
	    ProviderData providerData = builder.getCurrentProvider().createWithContext(inputTypes);
	    providerData.setNargouts(1);
	    providerData.setOutputType(dynamicMatrixType);
	    currentBlock.addFunctionCall(createProvider.newCInstance(providerData), zeroNode, zeroNode, outputNode);

	} else if (outputNode.getVariableType() instanceof StaticMatrixType) {
	    // Do nothing.
	} else {
	    throw new NotImplementedException(outputNode.getVariableType().getClass());
	}
    }

}
