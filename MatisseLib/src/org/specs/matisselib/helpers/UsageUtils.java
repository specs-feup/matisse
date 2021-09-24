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

package org.specs.matisselib.helpers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.ControlFlowGraph;

public class UsageUtils {
    public static boolean isVariableUsedAfter(FunctionBody body, ControlFlowGraph cfg, InstructionLocation point,
            String variableName) {

        List<SsaInstruction> instructionsInSameBlock = body.getBlock(point.getBlockId()).getInstructions();
        for (int i = point.getInstructionId(); i < instructionsInSameBlock.size(); ++i) {
            if (instructionsInSameBlock.get(i).getInputVariables().contains(variableName)) {
                return true;
            }
        }

        Set<Integer> visitedBlocks = new HashSet<>();
        Queue<Integer> blocksToVisit = new LinkedList<>();
        blocksToVisit.addAll(cfg.getSuccessorsOf(point.getBlockId()));

        while (!blocksToVisit.isEmpty()) {
            int block = blocksToVisit.poll();
            if (visitedBlocks.contains(block)) {
                continue;
            }
            visitedBlocks.add(block);

            boolean declaredInBlock = false;
            for (SsaInstruction instruction : body.getBlock(block).getInstructions()) {
                if (instruction.getInputVariables().contains(variableName)) {
                    return true;
                }
                if (instruction.getOutputs().contains(variableName)) {
                    declaredInBlock = true;
                    break;
                }
            }

            if (!declaredInBlock) {
                for (int successor : cfg.getSuccessorsOf(block)) {
                    if (!blocksToVisit.contains(successor)) {
                        blocksToVisit.add(successor);
                    }
                }
            }
        }

        return false;
    }
}
