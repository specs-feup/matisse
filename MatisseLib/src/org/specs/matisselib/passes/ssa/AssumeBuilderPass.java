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

package org.specs.matisselib.passes.ssa;

import java.util.ListIterator;

import org.specs.matisselib.PassMessage;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.instructions.AssumeInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Converts matisse_assume function calls into assume instructions.
 * 
 * @author Luís Reis
 *
 */
public class AssumeBuilderPass implements SsaPass {

    @Override
    public void apply(FunctionBody source, DataStore data) {
        InstructionReportingService reporter = data.get(TypeInferencePass.INSTRUCTION_REPORT_SERVICE);

        for (SsaBlock block : source.getBlocks()) {
            ListIterator<SsaInstruction> it = block.getInstructions().listIterator();
            while (it.hasNext()) {
                SsaInstruction instruction = it.next();

                if (!(instruction instanceof UntypedFunctionCallInstruction)) {
                    continue;
                }

                UntypedFunctionCallInstruction functionCall = (UntypedFunctionCallInstruction) instruction;

                if (!functionCall.getFunctionName().equals("matisse_assume")) {
                    continue;
                }

                if (functionCall.getInputVariables().size() != 1) {
                    throw reporter.emitError(source, instruction, PassMessage.CORRECTNESS_ERROR,
                            "matisse_assume must take a single input.");
                }

                if (!functionCall.getOutputs().isEmpty()) {
                    throw reporter.emitError(source, instruction, PassMessage.CORRECTNESS_ERROR,
                            "matisse_assume must not have any outputs.");
                }

                String input = functionCall.getInputVariables().get(0);
                it.set(new AssumeInstruction(input));
            }
        }
    }

}
