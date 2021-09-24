/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.helpers.sizeinfo;

import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class BranchInstructionBuilder implements InstructionInformationBuilder {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof BranchInstruction;
    }

    @SuppressWarnings("resource")
    @Override
    public SizeGroupInformation apply(SizeInfoBuilderContext ctx, SsaInstruction instruction) {
	SizeGroupInformation info = ctx.getCurrentInfo();

	BranchInstruction branch = (BranchInstruction) instruction;
	info = ctx.buildInfoFor(branch.getTrueBlock(), null, info);
	info = ctx.buildInfoFor(branch.getFalseBlock(), null, info);

	return ctx.buildInfoFor(branch.getEndBlock(), null, info);
    }

}
