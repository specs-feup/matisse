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

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class FixedAccessPropagationPass extends TypeNeutralSsaPass {
    private static final Map<String, Integer> VALUES = new HashMap<>();

    static {
        VALUES.put("zeros", 0);
        VALUES.put("ones", 1);
    }

    @Override
    public void apply(FunctionBody source, DataStore data) {
        Optional<DataProviderService> dataProvider = data.getTry(ProjectPassServices.DATA_PROVIDER);

        Map<String, Integer> fixedVariables = source
                .getFlattenedInstructionsOfTypeStream(FunctionCallInstruction.class)
                .filter(call -> VALUES.containsKey(call.getFunctionName()))
                .collect(Collectors.toMap(
                        call -> call.getOutputs().get(0),
                        call -> VALUES.get(call.getFunctionName())));

        for (SsaBlock block : source.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();

            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();
                if (instruction instanceof SimpleGetInstruction) {
                    SimpleGetInstruction get = (SimpleGetInstruction) instruction;

                    Integer result = fixedVariables.get(get.getInputMatrix());
                    if (result != null) {
                        dataProvider.ifPresent(dP -> dP.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION));

                        String output = get.getOutput();
                        SsaInstruction newInstruction = AssignmentInstruction.fromInteger(output, result);
                        iterator.set(newInstruction);
                    }
                }
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
