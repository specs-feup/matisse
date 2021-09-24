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

package org.specs.matisselib.helpers.sizeinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class ForInstructionBuilder implements InstructionInformationBuilder {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof ForInstruction;
    }

    @SuppressWarnings("resource")
    @Override
    public SizeGroupInformation apply(SizeInfoBuilderContext ctx, SsaInstruction instruction) {
        ForInstruction xfor = (ForInstruction) instruction;

        SizeGroupInformation info = ctx.getCurrentInfo();
        FunctionBody body = ctx.getFunctionBody();

        String start = xfor.getStart();
        String interval = xfor.getInterval();
        String end = xfor.getEnd();

        String intervalConstant = ctx.getVariableType(interval)
                .filter(ScalarUtils::hasConstant)
                .map(ScalarUtils::getConstantString)
                .orElse(null);
        boolean isIntervalKnownNonNegative = intervalConstant != null && !intervalConstant.contains("-");

        if (ScalarUtils.isScalar(ctx.getVariableType(start))
                && ScalarUtils.isScalar(ctx.getVariableType(interval))) {

            if (isTrivial(ctx, xfor)) {
                info = buildInfoForLoop(ctx, xfor.getLoopBlock(), end, false);
                return ctx.buildInfoFor(xfor.getEndBlock(), isIntervalKnownNonNegative ? end : null, info);
            }

            SizeGroupInformation newInfo = buildInfoForLoop(ctx,
                    xfor.getLoopBlock(),
                    end,
                    true);
            if (newInfo != null) {
                info.close();
                // Speculation turned out to be correct.
                return ctx.buildInfoFor(xfor.getEndBlock(), isIntervalKnownNonNegative ? end : null, newInfo);
            }

            ctx.log("(!) Speculation failure (" + body.getName() + ")");
        } else {
            ctx.log("Non-standard for loop. Not attempting speculation: " + xfor);
        }

        info = ctx.buildInfoFor(xfor.getLoopBlock(), end, info);
        return ctx.buildInfoFor(xfor.getEndBlock(), null, info);
    }

    static class Precondition {
        String debugInfo;
        Predicate<SizeGroupInformation> predicate;

        public Precondition(String debugInfo,
                Predicate<SizeGroupInformation> predicate) {

            this.debugInfo = debugInfo;
            this.predicate = predicate;
        }
    }

    private static boolean isTrivial(SizeInfoBuilderContext ctx, ForInstruction xfor) {
        int loopBlockId = xfor.getLoopBlock();
        SsaBlock loopBlock = ctx.getFunctionBody().getBlock(loopBlockId);

        if (loopBlock.getEndingInstruction().isPresent()) {
            return false;
        }

        Map<String, String> constructions = new HashMap<>();
        Set<String> validSets = new HashSet<>();

        for (SsaInstruction instruction : loopBlock.getInstructions()) {
            if (instruction.getOutputs().size() == 0) {
                continue;
            }

            if (instruction instanceof IterInstruction) {
                continue;
            }

            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                if (phi.getInputVariables().size() != 2) {
                    return false;
                }

                String output = phi.getOutput();
                int endIndex = phi.getSourceBlocks().indexOf(loopBlockId);
                if (endIndex < 0) {
                    // Is this possible?
                    return false;
                }
                String result = phi.getInputVariables().get(endIndex);

                constructions.put(output, result);
                continue;
            }

            if (instruction instanceof SimpleSetInstruction) {
                SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                String expectedResult = constructions.get(set.getInputMatrix());
                if (set.getOutput().equals(expectedResult)) {
                    validSets.add(set.getInputMatrix());
                }
                continue;
            }
        }

        return constructions.keySet().stream().allMatch(validSets::contains);
    }

    @SuppressWarnings("resource")
    private static SizeGroupInformation buildInfoForLoop(SizeInfoBuilderContext ctx,
            int loopBlockId,
            String end,
            boolean speculative) {

        FunctionBody body = ctx.getFunctionBody();
        int parentBlockId = ctx.getBlockId();
        SizeGroupInformation info = speculative ? new SizeGroupInformation(ctx.getCurrentInfo()) : ctx.getCurrentInfo();

        List<Precondition> preconditions = new ArrayList<>();

        SsaBlock block = body.getBlock(loopBlockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                String out = phi.getOutput();
                int parentIndex = phi.getSourceBlocks().indexOf(parentBlockId);
                assert parentIndex != -1 : "Phi node does not reference parent block " + parentBlockId + ", in " + phi
                        + ":\n" + body;
                if (phi.getSourceBlocks().size() == 2) {
                    String parentVar = phi.getInputVariables().get(parentIndex);

                    Optional<VariableType> parentType = ctx.getVariableType(parentVar);
                    if (!parentType.isPresent()) {
                        ctx.log("Could not find type of " + parentVar);
                    } else if (parentType.get() instanceof MatrixType) {
                        int otherIndex = parentIndex == 0 ? 1 : 0;
                        String otherVar = phi.getInputVariables().get(otherIndex);

                        preconditions
                                .add(new Precondition(phi.toString(), xinfo -> xinfo.areSameSize(parentVar, otherVar)));

                        ctx.log("Assuming " + out + " and " + parentVar + " have same sizes.");
                        info.buildMatrixWithSameSize(out, parentVar);
                    }

                    continue;
                }
            }
            info = ctx.handleInstruction(ctx, info, loopBlockId, end, instruction);
        }

        if (speculative) {
            if (!validatePreconditions(ctx, info, preconditions)) {
                info.close();
                return null;
            }
        } else {
            assert validatePreconditions(ctx, info, preconditions);
        }

        return info;
    }

    private static boolean validatePreconditions(SizeInfoBuilderContext ctx,
            SizeGroupInformation sizes,
            List<Precondition> validationCheck) {

        for (Precondition precondition : validationCheck) {
            if (!precondition.predicate.test(sizes)) {
                ctx.log("Speculation failed due to incorrect assumption: " + precondition.debugInfo);
                return false;
            }
        }

        ctx.log("Speculation successful");
        return true;
    }

}
