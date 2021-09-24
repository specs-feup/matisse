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

package org.specs.matisselib.typeinference.rules;

import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.MakeEmptyMatrixInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class MakeEmptyMatrixRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof MakeEmptyMatrixInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        MakeEmptyMatrixInstruction make = (MakeEmptyMatrixInstruction) instruction;

        String output = make.getOutputs().get(0);

        VariableType defaultRealType = context.getProviderData().getSettings().get(CirKeys.DEFAULT_REAL);

        InstructionReportingService reportService = context.getPassData().get(
                TypeInferencePass.INSTRUCTION_REPORT_SERVICE);

        VariableType type;
        Optional<VariableType> candidateType = context.getDefaultVariableType(output);
        if (candidateType.isPresent()) {
            type = candidateType.get();

            if (!(type instanceof MatrixType)) {
                reportService.emitMessage(context.getInstance(), instruction, PassMessage.SUSPICIOUS_CASE,
                        "Empty matrix variable has type overriden in aspects to non-matrix type.");
            } else {
                TypeShape shape = type.getTypeShape();
                if (shape.isFullyDefined() && !shape.isKnownEmpty()) {
                    reportService
                            .emitMessage(context.getInstance(), instruction, PassMessage.ASPECT_ERROR,
                                    "Variable has type overriden in aspects to non-empty matrix, but it is assigned an empty matrix.");
                }
            }
        } else {
            type = DynamicMatrixType.newInstance(defaultRealType, TypeShape.newInstance(0, 0));
        }
        context.addVariable(output, type);
    }

}
