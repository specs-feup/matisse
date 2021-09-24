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

package org.specs.matisselib.passes.posttype;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class DuplicatedReadEliminationPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "duplicated_read_elimination";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.logSkip(instance);
            return;
        }

        logger.logStart(instance);

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        ControlFlowGraph cfg = dataProvider.buildData(CompilerDataProviders.CONTROL_FLOW_GRAPH);

        Map<GetAccess, String> accesses = new HashMap<>();
        Map<GetAccess, InstructionLocation> accessSources = new HashMap<>();
        Map<String, String> newNames = new HashMap<>();

        boolean applied = false;

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            int instructionId = 0;
            for (ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator(); iterator.hasNext();) {
                ++instructionId;

                SsaInstruction instruction = iterator.next();
                if (instruction instanceof MatrixGetInstruction || instruction instanceof SimpleGetInstruction) {
                    IndexedInstruction get = (IndexedInstruction) instruction;

                    String matrix = get.getInputMatrix();
                    List<String> indices = get.getIndices();
                    String output = get.getOutputs().get(0);
                    Optional<VariableType> outputType = instance.getVariableType(output);

                    GetAccess access = new GetAccess(matrix, indices, outputType);
                    InstructionLocation instructionLocation = new InstructionLocation(blockId, instructionId);

                    if (accesses.containsKey(access)) {
                        if (BlockUtils.covers(cfg, accessSources.get(access), instructionLocation)) {
                            iterator.remove();

                            logger.log("Remove " + instruction);
                            applied = true;
                            newNames.put(output, accesses.get(access));
                        }
                    } else {
                        accesses.put(access, output);
                        accessSources.put(access, instructionLocation);
                    }
                }
            }
        }

        instance.renameVariables(newNames);

        if (applied) {
            dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION // Manually invalidated
        );
    }

    private static class GetAccess {
        private final String matrix;
        private final List<String> indices;
        private final Optional<VariableType> outputType;

        private GetAccess(String matrix, List<String> indices, Optional<VariableType> outputType) {
            this.matrix = matrix;
            this.indices = indices;
            this.outputType = outputType;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GetAccess && equals((GetAccess) obj);
        }

        public boolean equals(GetAccess obj) {
            if (obj == null) {
                return false;
            }

            return matrix.equals(obj.matrix) &&
                    indices.equals(obj.indices) &&
                    outputType.equals(obj.outputType);
        }

        @Override
        public int hashCode() {
            return matrix.hashCode() ^ indices.hashCode() ^ outputType.hashCode();
        }
    }

}
