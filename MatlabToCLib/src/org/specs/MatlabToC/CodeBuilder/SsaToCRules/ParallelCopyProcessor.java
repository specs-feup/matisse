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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.ParallelCopySequentializer;
import org.specs.matisselib.unssa.ParallelCopySequentializer.Copy;

public class ParallelCopyProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
	return instruction instanceof ParallelCopyInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
	ParallelCopyInstruction parallelCopy = (ParallelCopyInstruction) instruction;

	List<String> ssaInputs = parallelCopy.getInputVariables();
	List<String> ssaOutputs = parallelCopy.getOutputs();

	assert ssaInputs.size() == ssaOutputs.size();

	Set<Copy> parallelCopies = new HashSet<>();

	for (int i = 0; i < ssaInputs.size(); ++i) {
	    String ssaInput = ssaInputs.get(i);
	    String ssaOutput = ssaOutputs.get(i);

	    String actualInput = builder.convertSsaToFinalName(ssaInput);
	    String actualOutput = builder.convertSsaToFinalName(ssaOutput);

	    if (actualInput.equals(actualOutput)) {
		continue;
	    }

	    parallelCopies.add(new Copy(actualInput, actualOutput));
	}

	// FIXME: freshVariable name
	// FIXME: Should we be worried about having a mix of variables of potentially different types?
	List<Copy> sequentialCopies = ParallelCopySequentializer.sequentializeParallelCopies(parallelCopies,
		"<FIXME_SEQUENTIALIZER>");

	for (Copy copy : sequentialCopies) {
	    builder.generateAssignmentForFinalNames(currentBlock, copy.getDestination(), copy.getSource());
	}
    }

}
