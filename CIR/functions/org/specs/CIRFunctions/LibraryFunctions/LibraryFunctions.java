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

package org.specs.CIRFunctions.LibraryFunctions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.Pointer.PointerType;

public class LibraryFunctions {

    public static InstanceProvider newMalloc(final VariableType type) {

	return data -> {
	    FunctionType types = FunctionType.newInstanceNotImplementable(null, new PointerType(type));
	    String functionName = "malloc_" + type.getSmallId();

	    InlineCode code = arguments -> {
		String argCode = arguments.get(0).getCodeForRightSideOf(PrecedenceLevel.Multiplication);
		return "malloc(sizeof(" + type.code().getSimpleType() + ") * " + argCode + ")";
	    };

	    InlinedInstance instance = new InlinedInstance(types, functionName, code);
	    instance.setCheckCallInputs(false);
	    instance.setCustomCallIncludes(SystemInclude.Stdlib.getIncludeName());

	    return instance;
	};

    }

    /**
     * Receives as input the input matrix, the output matrix and the number of bytes to copy.
     * 
     * @param input
     * @param output
     * @return
     */
    public static InstanceProvider newMemcpyDec(MatrixType input, MatrixType output) {

	return data -> {
	    List<VariableType> inputTypes = Arrays.asList(output, input);

	    FunctionType types = FunctionType.newInstanceNotImplementable(inputTypes, output);
	    String functionName = "memcpy_" + input.getSmallId() + "_" + input.getTypeShape().getNumElements();

	    InlineCode code = arguments -> {
		return "memcpy(" + arguments.get(1).getCode() + ", " + arguments.get(0).getCode() + ", " + "sizeof("
			+ input.code().getSimpleType() + ") * " + input.getTypeShape().getNumElements()
			+ ")";
	    };

	    InlinedInstance instance = new InlinedInstance(types, functionName, code);
	    instance.setCheckCallInputs(false);
	    instance.setCustomCallIncludes(SystemInclude.String.getIncludeName());

	    return instance;
	};

    }
}
