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

package org.specs.CIR.Utilities.CLibraryProvider;

import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public interface CLibraryProvider extends InstanceProvider {

    String getFunctionName();

    SystemInclude getLibrary();

    /**
     * The types of the inputs.
     * 
     * <p>
     * A null value represents variadic inputs that will not be checked.
     * 
     * @return
     */
    List<VariableType> getInputTypes();

    VariableType getOutputType();

    boolean canHaveSideEffects();

    /* (non-Javadoc)
     * @see org.specs.CIR.FunctionInstance.InstanceProvider#create(org.specs.CIR.FunctionInstance.ProviderData)
     */
    @Override
    public default FunctionInstance newCInstance(ProviderData data) {

	// Create FunctionTypes

	FunctionType functionTypes = FunctionTypeBuilder.newInline()
		.addInputs(getInputTypes())
		.returning(getOutputType())
		.withSideEffectsIf(canHaveSideEffects())
		.build();

	InlineCode inlineCode = new InlineCode() {

	    @Override
	    public String getInlineCode(List<CNode> arguments) {

		List<VariableType> functionInputTypes = functionTypes.getCInputTypes();

		// Input types of CLibrary functions can be null, if the function has
		// variadic inputs (e.g., printf).
		// If so, create a list of input types equal to the arguments
		if (getInputTypes() == null) {
		    functionInputTypes = SpecsFactory.newArrayList();
		    for (CNode arg : arguments) {
			functionInputTypes.add(arg.getVariableType());
		    }
		}

		return CodeGeneratorUtils.functionCallCode(getFunctionName(), functionInputTypes,
			arguments);
	    }
	};

	InlinedInstance clibInstance = new InlinedInstance(functionTypes, getFunctionName(), inlineCode);

	String selfInclude = getLibrary().getIncludeName();
	clibInstance.setSelfInclude(selfInclude);

	// Disable checkCallInputs if function as variadic input
	if (getInputTypes() == null) {
	    clibInstance.setCheckCallInputs(false);
	}

	return clibInstance;
    }

}