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

package org.specs.matisselib.passes.posttype;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public abstract class SizeAwareInstructionRemovalPass<T extends SsaInstruction> extends TypeTransparentSsaPass {
    private final Class<T> clazz;

    protected SizeAwareInstructionRemovalPass(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * This function is public so that unit tests can call it directly.
     */
    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        SizeGroupInformation sizes = PassUtils.getData(passData, CompilerDataProviders.SIZE_GROUP_INFORMATION);

        /*
         * We'll keep trying to remove the instructions until none are left
         */
        boolean performedElimination = false;
        while (tryRemoveInstruction(body, providerData, typeGetter, makeTemporary, sizes, passData)) {
            performedElimination = true;
        }

        afterPass(body, passData, performedElimination);
    }

    private boolean tryRemoveInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SizeGroupInformation sizes,
            DataStore passData) {

        List<SsaBlock> blocks = body.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);

                if (this.clazz.isInstance(instruction)) {
                    T castInstruction = this.clazz.cast(instruction);
                    if (canEliminate(body, castInstruction, typeGetter, sizes)) {

                        removeInstruction(body,
                                providerData,
                                typeGetter,
                                makeTemporary,
                                block,
                                blockId,
                                instructionId,
                                castInstruction,
                                sizes,
                                passData);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected boolean canEliminate(FunctionBody body,
            T instruction,
            Function<String, Optional<VariableType>> typeGetter,
            SizeGroupInformation sizes) {

        return true;
    }

    protected abstract void removeInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            T instruction,
            SizeGroupInformation sizes,
            DataStore passData);

    protected void afterPass(FunctionBody body, DataStore dataStore, boolean performedElimination) {

    }
}
