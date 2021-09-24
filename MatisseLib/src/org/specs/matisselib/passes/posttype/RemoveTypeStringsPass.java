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

package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * Removes the type strings from functions such as "zeros", since these are redundant after type inference. This allows
 * us to generate cleaner code.
 * 
 * @author Luís Reis
 *
 */
public class RemoveTypeStringsPass extends TypeTransparentSsaPass {

    private static final List<String> TYPE_AWARE_FUNCTIONS = Arrays.asList("zeros", "ones", "eye",
	    "matisse_new_array_from_dims");

    /**
     * This function is public so that unit tests can call it directly.
     */
    @Override
    public void apply(FunctionBody body,
	    ProviderData providerData,
	    Function<String, Optional<VariableType>> typeGetter,
	    BiFunction<String, VariableType, String> makeTemporary,
	    DataStore passData) {

	Preconditions.checkArgument(body != null);
	Preconditions.checkArgument(typeGetter != null);
	Preconditions.checkArgument(makeTemporary != null);
	Preconditions.checkArgument(passData != null);

	removeTypeStrings(body, providerData, typeGetter, makeTemporary, passData);
    }

    private static void removeTypeStrings(FunctionBody body,
	    ProviderData providerData,
	    Function<String, Optional<VariableType>> typeGetter,
	    BiFunction<String, VariableType, String> makeTemporary,
	    DataStore passData) {

	List<SsaBlock> blocks = body.getBlocks();
	for (int blockId = 0; blockId < blocks.size(); blockId++) {
	    SsaBlock block = blocks.get(blockId);

	    List<SsaInstruction> instructions = block.getInstructions();
	    for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
		SsaInstruction instruction = instructions.get(instructionId);

		if (instruction instanceof TypedFunctionCallInstruction) {
		    TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

		    if (RemoveTypeStringsPass.TYPE_AWARE_FUNCTIONS.contains(functionCall.getFunctionName())) {
			removeTypeArgument(body, providerData, typeGetter, makeTemporary, block, instructionId,
				functionCall,
				passData);
		    }
		}
	    }
	}
    }

    private static void removeTypeArgument(FunctionBody body,
	    ProviderData providerData,
	    Function<String, Optional<VariableType>> typeGetter,
	    BiFunction<String, VariableType, String> makeTemporary,
	    SsaBlock block,
	    int instructionId,
	    TypedFunctionCallInstruction instruction,
	    DataStore passData) {

	List<String> inputVariables = instruction.getInputVariables();
	if (inputVariables.size() < 2) {
	    return;
	}

	String lastInput = inputVariables.get(inputVariables.size() - 1);
	VariableType variableType = typeGetter.apply(lastInput).get();

	if (!(variableType instanceof StringType)) {
	    return;
	}

	FunctionType originalFunctionType = instruction.getFunctionType();

	List<VariableType> inputTypes = new ArrayList<>(originalFunctionType.getArgumentsTypes());
	List<String> inputs = new ArrayList<>(instruction.getInputVariables());

	if (inputTypes.size() == inputs.size()) {
	    // The zeros(Matrix, String) overload "filters" the string arguments and removes them
	    // As such, there is no string input type to remove.
	    inputTypes.remove(inputTypes.size() - 1);
	}
	inputs.remove(inputs.size() - 1);
	List<VariableType> outputTypes = originalFunctionType.getOutputTypes();

	SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

	String functionName = instruction.getFunctionName();
	InstanceProvider instanceProvider = systemFunctions.getSystemFunction(functionName).get();
	ProviderData data = providerData.create(inputTypes);
	data.setOutputType(outputTypes);
	FunctionType newFunctionType = instanceProvider.getType(data);

	TypedFunctionCallInstruction newInstruction = new TypedFunctionCallInstruction(functionName, newFunctionType,
		instruction.getOutputs(), inputs);

	block.replaceInstructionAt(instructionId, newInstruction);
    }
}
