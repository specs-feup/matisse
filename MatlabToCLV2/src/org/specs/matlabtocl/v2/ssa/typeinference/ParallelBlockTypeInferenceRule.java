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

package org.specs.matlabtocl.v2.ssa.typeinference;

import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.SourcedBlockContext;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelBlockInstruction;

public class ParallelBlockTypeInferenceRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof ParallelBlockInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
        ParallelBlockInstruction parallelBlock = (ParallelBlockInstruction) instruction;

        ParallelBlockTypeInferenceContext parallelBlockContext = new ParallelBlockTypeInferenceContext(context);
        TypeInferencePass.inferTypes(parallelBlockContext,
                parallelBlock.getContentBlock());
        if (parallelBlockContext.isInterrupted()) {
            context.markUnreachable();
        } else {
            TypeInferencePass.inferTypes(new SourcedBlockContext(context, -1), parallelBlock.getEndBlock());
        }
    }

}
