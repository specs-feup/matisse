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
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class RedundantTransposeEliminationPass implements PostTypeInferencePass {

    private static final boolean ENABLE_LOG = false;

    private static final Set<String> transposeFunctions = new HashSet<>();
    static {
        transposeFunctions.add("transpose");
        transposeFunctions.add("ctranspose");
    }

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        log("Starting");

        Map<String, String> transpose1dVariables = new HashMap<>();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction
                    && transposeFunctions.contains(((FunctionCallInstruction) instruction).getFunctionName())
                    && instruction.getInputVariables().size() == 1
                    && instruction.getOutputs().size() == 1) {

                String input = instruction.getInputVariables().get(0);
                String output = instruction.getOutputs().get(0);

                if (output.endsWith("$ret")) {
                    // Returned matrices are never redundant.
                    // Transposition should be done because we don't know how the caller will use the result value.
                    log("Excluding " + output + " because it is a returned value.");
                    continue;
                }

                boolean is1d = instance.getVariableType(input)
                        .filter(type -> MatrixUtils.isMatrix(type) && MatrixUtils.getShape(type).isKnown1D())
                        .isPresent();

                if (is1d) {
                    transpose1dVariables.put(output, input);
                } else {
                    log("Not removing " + output + " because matrix is not 1D.");
                }
            }
        }

        // Verify if all uses are "approved".
        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            boolean usesRelevantVariable = instruction.getInputVariables()
                    .stream()
                    .anyMatch(input -> transpose1dVariables.containsKey(input));
            if (!usesRelevantVariable) {
                continue;
            }

            if (instruction instanceof SimpleGetInstruction
                    || instruction instanceof GetOrFirstInstruction
                    || instruction instanceof MatrixGetInstruction) {

                IndexedInstruction indexedInstruction = (IndexedInstruction) instruction;
                for (String index : indexedInstruction.getIndices()) {
                    if (transpose1dVariables.remove(index) != null) {
                        log("Removed " + index + ", because it is an index of " + instruction);
                    }
                }

                List<String> indices = indexedInstruction.getIndices();
                if (indices.size() != 1
                        || !ScalarUtils.isScalar(instance.getVariableType(indices.get(0)))) {

                    String input = indexedInstruction.getInputMatrix();
                    if (transpose1dVariables.remove(input) != null) {
                        log("Removing " + input + " due to " + instruction + "\nIndices: "
                                + indices + "\nIndex type: " + instance.getVariableType(indices.get(0)));
                    }
                }
            } else {
                for (String input : instruction.getInputVariables()) {
                    if (transpose1dVariables.remove(input) != null) {
                        log("Removed " + input + " due to " + instruction);
                    }
                }
            }
        }

        log("Removing " + transpose1dVariables);

        // What we have now are approved matrices. First, delete the allocations
        for (SsaBlock block : instance.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction.getOutputs().size() != 1) {
                    continue;
                }

                String output = instruction.getOutputs().get(0);
                if (transpose1dVariables.containsKey(output)) {
                    iterator.remove();
                }
            }
        }

        // Now, perform the renames
        instance.renameVariables(transpose1dVariables);
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[redundant_transpose_elimination] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH);
    }
}
