/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIR.FunctionInstance.Instances;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Instance that represents stub functions (functions whose implementation is unknown).
 * 
 * @author Joao Bispo
 *
 */
public class StubInstance extends LiteralInstance {

    private final static String C_FILENAME = "stubs";

    private StubInstance(FunctionType functionTypes, String cFunctionName, String cFilename,
	    String cBody) {
	super(functionTypes, cFunctionName, cFilename, cBody);
    }

    public static StubInstance newInstance(String functionName, List<VariableType> inputTypes,
	    VariableType outputType, boolean outputIsPointer) {

	List<String> inputNames = SpecsFactory.newArrayList();
	for (int i = 0; i < inputTypes.size(); i++) {
	    inputNames.add("arg" + (i + 1));
	}

	FunctionType types = null;
	if (outputIsPointer) {
	    types = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "output",
		    outputType);
	} else {
	    types = FunctionType.newInstance(inputNames, inputTypes, "output", outputType);
	}

	String cFunctionName = functionName + FunctionInstanceUtils.getTypesSuffix(types.getCInputTypes());

	String cBody = "\n\n";

	return new StubInstance(types, cFunctionName, C_FILENAME, cBody);
    }

    public static String getStubFilename() {
	return C_FILENAME;
    }

}
