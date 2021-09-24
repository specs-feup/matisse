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

package org.specs.MatlabToC.Functions.MathFunctions.General;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Language.Operators.COperatorProvider;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.PassMessage;

public class BitshiftScalar extends AInstanceBuilder {

    public BitshiftScalar(ProviderData data) {
	super(data);
    }

    /**
     * Input:<br>
     * - A scalar, that will be shifted;<br>
     * - A scalar, the amount to shift;<br>
     * 
     * @return
     */
    @Override
    public FunctionInstance create() {
	ScalarType valueType = getTypeAtIndex(ScalarType.class, 0);
	ScalarType amountType = getTypeAtIndex(ScalarType.class, 1);

	// Check if second input is literal
	if (!amountType.scalar().hasConstant()) {
	    throw new RuntimeException("Not yet implemented when second type is not a constant value.");
	}

	Number constant = amountType.scalar().getConstant();
	if (constant.doubleValue() != constant.intValue()) {
	    throw getData().getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
		    "Inputs of bitshift must be integers.");
	}

	int amountInt = constant.intValue();

	// As default, use left shift
	COperator shift = getShift(amountInt);

	// Inputs are always integers
	// ScalarType integerType = StdIntTypeUtils.getInteger(valueType);
	ScalarType integerType = valueType.scalar().toInteger();
	FunctionType types = FunctionType.newInstanceNotImplementable(
		Arrays.asList(integerType, getNumerics().newInt()), integerType);
	String functionName = shift.name() + "_" + amountInt;

	InlineCode code = (List<CNode> arguments) -> {
	    // Craete token for the fixed amount to shift
	    int amount = Math.abs(amountInt);

	    // Create call to shift operator
	    CNode shiftCall = getFunctionCall(new COperatorProvider(shift), arguments.get(0),
		    CNodeFactory.newCNumber(amount));

	    return shiftCall.getCode();
	};

	InlinedInstance instance = new InlinedInstance(types, functionName, code);
	instance.setCallPrecedenceLevel(PrecedenceLevel.BitShift);
	return instance;

	// Inputs are always integers
	// return getInstance(new COperatorProvider(shift), integerType, numerics().newInt());

    }

    private static COperator getShift(int amountInt) {

	// If amount is less than zero, use right shift and invert number
	if (amountInt < 0) {
	    return COperator.BitwiseRightShit;
	}

	return COperator.BitwiseLeftShit;
    }
}
