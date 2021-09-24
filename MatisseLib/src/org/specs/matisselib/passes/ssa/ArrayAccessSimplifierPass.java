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

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * This pass identifies cases where a simple_get is performed after a simple_set and to that matrix and with the same
 * index. When this happens, the simple_get is replaced by a simple assignment to the stored value.
 * 
 * @author Luís Reis
 *
 */
public class ArrayAccessSimplifierPass extends TypeNeutralSsaPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(FunctionBody source, DataStore data) {
        if (PassUtils.skipPass(source, "array_access_simplifier")) {
            return;
        }

        Optional<DataProviderService> dataProvider = data.getTry(ProjectPassServices.DATA_PROVIDER);

        for (int blockId = 0; blockId < source.getBlocks().size(); ++blockId) {
            SsaBlock block = source.getBlock(blockId);

            Map<String, SimpleSetInstruction> setsSoFar = new HashMap<>();

            ListIterator<SsaInstruction> instructionIterator = block.getInstructions().listIterator();
            while (instructionIterator.hasNext()) {
                SsaInstruction instruction = instructionIterator.next();

                if (instruction instanceof SimpleSetInstruction) {
                    SimpleSetInstruction set = (SimpleSetInstruction) instruction;
                    setsSoFar.put(set.getOutput(), set);
                    continue;
                }

                if (instruction instanceof SimpleGetInstruction) {
                    SimpleGetInstruction get = (SimpleGetInstruction) instruction;
                    String matrix = get.getInputMatrix();
                    String getOutput = get.getOutput();

                    if (setsSoFar.containsKey(matrix)) {
                        SimpleSetInstruction set = setsSoFar.get(matrix);
                        if (get.getIndices().equals(set.getIndices())) {
                            // Found a match
                            log("Removing simple_get: " + instruction);

                            dataProvider.ifPresent(dp -> dp.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION));
                            instructionIterator.set(AssignmentInstruction.fromVariable(getOutput, set.getValue()));
                        }
                    }

                    continue;
                }
            }
        }
    }

    private static void log(String message) {
        if (ArrayAccessSimplifierPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[access_simplifier] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION // Manually invalidated
        );
    }
}
