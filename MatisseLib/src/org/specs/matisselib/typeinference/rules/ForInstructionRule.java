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
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.ForTypeInferenceContext;
import org.specs.matisselib.typeinference.LoopInformationSink;
import org.specs.matisselib.typeinference.SourcedBlockContext;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class ForInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof ForInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
	ForInstruction forInstruction = (ForInstruction) instruction;

	int loop = forInstruction.getLoopBlock();
	int end = forInstruction.getEndBlock();

	LoopInformationSink loopSink = new LoopInformationSink(context.getProviderData(), true);
	loopSink.doContinue(location.getBlockId(), Collections.emptyMap(), true);

	while (loopSink.hasPendingStartBlocks()) {
	    int pendingBlock = loopSink.nextPendingStartBlock();
	    Map<String, VariableType> variableTypesStartingFrom = loopSink.getVariableTypesStartingFrom(pendingBlock);

	    ForTypeInferenceContext forContext = new ForTypeInferenceContext(
		    new SourcedBlockContext(context, pendingBlock),
		    forInstruction.getStart(), forInstruction.getInterval(),
		    loopSink,
		    variableTypesStartingFrom);

	    TypeInferencePass.inferTypes(forContext, loop);
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
