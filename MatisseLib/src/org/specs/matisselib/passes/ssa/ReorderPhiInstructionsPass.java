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

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Ensures that phi instructions come before other types of instructions.
 * 
 * @author Luís Reis
 *
 */
public class ReorderPhiInstructionsPass extends TypeNeutralSsaPass {

    @Override
    public void apply(FunctionBody source, DataStore data) {
        for (int blockId = 0; blockId < source.getBlocks().size(); ++blockId) {
            SsaBlock block = source.getBlock(blockId);

            // FIXME: How do we want to deal with line instructions?

            block.getInstructions().sort(ReorderPhiInstructionsPass::compareInstructions);
        }
    }

    private static int compareInstructions(SsaInstruction instruction1, SsaInstruction instruction2) {
        boolean isPhi1 = instruction1 instanceof PhiInstruction;
        boolean isPhi2 = instruction2 instanceof PhiInstruction;

        return -Boolean.compare(isPhi1, isPhi2);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

}
