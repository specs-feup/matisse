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

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class OptimizationOpportunityReporterPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        InstructionReportingService reporter = passData.get(TypeInferencePass.INSTRUCTION_REPORT_SERVICE);

        for (SsaInstruction instruction : instance.getFunctionBody().getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;
                List<String> arguments = functionCall.getInputVariables();

                if (functionCall.getFunctionName().equals("sum") && arguments.size() == 1) {
                    String argument = arguments.get(0);

                    VariableType variableType = instance.getVariableType(argument).get();
                    if (variableType instanceof MatrixType) {
                        TypeShape shape = ((MatrixType) variableType).getTypeShape();

                        if (!shape.isKnownMultiDimensional()) {
                            reporter.emitMessage(instance,
                                    instruction,
                                    PassMessage.OPTIMIZATION_OPPORTUNITY,
                                    "Could not inline call to sum. If sum is 1D, then consider making that explicit to improve performance.");
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return true;
    }
}
