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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.Logical.LogicalType;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class LogicalMatrixAccessPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        SystemFunctionProviderService systemFunctionProvider = passData
                .get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        InstanceProvider findInstance = systemFunctionProvider.getSystemFunction("find").get();

        for (SsaBlock block : instance.getBlocks()) {
            for (ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator(); iterator.hasNext();) {
                SsaInstruction instruction = iterator.next();
                if (instruction instanceof MatrixGetInstruction) {

                    MatrixGetInstruction get = (MatrixGetInstruction) instruction;
                    iterator.remove();

                    List<String> newIndices = new ArrayList<>();
                    for (int indexId = 0; indexId < get.getIndices().size(); ++indexId) {
                        String index = get.getIndices().get(indexId);

                        newIndices.add(
                                buildReplacementIndex(instance, findInstance, iterator, index));
                    }

                    iterator.add(new MatrixGetInstruction(get.getOutput(), get.getInputMatrix(), newIndices));
                } else if (instruction instanceof MatrixSetInstruction) {

                    MatrixSetInstruction set = (MatrixSetInstruction) instruction;
                    iterator.remove();

                    List<String> newIndices = new ArrayList<>();
                    for (int indexId = 0; indexId < set.getIndices().size(); ++indexId) {
                        String index = set.getIndices().get(indexId);

                        newIndices.add(
                                buildReplacementIndex(instance, findInstance, iterator, index));
                    }

                    iterator.add(new MatrixSetInstruction(set.getOutput(), set.getInputMatrix(), newIndices,
                            set.getValue()));
                }
            }
        }
    }

    private String buildReplacementIndex(TypedInstance instance,
            InstanceProvider findInstance,
            ListIterator<SsaInstruction> iterator,
            String index) {

        Optional<VariableType> indexType = instance.getVariableType(index);
        if (indexType
                .filter(MatrixType.class::isInstance)
                .map(MatrixType.class::cast)
                .filter(mt -> mt.matrix().getElementType() instanceof LogicalType)
                .isPresent()) {

            ProviderData findData = instance.getProviderData().create(indexType.get());
            FunctionType functionType = findInstance.getType(findData);

            String replacementIndex = instance.makeTemporary("indices",
                    functionType.getOutputTypes().get(0));

            iterator.add(new TypedFunctionCallInstruction("find", functionType,
                    Arrays.asList(replacementIndex), Arrays.asList(index)));
            return replacementIndex;
        }

        return index;
    }

}
