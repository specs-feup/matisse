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

package org.specs.matlabtocl.v2.services.informationbuilders;

import org.specs.matisselib.helpers.sizeinfo.InstructionInformationBuilder;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.helpers.sizeinfo.SizeInfoBuilderContext;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelBlockInstruction;

public class ParallelBlockInformationBuilder implements InstructionInformationBuilder {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof ParallelBlockInstruction;
    }

    @SuppressWarnings("resource")
    @Override
    public SizeGroupInformation apply(SizeInfoBuilderContext ctx, SsaInstruction instruction) {
	ParallelBlockInstruction parallelBlock = (ParallelBlockInstruction) instruction;

	SizeGroupInformation info = ctx.getCurrentInfo();
	info = ctx.buildInfoFor(parallelBlock.getContentBlock(), null, info);
	return ctx.buildInfoFor(parallelBlock.getEndBlock(), null, info);
    }

}
