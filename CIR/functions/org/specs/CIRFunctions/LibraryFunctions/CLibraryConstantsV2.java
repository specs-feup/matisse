/**
 * Copyright 2013 SPeCS Research Group.
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

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryConstant;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryConstantBase;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunctionUtils;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

/**
 * @author Joao Bispo
 * 
 */
public class CLibraryConstantsV2 {

    public enum Constant {
	EXIT_SUCCESS(SystemInclude.Stdlib),
	EXIT_FAILURE(SystemInclude.Stdlib);

	private final SystemInclude include;

	private Constant(SystemInclude include) {
	    this.include = include;
	}

	public SystemInclude getInclude() {
	    return include;
	}
    }

    private NumericFactory numericFactory;

    public CLibraryConstantsV2(NumericFactory numericFactory) {
	this.numericFactory = numericFactory;
    }

    public CNode getCToken(Constant constant) {
	FunctionInstance instance = CLibraryFunctionUtils.newInstance(getConstant(constant));
	return instance.newFunctionCall();
    }

    public CLibraryConstant getConstant(Constant constant) {
	return new CLibraryConstantBase(constant.name(), numericFactory.newInt(), constant.getInclude());
    }
}
