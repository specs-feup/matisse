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

package org.specs.matisselib.passes.ssa;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.DataDependencyGraph;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Extracts nodes from loops.
 * 
 * @author Luís Reis
 *
 */
public class LoopExtractionPass extends TypeTransparentSsaPass {

    @Override
    public void apply(FunctionBody body,
	    ProviderData providerData,
	    Function<String, Optional<VariableType>> typeGetter,
	    BiFunction<String, VariableType, String> makeTemporary,
	    DataStore passData) {

	DataDependencyGraph dataDependency = null;// DataDependencyGraphBuilder.build(body);

	while (tryApply(dataDependency, body)) {
	}
    }

    private static boolean tryApply(DataDependencyGraph dataDependency, FunctionBody body) {
	List<SsaBlock> blocks = body.getBlocks();
	for (int blockId = 0; blockId < blocks.size(); blockId++) {
	    SsaBlock block = blocks.get(blockId);

	    Optional<SsaInstruction> instruction = block.getEndingInstruction();
	    if (instruction.isPresent()) {
		if (instruction.get() instanceof ForInstruction) {
		    if (tryApply(dataDependency, body, blockId, ((ForInstruction) instruction.get()).getLoopBlock())) {
			return true;
		    }
		} else if (instruction.get() instanceof WhileInstruction) {
		    if (tryApply(dataDependency, body, blockId,
			    ((WhileInstruction) instruction.get()).getLoopBlock())) {
			return true;
		    }
		}
	    }
	}

	return false;
    }

    private static boolean tryApply(DataDependencyGraph dataDependency,
	    FunctionBody body,
	    int parentBlockId,
	    int loopBlockId) {

	return false;
    }

}
