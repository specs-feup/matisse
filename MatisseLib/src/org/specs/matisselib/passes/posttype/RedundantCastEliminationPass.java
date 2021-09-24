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

package org.specs.matisselib.passes.posttype;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class RedundantCastEliminationPass extends InstructionRemovalPass<TypedFunctionCallInstruction> {
    private final List<String> CAST_FUNCTIONS = Arrays.asList("int8", "uint8",
            "int16", "uint16",
            "int32", "uint32",
            "int64", "uint64",
            "single", "double",
            "char", "logical");

    public RedundantCastEliminationPass() {
        super(TypedFunctionCallInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, TypedFunctionCallInstruction instruction) {

        if (instruction.getOutputs().size() != 1) {
            return false;
        }

        if (instruction.getInputVariables().size() != 1) {
            return false;
        }

        if (!this.CAST_FUNCTIONS.contains(instruction.getFunctionName())) {
            return false;
        }

        FunctionType type = instruction.getFunctionType();
        if (!type.getArgumentsTypes().get(0).equals(type.getOutputTypes().get(0))) {
            // Not redundant
            return false;
        }

        return true;
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block, int blockId, int instructionId, TypedFunctionCallInstruction instruction,
            DataStore passData) {

        String input = instruction.getInputVariables().get(0);
        String output = instruction.getOutputs().get(0);

        block.removeInstructionAt(instructionId);

        Map<String, String> newNames = new HashMap<>();
        newNames.put(output, input);
        instance.renameVariables(newNames);
    }
}
