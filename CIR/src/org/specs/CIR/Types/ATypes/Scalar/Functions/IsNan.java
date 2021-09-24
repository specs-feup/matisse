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

package org.specs.CIR.Types.ATypes.Scalar.Functions;

import java.util.Arrays;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.CirLibrary;
import org.specs.CIR.Utilities.InputChecker.AInputsChecker;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;

import pt.up.fe.specs.util.utilities.Replacer;

public class IsNan extends AInstanceBuilder {

    public IsNan(ProviderData data) {
	super(data);
    }

    @Override
    public FunctionInstance create() {

	// Get first scalar
	ScalarType scalarType = getTypeAtIndex(ScalarType.class, 0);

	String inputName = "x";
	String outputName = "out";
	VariableType intType = getNumerics().newInt();

	// Build function types
	FunctionType functionTypes = FunctionType.newInstance(Arrays.asList(inputName), Arrays.asList(scalarType),
		outputName, intType);

	String functionName = "isnan_" + scalarType.getSmallId();
	String libName = CirLibrary.MATH.getName();

	// Taken from: http://www.devx.com/tips/Tip/42853
	String code = "   volatile <INPUT_TYPE> temp = x;\n" +
		"   return temp != x;\n";

	Replacer replacer = new Replacer(code);
	replacer.replace("<INPUT_TYPE>", scalarType.code().getSimpleType());

	return new LiteralInstance(functionTypes, functionName, libName, replacer.toString());
    }

    public static InstanceProvider getProvider() {
	AInputsChecker<?> checker = new CirInputsChecker()
		// Accepts one input
		.numOfInputs(1)
		// First input must be scalar
		.isScalar(0);

	return new GenericInstanceProvider(checker, data -> new IsNan(data).create());
    }
}
