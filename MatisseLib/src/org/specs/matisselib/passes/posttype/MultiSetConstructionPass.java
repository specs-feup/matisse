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
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.MultiSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Meant to be used as a very late stage of compilation. Serves to speed up compilation.
 * 
 * @author Luís Reis
 *
 */
public class MultiSetConstructionPass implements PostTypeInferencePass {
    public static final String PASS_NAME = "multi_set_construction";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.logSkip(instance);
            return;
        }

        logger.logStart(instance);

        UsageMap usages = UsageMap.build(instance.getFunctionBody());
        for (SsaBlock block : instance.getBlocks()) {
            Map<String, List<String>> optimizationCandidates = new HashMap<>();
            Map<String, String> sourceValues = new HashMap<>();

            for (SsaInstruction instruction : block.getInstructions()) {
                if (!(instruction instanceof SimpleSetInstruction)) {
                    continue;
                }

                SimpleSetInstruction set = (SimpleSetInstruction) instruction;
                if (set.getIndices().size() != 1) {
                    continue;
                }

                String output = set.getOutput();
                String inputMatrix = set.getInputMatrix();
                String index = set.getIndices().get(0);
                String value = set.getValue();

                sourceValues.put(output, value);

                if (!inputMatrix.endsWith("$ret")) {
                    if (optimizationCandidates.containsKey(inputMatrix)
                            && usages.getUsageCount(inputMatrix) == 1) {
                        List<String> candidates = optimizationCandidates.get(inputMatrix);
                        if (ConstantUtils.isEqualToValue(instance.getVariableType(index).get(),
                                candidates.size() + 1)) {
                            candidates.add(inputMatrix);
                            optimizationCandidates.remove(inputMatrix);
                            optimizationCandidates.put(output, candidates);
                            continue;
                        }
                    }
                    if (ConstantUtils.isConstantOne(instance, index)) {
                        List<String> candidates = new ArrayList<>();
                        candidates.add(inputMatrix);
                        optimizationCandidates.put(output, candidates);
                    }
                }
            }

            Set<String> outputsToRemove = new HashSet<>();
            for (String key : new HashSet<>(optimizationCandidates.keySet())) {
                List<String> value = optimizationCandidates.get(key);
                if (value.size() == 1) {
                    // Since multi_set with a single index is equivalent to simple_set
                    // we'll ignore this case
                    optimizationCandidates.remove(key);
                    continue;
                }

                outputsToRemove.addAll(value);
            }
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();
                if (!(instruction instanceof SimpleSetInstruction)) {
                    continue;
                }
                SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                String output = set.getOutput();
                if (outputsToRemove.contains(output)) {
                    iterator.remove();
                    continue;
                }

                if (optimizationCandidates.containsKey(output)) {
                    List<String> candidates = optimizationCandidates.get(output);
                    String firstSource = candidates.get(0);

                    List<String> indices = new ArrayList<>();
                    List<String> values = new ArrayList<>();
                    for (int i = 1; i < candidates.size(); ++i) {
                        String candidate = candidates.get(i);
                        values.add(sourceValues.get(candidate));
                    }

                    values.add(sourceValues.get(output));

                    iterator.set(new MultiSetInstruction(output, firstSource, values));
                }

            }
        }
    }
}
