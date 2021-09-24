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

import java.util.ListIterator;

import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateBooleanInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ScalarValidateBooleanRemovalPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        for (SsaBlock block : instance.getBlocks()) {
            ListIterator<SsaInstruction> it = block.getInstructions().listIterator();

            while (it.hasNext()) {
                SsaInstruction instruction = it.next();

                if (instruction instanceof ValidateBooleanInstruction) {
                    ValidateBooleanInstruction vb = (ValidateBooleanInstruction) instruction;

                    String var = vb.getInputVariable();
                    boolean isScalar = instance.getVariableType(var)
                            .map(ScalarUtils::isScalar)
                            .orElse(false);

                    // Scalars are always valid booleans
                    if (isScalar) {
                        it.remove();
                    }
                }
            }
        }
    }

}
