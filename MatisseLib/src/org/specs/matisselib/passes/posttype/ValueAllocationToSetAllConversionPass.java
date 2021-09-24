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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SetAllInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ValueAllocationToSetAllConversionPass implements PostTypeInferencePass {

    private static final Map<String, Integer> VALUES = new HashMap<>();
    static {
        VALUES.put("zeros", 0);
        VALUES.put("ones", 1);
    }

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        if (PassUtils.skipPass(instance, "value_allocation_to_set_all_conversion")) {
            return;
        }

        for (SsaBlock block : instance.getBlocks()) {
            for (ListIterator<SsaInstruction> it = block.getInstructions().listIterator(); it.hasNext();) {
                SsaInstruction instruction = it.next();

                if (instruction instanceof TypedFunctionCallInstruction) {
                    TypedFunctionCallInstruction typedCall = (TypedFunctionCallInstruction) instruction;

                    String functionName = typedCall.getFunctionName();
                    if (!VALUES.keySet().contains(functionName)) {
                        continue;
                    }

                    if (typedCall.getOutputs().size() != 1) {
                        continue;
                    }
                    if (typedCall.getInputVariables().size() == 0) {
                        continue;
                    }

                    String output = typedCall.getOutputs().get(0);

                    String temp = instance.makeTemporary(NameUtils.getSuggestedName(output),
                            instance.getVariableType(output));

                    boolean isMatrixArgument = MatrixUtils
                            .isMatrix(instance.getVariableType(typedCall.getInputVariables().get(0)));
                    String newFunctionName = isMatrixArgument
                            ? "matisse_new_array" : "matisse_new_array_from_dims";

                    List<String> inputs = typedCall.getInputVariables();
                    if (!isMatrixArgument && inputs.size() == 1) {
                        String input = inputs.get(0);
                        inputs = Arrays.asList(input, input);
                    }

                    TypedFunctionCallInstruction newAlloc = new TypedFunctionCallInstruction(newFunctionName,
                            typedCall.getFunctionType(),
                            Arrays.asList(temp),
                            inputs);
                    it.set(newAlloc);

                    int constant = VALUES.get(functionName);
                    VariableType valueType = instance.getProviderData().getNumerics().newInt(constant);
                    String value = instance.makeTemporary("value", valueType);
                    it.add(AssignmentInstruction.fromInteger(value, constant));
                    it.add(new SetAllInstruction(output, temp, value));
                }
            }
        }
    }

}
