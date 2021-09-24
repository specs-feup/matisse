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

package org.specs.MatlabToC.Functions.Misc;

import java.util.Arrays;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ClassBuilder extends AInstanceBuilder {
    private ClassBuilder(ProviderData data) {
	super(data);
    }

    public static InstanceProvider newProvider() {
	Checker checker = new MatisseChecker()
		.numOfInputs(1);
	return new GenericInstanceProvider(checker, data -> new ClassBuilder(data).create());
    }

    @Override
    public FunctionInstance create() {
	VariableType inputType = getData().getInputTypes().get(0);
	String typeName = getTypeName(inputType);
	int charBitSize = getNumerics().getSizes().getCharSize();
	StringType stringType = StringType.create(typeName, charBitSize, true);

	String functionName = getFunctionName("class", stringType);

	FunctionType functionType = FunctionType.newInstanceNotImplementable(Arrays.asList(inputType), stringType);

	return new InlinedInstance(functionType, functionName, args -> {
	    return CNodeFactory.newString(typeName, charBitSize).getCode();
	});
    }

    private static String getTypeName(VariableType inputType) {
	if (inputType instanceof StringType) {
	    return "char";
	}
	if (inputType instanceof MatrixType) {
	    return getTypeName(((MatrixType) inputType).matrix().getElementType());
	}
	if (inputType instanceof ScalarType) {
	    return getTypeName((ScalarType) inputType);
	}

	throw new NotImplementedException("Class for " + inputType);
    }

    private static String getTypeName(ScalarType inputType) {
	int numBits = inputType.scalar().getBits();

	if (inputType.scalar().isInteger()) {
	    String typeName;

	    switch (numBits) {
	    case 8:
		typeName = "int8";
		break;
	    case 16:
		typeName = "int16";
		break;
	    case 32:
		typeName = "int32";
		break;
	    case 64:
		typeName = "int64";
		break;
	    default:
		throw new NotImplementedException("Integer type of " + numBits + " bits");
	    }

	    if (inputType.scalar().isUnsigned()) {
		return "u" + typeName;
	    }
	    return typeName;
	} else if (numBits == 32) {
	    return "single";
	} else if (numBits == 64) {
	    return "double";
	}

	throw new NotImplementedException("Floating point type of " + numBits + " bits");
    }
}
