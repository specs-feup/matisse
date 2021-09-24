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

import java.util.ArrayList;
import java.util.List;

import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.BranchTypeInferenceContext;
import org.specs.matisselib.typeinference.SourcedBlockContext;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class BranchInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof BranchInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
        BranchInstruction branch = (BranchInstruction) instruction;

        String condition = branch.getConditionVariable();
        int trueBlock = branch.getTrueBlock();
        int falseBlock = branch.getFalseBlock();
        int endBlock = branch.getEndBlock();

        List<Integer> reachableEnds = new ArrayList<>();

        if (!context.isKnownAllFalse(condition)) {
            BranchTypeInferenceContext trueContext = new BranchTypeInferenceContext(context, condition, true);
            TypeInferencePass.inferTypes(trueContext, trueBlock);

            if (!trueContext.isInterrupted()) {
                reachableEnds.add(trueContext.getEnd());
            }
        }
        if (!context.isKnownAllTrue(condition)) {
            BranchTypeInferenceContext falseContext = new BranchTypeInferenceContext(context, condition, false);
            TypeInferencePass.inferTypes(falseContext, falseBlock);

            if (!falseContext.isInterrupted()) {
                reachableEnds.add(falseContext.getEnd());
            }
        }

        switch (reachableEnds.size()) {
        case 0:
            // No sequence.
            context.markUnreachable();
            break;
        case 1:
            // Sequence from only one point.
            // This is the case for e.g. if X, break; else ...
            TypeInferencePass.inferTypes(
                    new SourcedBlockContext(context, reachableEnds.get(0)),
                    endBlock);
            break;
        case 2:
            TypeInferencePass.inferTypes(new SourcedBlockContext(context, -1), endBlock);
            break;
        default:
            assert false;
        }
    }

}
