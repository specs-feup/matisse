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

import org.specs.matisselib.functionproperties.AssumeMatrixSizesMatchProperty;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.AssumeMatrixSizesMatchDirectiveInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class AssumeMatrixSizesMatchDirectiveInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof AssumeMatrixSizesMatchDirectiveInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
	    InstructionLocation location,
	    SsaInstruction instruction) {

	context.pushInstructionRemoval(location);
	context.addFunctionProperty(new AssumeMatrixSizesMatchProperty());
    }

}
