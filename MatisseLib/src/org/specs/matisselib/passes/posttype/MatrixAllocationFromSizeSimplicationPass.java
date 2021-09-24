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
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class MatrixAllocationFromSizeSimplicationPass implements PostTypeInferencePass {
    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        for (SsaBlock block : instance.getBlocks()) {
            Map<String, String> sizeMatrices = new HashMap<>();

            for (ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator(); iterator.hasNext();) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof TypedFunctionCallInstruction) {
                    TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

                    if (functionCall.getFunctionName().equals("size") && functionCall.getInputVariables().size() == 1) {
                        sizeMatrices.put(functionCall.getOutputs().get(0), functionCall.getInputVariables().get(0));
                    }
                    if (functionCall.getFunctionName().equals("matisse_new_array")
                            && functionCall.getInputVariables().size() == 1) {
                        String input = functionCall.getInputVariables().get(0);
                        String baseMatrix = sizeMatrices.get(input);
                        if (baseMatrix != null) {
                            Optional<VariableType> type = instance.getVariableType(baseMatrix);
                            if (type.isPresent()) {
                                // FIXME: is "functionCall.getFunctionType" correct?
                                if (type.get() instanceof MatrixType) {
                                    SsaInstruction newInstruction = new TypedFunctionCallInstruction(
                                            "matisse_new_array_from_matrix", functionCall.getFunctionType(),
                                            functionCall.getOutputs(), Arrays.asList(baseMatrix));
                                    iterator.set(newInstruction);
                                } else if (type.get() instanceof ScalarType) {
                                    VariableType oneType = instance.getProviderData().getNumerics().newInt(1);
                                    String one = instance.makeTemporary("one", oneType);
                                    SsaInstruction setOne = AssignmentInstruction.fromInteger(one, 1);
                                    iterator.set(setOne);

                                    SsaInstruction newInstruction = new TypedFunctionCallInstruction(
                                            "matisse_new_array_from_dims", functionCall.getFunctionType(),
                                            functionCall.getOutputs(), Arrays.asList(one, one));
                                    iterator.add(newInstruction);
                                }
                            }
                        }
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
