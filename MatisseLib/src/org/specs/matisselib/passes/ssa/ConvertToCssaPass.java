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

package org.specs.matisselib.passes.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class ConvertToCssaPass extends TypeNeutralSsaPass {

    @Override
    public void apply(TypedInstance instance, DataStore data) {
	apply(instance.getFunctionBody(), data, (oldName, targetName) -> {
	    Preconditions.checkArgument(targetName != null);
	    Preconditions.checkArgument(!targetName.isEmpty());

	    // The type of the variable is the type of the phi output, not the type
	    // of the input.
	    // Otherwise, in A' = phi(B', C') we would not be able to use the same variable for A', B' and C'.
	    Optional<VariableType> variableType = instance.getVariableType(targetName);
	    if (!variableType.isPresent()) {
		System.err.println("Failed to find type of: '" + targetName + "', in "
			+ instance.getFunctionIdentification());
		System.out.println(instance);
		throw new UnsupportedOperationException();
	    }
	    return instance.makeTemporary(NameUtils.getSuggestedName(oldName), variableType.get());
	});
    }

    // Normally, we'll only use the typed overload
    // But this is so much easier to write unit tests for.
    @Override
    public void apply(FunctionBody source, DataStore data) {
	apply(source, data, (oldName, targetName) -> source.makeTemporary(NameUtils.getSuggestedName(oldName)));
    }

    public static void apply(FunctionBody body, DataStore data, BiFunction<String, String, String> allocator) {
	// See:
	// 1. Method I of Sredhar et al.'s "Translating Out of Static Single Assignment Form"
	// 2. Boissinot et al.'s "Revisiting Out-of-SSA Translation for Correctness, Code Quality,
	// and Effiency"

	// Identify all phi nodes.

	List<PhiInstruction> phiInstructions = new ArrayList<>();
	List<Integer> phiBlocks = new ArrayList<>();
	Map<Integer, Integer> lastPhiInBlock = new HashMap<>();

	List<SsaBlock> blocks = body.getBlocks();
	for (int i = 0; i < blocks.size(); i++) {
	    SsaBlock block = blocks.get(i);
	    List<SsaInstruction> instructions = block.getInstructions();
	    for (int j = 0; j < instructions.size(); j++) {
		SsaInstruction instruction = instructions.get(j);
		if (instruction instanceof PhiInstruction) {
		    phiInstructions.add((PhiInstruction) instruction);
		    phiBlocks.add(i);

		    // We don't need to compare it to the old value in lastPhiInBlock
		    // If there is any, we know it is less than j because j is increasing.
		    lastPhiInBlock.put(i, j);
		}
	    }
	}

	// Apply the algorithm for each phi instruction.

	Map<Integer, List<String>> parallelCopyFinalAssignmentInputs = new HashMap<>();
	Map<Integer, List<String>> parallelCopyFinalAssignmentOutputs = new HashMap<>();
	Map<Integer, List<String>> parallelCopySourceAssignmentInputs = new HashMap<>();
	Map<Integer, List<String>> parallelCopySourceAssignmentOutputs = new HashMap<>();

	for (int i = 0; i < phiInstructions.size(); i++) {
	    PhiInstruction phi = phiInstructions.get(i);
	    String phiOutput = phi.getOutputs().get(0);
	    List<String> inputs = phi.getInputVariables();
	    List<Integer> sourceBlocks = phi.getSourceBlocks();

	    // Create a new variable for each phi input
	    // and assign it at the end of its source block.
	    for (int j = 0; j < sourceBlocks.size(); ++j) {
		int blockId = sourceBlocks.get(j);

		String oldName = inputs.get(j);
		String newName = allocator.apply(oldName, phiOutput);

		addParallelCopy(parallelCopySourceAssignmentInputs,
			parallelCopySourceAssignmentOutputs,
			blockId,
			oldName,
			newName);

		phi.setInputVariable(j, newName);
	    }

	    // Create a new variable that will be the phi output
	    // and assign it to the old output after all phi nodes in the current block.
	    String oldOutput = phi.getOutputs().get(0);
	    String newOutput = allocator.apply(oldOutput, phiOutput);
	    phi.setOutputVariable(newOutput);

	    int currentBlockId = phiBlocks.get(i);

	    addParallelCopy(parallelCopyFinalAssignmentInputs,
		    parallelCopyFinalAssignmentOutputs,
		    currentBlockId,
		    newOutput,
		    oldOutput);
	}

	// Finally, add the parallel copies.

	for (int blockId : parallelCopySourceAssignmentInputs.keySet()) {
	    SsaBlock block = body.getBlock(blockId);

	    List<String> inputs = parallelCopySourceAssignmentInputs.get(blockId);
	    List<String> outputs = parallelCopySourceAssignmentOutputs.get(blockId);

	    int endingInstruction = block.getInstructions().size();
	    if (endingInstruction != 0 && block.getInstructions().get(endingInstruction - 1).isEndingInstruction()) {
		--endingInstruction;
	    }

	    block.insertInstruction(endingInstruction, new ParallelCopyInstruction(inputs, outputs));
	}

	for (int blockId : parallelCopyFinalAssignmentInputs.keySet()) {
	    SsaBlock block = body.getBlock(blockId);

	    List<String> inputs = parallelCopyFinalAssignmentInputs.get(blockId);
	    List<String> outputs = parallelCopyFinalAssignmentOutputs.get(blockId);

	    int lastPhi = lastPhiInBlock.get(blockId);

	    block.insertInstruction(lastPhi + 1, new ParallelCopyInstruction(inputs, outputs));
	}
    }

    private static void addParallelCopy(Map<Integer, List<String>> assignmentInputs,
	    Map<Integer, List<String>> assignmentOutputs,
	    int blockId,
	    String oldValue,
	    String newValue) {

	List<String> inputs = assignmentInputs.get(blockId);
	List<String> outputs = assignmentOutputs.get(blockId);
	if (inputs == null) {
	    inputs = new ArrayList<>();
	    outputs = new ArrayList<>();

	    assignmentInputs.put(blockId, inputs);
	    assignmentOutputs.put(blockId, outputs);
	}

	inputs.add(oldValue);
	outputs.add(newValue);
    }
}