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

package org.specs.matlabtocl.v2.codegen.loopconverters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.codegen.GeneratedCodeSegment;
import org.specs.matlabtocl.v2.codegen.GeneratedSetRange;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.services.KernelInstanceSink;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.reporting.Reporter;

public class SetRangeBuilder implements LoopConverter {

    @Override
    public Optional<GeneratedCodeSegment> generateCode(TypedInstance containerInstance,
            ParallelRegionInstance parallelInstance,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings parallelSettings,
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            KernelInstanceSink kernelSink,
            DataStore passData,
            BiFunction<String, VariableType, String> makeTemporary,
            ProviderData providerData,
            Reporter reporter) {

        if (!codeGenerationStrategyProvider.isRangeSetInstructionEnabled()) {
            return Optional.empty();
        }

        if (parallelSettings.isRangeSetDisabled()) {
            return Optional.empty();
        }

        if (parallelLoop.loopDeclarationBlockIds.size() != 1) {
            return Optional.empty();
        }

        // TODO: Maybe we can handle more than one?
        if (parallelLoop.reductions.size() != 1) {
            return Optional.empty();
        }

        Reduction reduction = parallelLoop.reductions.get(0);
        if (reduction.getReductionType() != ReductionType.MATRIX_SET) {
            return Optional.empty();
        }

        List<LoopVariable> loopVariables = reduction.getLoopVariables();
        assert loopVariables.size() == 1;
        LoopVariable loopVariable = loopVariables.get(0);

        ForInstruction xfor = (ForInstruction) parallelInstance.getBody()
                .getBlock(parallelLoop.loopDeclarationBlockIds.get(0))
                .getEndingInstruction()
                .get();

        if (!ConstantUtils.isConstantOne(parallelInstance.getType(xfor.getStart())) ||
                !ConstantUtils.isConstantOne(parallelInstance.getType(xfor.getInterval()))) {
            return Optional.empty();
        }

        String iterVar = null;
        List<String> indices = null;
        String value = null;
        Set<String> declaredVars = new HashSet<>();

        for (SsaInstruction instruction : parallelInstance.getBody().getBlock(xfor.getLoopBlock()).getInstructions()) {
            declaredVars.addAll(instruction.getOutputs());

            if (instruction instanceof IterInstruction) {
                iterVar = ((IterInstruction) instruction).getOutput();
                continue;
            }
            if (instruction instanceof SimpleSetInstruction) {
                SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                if (!set.getInputMatrix().equals(loopVariable.loopStart)) {
                    return Optional.empty();
                }
                if (!set.getOutput().equals(loopVariable.loopEnd)) {
                    return Optional.empty();
                }

                indices = set.getIndices();
                value = set.getValue();
                continue;
            }
        }

        if (iterVar == null || indices == null || !indices.equals(Arrays.asList(iterVar))) {
            return Optional.empty();
        }

        assert value != null;
        if (declaredVars.contains(value)) {
            // value was declared in loop.
            return Optional.empty();
        }

        return Optional.of(new GeneratedSetRange(parallelSettings, xfor.getEnd(), value));
    }

}
