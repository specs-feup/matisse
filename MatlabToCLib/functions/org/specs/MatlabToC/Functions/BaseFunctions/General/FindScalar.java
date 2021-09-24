/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.Arrays;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;

/**
 * @author Joao Bispo
 *
 */
public class FindScalar extends AInstanceBuilder {

    /**
     * @param data
     */
    public FindScalar(ProviderData data) {
	super(data);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.FunctionInstance.InstanceHelper.AInstanceHelper#create()
     */
    @Override
    public FunctionInstance create() {
	String cFunctionName = "find_scalar_dynamic_" + getData().getInputTypes().get(0).getSmallId();
	// String cFilename = "math";

	FunctionType types = FunctionType.newInstanceNotImplementable(getData().getInputTypes(),
		getNumerics().newInt());
	InlineCode code = args -> "2";
	return new InlinedInstance(types, cFunctionName, code);
	// LiteralInstance instance = new LiteralInstance(types, cFunctionName, cFilename, "1");

	// return instance;
    }

    public FunctionInstance createOld() {

	String cFunctionName = "find_scalar_dynamic";
	String cFilename = "builtin";

	String inputName = "input";
	ScalarType firstType = getTypeAtIndex(ScalarType.class, 0);

	MatrixType outputType = DynamicMatrixType.newInstance(getNumerics().newInt());
	String outputName = "output";

	// List<String> inputNames = Arrays.asList("input", "output");
	// List<VariableType> inputTypes = CollectionUtils.asList(VariableType.class, firstType, outputType.pointer()
	// .getType(true));

	// inputNames, inputTypes, outputName, cReturnType

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(Arrays.asList(inputName),
		Arrays.asList(firstType), outputName, outputType);

	CInstructionList cBody = new CInstructionList(functionTypes);
	// String cFunctionName, String cFilename, CInstructionList cBody

	CNode condition = getFunctionCall(firstType.scalar().functions().cOperator(COperator.Equal),
		CNodeFactory.newVariable(inputName, firstType), CNodeFactory.newCNumber(0, getNumerics().newInt()));

	CNode thenInstructions = getFunctionCall(TensorProvider.NEW_EMPTY,
		CNodeFactory.newVariable(outputName, outputType.pointer().getType(true)));
	thenInstructions = CNodeFactory.newReturn(thenInstructions);

	CNode elseInstructions = CNodeFactory.newCNumber(1, getNumerics().newInt());
	elseInstructions = CNodeFactory.newReturn(elseInstructions);

	cBody.addInstruction(IfNodes.newIfThenElse(condition, thenInstructions, elseInstructions));

	InstructionsInstance instance = new InstructionsInstance(cFunctionName, cFilename, cBody);

	System.out.println("CODE:\n" + instance.getImplementationCode());

	return instance;
    }
}
