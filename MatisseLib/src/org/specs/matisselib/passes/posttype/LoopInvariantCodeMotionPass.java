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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Moves code out of loops, when possible. The code must not have side-effects, must not depend on the current iteration
 * and must not depend on phi nodes. Additionally, if a variable X depends on another variable Y, then X can only be
 * moved out of the loop if Y is.
 * 
 * <p>
 * Because this pass was designed solely to assist Loop Interchange, there are some cases where the optimization is
 * valid but disabled. Notably, when we suspect that enabling it could cause substantial extraneous copies in the
 * resulting C code. This is the case for matrix variables so we only extract scalars for now. In the future, if needed,
 * we could try to perform a more sophisticated analysis to determine whether to extract something.
 * 
 * @author Luís Reis
 *
 */
public class LoopInvariantCodeMotionPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "loop_invariant_code_motion";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        if (PassUtils.skipPass(instance, PASS_NAME)) {
            return;
        }

        boolean appliedAnyChange = false;
        while (tryApply(instance, passData))
            appliedAnyChange = true;

        if (appliedAnyChange) {
            passData.get(ProjectPassServices.DATA_PROVIDER).invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    private boolean tryApply(TypedInstance instance, DataStore passData) {
        boolean appliedChange = false;

        SizeGroupInformation info = passData.get(ProjectPassServices.DATA_PROVIDER)
                .buildData(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        List<SsaBlock> blocks = instance.getBlocks();
        for (int outerBlockId = 0; outerBlockId < blocks.size(); outerBlockId++) {
            SsaBlock block = blocks.get(outerBlockId);
            Optional<ForInstruction> maybeFor = block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);

            if (!maybeFor.isPresent()) {
                continue;
            }

            logger.log("Extracting variables from loop starting at #" + outerBlockId);

            ForInstruction xfor = maybeFor.get();

            int loopBlockId = xfor.getLoopBlock();
            SsaBlock loopBlock = instance.getBlock(loopBlockId);

            Set<String> invariantVariables = new HashSet<>();
            List<SsaInstruction> extractions = new ArrayList<>();
            Set<String> declaredInLoop = BlockUtils.getVariablesDeclaredInContainedBlocks(instance.getFunctionBody(),
                    loopBlockId);
            Map<String, String> sameSize = new HashMap<>();

            for (SsaInstruction instruction : loopBlock.getInstructions()) {
                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    if (phi.getSourceBlocks().size() != 2) {
                        continue;
                    }

                    String output = phi.getOutput();
                    String sourceValue = phi.getInputVariables().get(phi.getSourceBlocks().indexOf(outerBlockId));

                    if (info.areSameSize(output, sourceValue)) {
                        logger.log("Same size: " + output + ", " + sourceValue);
                        sameSize.put(output, sourceValue);
                    }
                    continue;
                }

                if (isSafeSizeInstruction(instruction, declaredInLoop, invariantVariables, sameSize)) {
                    logger.log("Smart extraction: " + instruction);

                    invariantVariables.addAll(instruction.getOutputs());
                    instruction.renameVariables(sameSize);
                    extractions.add(instruction);

                    continue;
                }

                if (instruction.getOutputs()
                        .stream()
                        .map(output -> instance.getVariableType(output))
                        .anyMatch(MatrixUtils::isMatrix)) {
                    logger.log("Not extracting " + instruction + " because output is a matrix.");
                    continue;
                }

                boolean dependenciesAreSafe = instruction.getInputVariables()
                        .stream()
                        .filter(declaredInLoop::contains)
                        .allMatch(invariantVariables::contains);
                if (!dependenciesAreSafe) {
                    logger.log("Can't extract " + instruction);
                    continue;
                }

                if (isInstructionSafe(instruction)) {
                    invariantVariables.addAll(instruction.getOutputs());
                    extractions.add(instruction);
                } else {
                    logger.log("Could not extract: " + instruction);
                }
            }

            if (invariantVariables.isEmpty()) {
                logger.log("Nothing to extract");
            } else {
                logger.log("Extracting " + invariantVariables);
                block.insertInstructions(block.getInstructions().size() - 1, extractions);
                loopBlock.removeInstructions(extractions);
                appliedChange = true;
            }
        }

        return appliedChange;
    }

    private static final List<String> SIZE_FUNCTIONS = Arrays.asList("numel", "length", "size", "ndims");

    private boolean isSafeSizeInstruction(SsaInstruction instruction,
            Set<String> declaredInLoop,
            Set<String> invariantVariables,
            Map<String, String> sameSize) {

        if (instruction instanceof EndInstruction) {
            String matrix = ((EndInstruction) instruction).getInputVariable();

            return sameSize.containsKey(matrix);
        }

        if (!(instruction instanceof FunctionCallInstruction)) {
            return false;
        }

        FunctionCallInstruction call = (FunctionCallInstruction) instruction;
        if (!SIZE_FUNCTIONS.contains(call.getFunctionName())) {
            return false;
        }

        if (call.getInputVariables().size() == 0) {
            return false;
        }

        String matrix = call.getInputVariables().get(0);
        if (!sameSize.containsKey(matrix)) {
            return false;
        }

        for (int i = 1; i < call.getInputVariables().size(); ++i) {
            String input = call.getInputVariables().get(i);

            if (declaredInLoop.contains(input) && !invariantVariables.contains(input)) {
                return false;
            }
        }

        return true;
    }

    private boolean isInstructionSafe(SsaInstruction instruction) {
        if (instruction instanceof IterInstruction) {
            return false;
        }
        if (instruction instanceof PhiInstruction) {
            return false;
        }
        if (instruction.dependsOnGlobalState()) {
            return false;
        }
        if (instruction.getInstructionType() == InstructionType.CONTROL_FLOW) {
            return false;
        }
        if (instruction.getInstructionType() == InstructionType.HAS_VALIDATION_SIDE_EFFECT
                || instruction.getInstructionType() == InstructionType.HAS_SIDE_EFFECT) {
            return false;
        }

        return true;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

}
