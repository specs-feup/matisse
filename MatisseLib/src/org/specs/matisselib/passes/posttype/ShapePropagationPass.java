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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Consider <code>X = [a b]'</code>. The generated code without this pass will be (pseudo-code):
 * 
 * <pre>
 * X$1 = alloc
 * X$2 = simple_set X$1, 1, a
 * X$3 = simple_set X$2, 2, b
 * X$4 = call transpose X$3
 * </pre>
 * 
 * If {@link RedundantTransposeEliminationPass} is applied to <code>X$4</code>, then the shapes of X$2 and X$3 will be
 * different. As such, there will be a copy inserted there.
 * 
 * This is undesirable, so this pass identifies cases where a matrix is unnecessarily copied due to a shape mismatch,
 * and fixes the shape.
 * 
 * @author Luís Reis
 *
 */
public class ShapePropagationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);

        UsageMap usages = UsageMap.build(instance.getFunctionBody());

        Map<String, String> candidateMatrices = new HashMap<>();
        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof SimpleSetInstruction) {
                SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                String input = set.getInputMatrix();
                String output = set.getOutput();

                if (usages.getUsageCount(input) != 1) {
                    // Variable is used elsewhere
                    continue;
                }

                MatrixType inputType = instance.getVariableType(input)
                        .filter(MatrixType.class::isInstance)
                        .map(MatrixType.class::cast)
                        .orElse(null);
                MatrixType outputType = instance.getVariableType(output)
                        .filter(MatrixType.class::isInstance)
                        .map(MatrixType.class::cast)
                        .orElse(null);

                if (inputType == null || outputType == null) {
                    continue;
                }

                TypeShape inputShape = inputType.getTypeShape();
                TypeShape outputShape = outputType.getTypeShape();

                if (!inputShape.isNumElementsKnown() || !outputShape.isNumElementsKnown()) {
                    continue;
                }

                if (inputShape.getNumElements() != outputShape.getNumElements()) {
                    continue;
                }

                candidateMatrices.put(input, output);
            }
        }

        Set<String> visited = new HashSet<>();
        for (String output : candidateMatrices.keySet()) {
            List<String> matricesToModify = new ArrayList<>();
            matricesToModify.add(output);

            for (;;) {
                String input = candidateMatrices.get(SpecsCollections.last(matricesToModify));
                if (input == null) {
                    break;
                }

                matricesToModify.add(input);

                if (visited.contains(input)) {
                    break;
                }
            }

            visited.addAll(matricesToModify);

            TypeShape lastShape = MatrixUtils
                    .getShape(instance.getVariableType(SpecsCollections.last(matricesToModify)).get());

            for (String matrixToModify : matricesToModify) {
                MatrixType originalType = (MatrixType) instance.getVariableType(matrixToModify).get();
                instance.addOrOverwriteVariable(matrixToModify, originalType.matrix().setShape(lastShape));
            }
        }

        if (candidateMatrices.size() != 0) {
            dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
