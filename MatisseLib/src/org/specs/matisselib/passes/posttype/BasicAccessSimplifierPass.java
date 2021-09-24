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
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Simplifies matrix_sets into simple_sets, and matrix gets into simple_gets (if possible).
 * 
 * @author Luís Reis
 *
 */
public class BasicAccessSimplifierPass extends TypeTransparentSsaPass {
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

        for (SsaBlock block : body.getBlocks()) {
            ListIterator<SsaInstruction> iter = block.getInstructions().listIterator();
            while (iter.hasNext()) {
                SsaInstruction instruction = iter.next();

                if (instruction instanceof MatrixGetInstruction) {
                    MatrixGetInstruction get = (MatrixGetInstruction) instruction;

                    String inputMatrix = get.getInputMatrix();
                    List<String> indices = get.getIndices();
                    if (sizes.inRangeOfMatrix(indices, inputMatrix)) {
                        iter.set(new SimpleGetInstruction(get.getOutput(), inputMatrix, indices));
                    } else {
                        System.out.println(get);
                        System.out.println("Not in range of matrix");
                    }
                } else if (instruction instanceof MatrixSetInstruction) {
                    MatrixSetInstruction set = (MatrixSetInstruction) instruction;

                    String inputMatrix = set.getInputMatrix();
                    List<String> indices = set.getIndices();
                    if (sizes.inRangeOfMatrix(indices, inputMatrix)) {
                        iter.set(new SimpleSetInstruction(set.getOutput(), inputMatrix, indices, set.getValue()));
                    }
                }
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
