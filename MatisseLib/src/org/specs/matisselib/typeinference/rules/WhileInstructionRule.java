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

package org.specs.matisselib.typeinference.rules;

import java.util.Collections;
import java.util.Map;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;
import org.specs.matisselib.typeinference.LoopInformationSink;
import org.specs.matisselib.typeinference.SourcedBlockContext;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;
import org.specs.matisselib.typeinference.WhileTypeInferenceContext;

public class WhileInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof WhileInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
	WhileInstruction whileInstruction = (WhileInstruction) instruction;

	int loop = whileInstruction.getLoopBlock();
	int end = whileInstruction.getEndBlock();

	LoopInformationSink loopSink = new LoopInformationSink(context.getProviderData(), false);
	loopSink.doContinue(location.getBlockId(), Collections.emptyMap(), true);

	while (loopSink.hasPendingStartBlocks()) {
	    int pendingBlock = loopSink.nextPendingStartBlock();
	    Map<String, VariableType> variableTypesStartingFrom = loopSink.getVariableTypesStartingFrom(pendingBlock);

	    WhileTypeInferenceContext loopContext = new WhileTypeInferenceContext(
		    new SourcedBlockContext(context, pendingBlock),
		    loopSink,
		    variableTypesStartingFrom);

	    TypeInferencePass.inferTypes(loopContext, loop);
	}

	if (loopSink.isEndReachable()) {
	    loopSink.submitEndToContext(context);

	    TypeInferencePass.inferTypes(new SourcedBlockContext(context, -1), end);
	} else {
	    // Infinite loop
	    context.markUnreachable();
	}
    }

}
