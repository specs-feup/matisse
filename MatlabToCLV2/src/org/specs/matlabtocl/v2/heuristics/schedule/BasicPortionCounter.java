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

package org.specs.matlabtocl.v2.heuristics.schedule;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.loopproperties.EstimatedIterationsProperty;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;

import pt.up.fe.specs.util.SpecsCollections;

public abstract class BasicPortionCounter<T> implements ScheduleRule {

    protected void countBody(SchedulePredictorContext context, T info) {

        int outerBlockId = SpecsCollections.last(context.parallelLoop.loopDeclarationBlockIds);
        ForInstruction xfor = (ForInstruction) context.body.getBlock(outerBlockId)
                .getEndingInstruction()
                .map(ForInstruction.class::cast)
                .get();

        countInstructions(context, xfor.getLoopBlock(), 1.0, info);
    }

    private void countInstructions(SchedulePredictorContext context, int blockId, double factor, T info) {
        SsaBlock block = context.body.getBlock(blockId);
        List<SsaInstruction> instructions = block.getInstructions();
        for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
            SsaInstruction instruction = instructions.get(instructionId);
            InstructionLocation instructionLocation = new InstructionLocation(blockId, instructionId);
            if (!countInstruction(context, instructionLocation, instruction, factor, info)) {
                if (instruction instanceof TypedFunctionCallInstruction) {
                    TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

                    // FIXME
                    ProviderData providerData = context.providerData
                            .create(functionCall.getFunctionType().getArgumentsTypes());

                    context.wideScope
                            .getUserFunction(functionCall.getFunctionName())
                            .map(id -> context.typedInstanceProvider.getTypedInstance(id, providerData))
                            .ifPresent(instance -> {
                                SchedulePredictorContext subContext = context.copy();
                                subContext.body = instance.getFunctionBody();
                                subContext.typeGetter = instance::getVariableType;
                                subContext.providerData = providerData;

                                countInstructions(subContext, 0, factor, info);
                            });
                } else if (instruction instanceof ForInstruction) {
                    ForInstruction xfor = (ForInstruction) instruction;

                    int iters = 100;
                    boolean knownIters = false;

                    String interval = xfor.getInterval();
                    if (ConstantUtils.isConstantOne(context.typeGetter.apply(interval))) {
                        String start = xfor.getStart();
                        String end = xfor.getEnd();

                        Optional<VariableType> startType = context.typeGetter.apply(start);
                        Optional<VariableType> endType = context.typeGetter.apply(end);
                        if (ScalarUtils.hasConstant(startType) &&
                                ScalarUtils.hasConstant(endType)) {

                            Number startNum = ScalarUtils.getConstant(startType.get());
                            Number endNum = ScalarUtils.getConstant(endType.get());

                            if (startNum.intValue() == startNum.doubleValue()
                                    && endNum.intValue() == endNum.doubleValue()) {
                                int diff = endNum.intValue() - startNum.intValue();

                                iters = diff + 1;
                                knownIters = true;
                            }
                        }
                    }
                    if (!knownIters) {
                        iters = xfor.getLoopProperties().stream()
                                .filter(EstimatedIterationsProperty.class::isInstance)
                                .map(EstimatedIterationsProperty.class::cast)
                                .map(prop -> prop.getIterationCount())
                                .findFirst()
                                .orElse(iters); // Fallback to our earlier estimate
                    }

                    countInstructions(context, xfor.getLoopBlock(), iters * factor, info);
                    countInstructions(context, xfor.getEndBlock(), factor, info);
                } else if (instruction instanceof WhileInstruction) {
                    WhileInstruction xwhile = (WhileInstruction) instruction;

                    int iters = 100;

                    countInstructions(context, xwhile.getLoopBlock(), iters * factor, info);
                    countInstructions(context, xwhile.getEndBlock(), factor, info);
                } else {
                    for (int nestedBlockId : instruction.getOwnedBlocks()) {
                        countInstructions(context, nestedBlockId, factor, info);
                    }
                }
            }
        }
    }

    protected abstract boolean countInstruction(SchedulePredictorContext context,
            InstructionLocation instructionLocation,
            SsaInstruction instruction,
            double factor,
            T info);
}
