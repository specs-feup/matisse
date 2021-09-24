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

package org.specs.matisselib.matlabinference;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabLanguage.MatlabClass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Datakey.KeyUser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.classmap.BiConsumerClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatlabTypesPass implements SsaPass, KeyUser {

    // Key for the map with the inferred MATLAB types in the SSA
    public static final DataKey<MatlabTypesTable> MATLAB_SSA_TYPES = KeyFactory.object("matlab_ssa_types",
	    MatlabTypesTable.class);

    private static BiConsumerClassMap<SsaInstruction, DataStore> PROCESS_MAP;

    static {
	MatlabTypesPass.PROCESS_MAP = new BiConsumerClassMap<>();
	MatlabTypesPass.PROCESS_MAP.put(LineInstruction.class, MatlabTypesPass::nullOp);
	MatlabTypesPass.PROCESS_MAP.put(ArgumentInstruction.class, MatlabTypesPass::argInst);
	MatlabTypesPass.PROCESS_MAP.put(AssignmentInstruction.class, MatlabTypesPass::assignInst);
	MatlabTypesPass.PROCESS_MAP.put(UntypedFunctionCallInstruction.class, MatlabTypesPass::assignCall);
	MatlabTypesPass.PROCESS_MAP.put(CommentInstruction.class, MatlabTypesPass::nullOp);
    }

    @Override
    public Collection<DataKey<?>> getWriteKeys() {
	return Arrays.asList(MatlabTypesPass.MATLAB_SSA_TYPES);
    }

    @Override
    public void apply(FunctionBody source, DataStore data) {
	// TODO: Add a pass in MATLAB code level, which maps the argument index of the function inputs to a MatlabType
	// Right now, just defining them manually, to test approach
	MatlabTypesTable typesTable = new MatlabTypesTable();
	typesTable.addArgument(0, MatlabType.newScalar(MatlabClass.DOUBLE));

	// Start MATLAB SSA types
	data.add(MatlabTypesPass.MATLAB_SSA_TYPES, typesTable);

	// Start with a block, iterate over all blocks that are called by the code / or just iterating over them is
	// enough?
	for (SsaBlock block : source.getBlocks()) {
	    for (SsaInstruction instruction : block.getInstructions()) {
		MatlabTypesPass.PROCESS_MAP.accept(instruction, data);
		/*
		BiConsumer<SsaInstruction, DataStore> processor = PROCESS_MAP.get(instruction);
		
		if (processor == null) {
		    throw new NotImplementedException("SsaInstructionProcessor not defined for class '"
			    + instruction.getClass() + "'");
		}
		
		processor.accept(instruction, data);
		*/
	    }
	}

	System.out.println("TYPES:" + data.get(MatlabTypesPass.MATLAB_SSA_TYPES));
    }

    private static void nullOp(SsaInstruction instruction, DataStore data) {
	return;
    }

    /**
     * 
     * @param instruction
     * @param data
     */
    private static void argInst(ArgumentInstruction instruction, DataStore data) {
	MatlabTypesTable table = data.get(MatlabTypesPass.MATLAB_SSA_TYPES);

	// Assignment from argument, just copy the type to output
	MatlabType argType = table.getArgument(instruction.getArgumentIndex());
	table.addVariable(instruction.getOutput(), argType);

    }

    /**
     * 
     * @param instruction
     * @param data
     */
    private static void assignInst(AssignmentInstruction instruction, DataStore data) {
	MatlabTypesTable table = data.get(MatlabTypesPass.MATLAB_SSA_TYPES);

	// Define the output type according to the input
	MatlabType type = getType(instruction.getInput(), table);
	String name = instruction.getOutput();

	table.addVariable(name, type);
    }

    private static MatlabType getType(Input input, MatlabTypesTable table) {
	// Return a double scalar
	if (input instanceof NumberInput) {
	    return MatlabType.newScalar(MatlabClass.DOUBLE);
	}

	// Return the type of the variable
	if (input instanceof VariableInput) {
	    String name = ((VariableInput) input).getName();
	    return table.getVariable(name);
	}

	throw new NotImplementedException("Case not implemented: " + input.getClass().getName() + " -> " + input);
    }

    /**
     * 
     * @param instruction
     * @param data
     */
    private static void assignCall(UntypedFunctionCallInstruction instruction, DataStore data) {
	MatlabTypesTable table = data.get(MatlabTypesPass.MATLAB_SSA_TYPES);
	System.out.println("INST:" + instruction);

	// Get output types of function call, according to the inputs
	String functionCall = instruction.getFunctionName();
	List<MatlabType> inputTypes = instruction.getInputVariables().stream()
		.map(var -> table.getVariable(var))
		.collect(Collectors.toList());

	System.out.println("Input types: " + inputTypes);

	MatlabFunctionType matlabFunction = MatlabInferenceUtils.getFunctionType(functionCall, data);
	System.out.println("Function: " + matlabFunction);

	// TODO?
    }
}
