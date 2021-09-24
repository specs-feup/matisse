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
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * In functions with %!by_ref arguments, it is possible for the initial and the final variables to have colliding
 * lifetimes.
 * 
 * <p>
 * We solve this problem by duplicating reference arguments. If the lifetimes did not intercept, then no harm done --
 * the assignment will be eliminated by the final variable allocator. If they do, this fixes it by allowing the final
 * variable allocator to safely assign var$1 and var$ret to the same group.
 * 
 * @author Luís Reis
 *
 */
public class ReferenceArgumentDuplicationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        // Can not be disabled or skipped.

        // All arg instructions are in the initial block
        SsaBlock entryBlock = instance.getBlock(0);
        ListIterator<SsaInstruction> iterator = entryBlock
                .getInstructions()
                .listIterator();
        while (iterator.hasNext()) {
            SsaInstruction instruction = iterator.next();

            if (!(instruction instanceof ArgumentInstruction)) {
                continue;
            }

            ArgumentInstruction arg = (ArgumentInstruction) instruction;
            int argIndex = arg.getArgumentIndex();
            if (instance.getFunctionType().isInputReference(argIndex)) {
                // Applying transformation
                String baseOutput = arg.getOutput();

                // The argument instruction refers to the new variable
                // but the rest of the function still references the old one.
                Optional<VariableType> argType = instance.getVariableType(baseOutput);
                String newOutput = instance.makeTemporary(NameUtils.getSuggestedName(baseOutput), argType);
                Map<String, String> newNames = new HashMap<>();
                newNames.put(baseOutput, newOutput);
                arg.renameVariables(newNames);

                iterator.add(AssignmentInstruction.fromVariable(baseOutput, newOutput));
            }
        }
    }

}
