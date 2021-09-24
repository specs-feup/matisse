/**
 * Copyright 2017 SPeCS.
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

package org.specs.matisselib.passes.posttype.loopinterchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

public class MemoryLoopInterchangeFormat implements LoopInterchangeFormat {
    private static final boolean ENABLE_LOG = false;

    public List<String> computeAccessIndices(TypedInstance instance,
            int blockId,
            List<String> iterationVars,
            List<String> iterIndices,
            Map<String, String> derivedFromIndex,
            Map<String, String> safeDerivedFrom,
            List<LoopVariableImportContext> sortedLoopData) {

        log("Iteration vars: " + iterationVars);
        log("Safe derived from: " + safeDerivedFrom);

        for (SsaInstruction instruction : instance.getBlock(blockId).getInstructions()) {
            if (instruction instanceof SimpleSetInstruction
                    || instruction instanceof SimpleGetInstruction
                    || instruction instanceof GetOrFirstInstruction) {

                List<String> usedIterIndices = new ArrayList<>();

                for (String input : ((IndexedInstruction) instruction).getIndices()) {
                    if (derivedFromIndex.containsKey(input)) {
                        String source = instruction instanceof SimpleSetInstruction ? input
                                : safeDerivedFrom.getOrDefault(input, input);

                        usedIterIndices.add(source);
                    }
                }

                if (usedIterIndices.size() > 0) {
                    if (iterIndices == null) {
                        iterIndices = usedIterIndices;
                    } else if (!iterIndices.equals(usedIterIndices)) {
                        log("Different access formats: " + usedIterIndices + " vs " + iterIndices);
                        return null;
                    }
                }

            }

            if (instruction instanceof BranchInstruction) {
                BranchInstruction branch = (BranchInstruction) instruction;

                return computeAccessIndices(instance, branch.getEndBlock(), iterationVars, iterIndices,
                        derivedFromIndex,
                        safeDerivedFrom,
                        sortedLoopData);
            }

            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;

                return computeAccessIndices(instance, xfor.getEndBlock(), iterationVars, iterIndices, derivedFromIndex,
                        safeDerivedFrom, sortedLoopData);
            }

            if (instruction.getOwnedBlocks().size() != 0) {
                log("TODO: " + instruction);
                return null;
            }
        }

        if (iterIndices == null) {
            log("No iteration variables used in matrix accesses. Interchange would be pointless.");
            return null;
        }

        return iterIndices;
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[memory_loop_interchange_format] ");
            System.out.println(message);
        }
    }
}
