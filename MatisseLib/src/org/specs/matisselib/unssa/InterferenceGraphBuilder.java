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

package org.specs.matisselib.unssa;

import java.util.List;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

public class InterferenceGraphBuilder {
    private InterferenceGraphBuilder() {
    }

    public static InterferenceGraph build(FunctionBody functionBody, ControlFlowGraph cfg) {
        Preconditions.checkArgument(functionBody != null);
        Preconditions.checkArgument(cfg != null);

        LifetimeInformation lifetimeInfo = LifetimeAnalyzer.analyze(functionBody, cfg);
        ValueIdentification values = ValueIdentificationBuilder.build(functionBody, cfg);

        InterferenceGraph graph = new InterferenceGraph();

        List<SsaBlock> blocks = functionBody.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);

                List<String> outputs = instruction.getOutputs();
                graph.addVariables(outputs);
                graph.addVariables(instruction.getReferencedGlobals());
                for (String out : outputs) {
                    for (String interference : lifetimeInfo.getLiveVariablesAtExit(blockId, instructionId)) {

                        if (!out.equals(interference) && !values.haveSameValueTag(out, interference)) {
                            graph.addInterference(out, interference);
                        }

                        // Return values and globals always interfere with each other.
                        if (out.endsWith("$ret") && !interference.contains("$")) {
                            graph.addInterference(out, interference);
                        }
                    }
                }
            }
        }

        return graph;
    }
}
