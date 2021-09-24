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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * This pass removes branches that always go through the same path (e.g. if 1).
 * 
 * @author Luís Reis
 *
 */
public class ConstantBranchEliminationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Preconditions.checkArgument(instance != null);
        Preconditions.checkArgument(passData != null);

        apply(instance.getFunctionBody(), passData, name -> getConstantBranchValue(instance.getVariableType(name)));
    }

    private static Optional<Boolean> getConstantBranchValue(Optional<VariableType> variableType) {
        return variableType.flatMap(ConstantBranchEliminationPass::getConstantBranchValue);
    }

    private static Optional<Boolean> getConstantBranchValue(VariableType variableType) {
        if (variableType instanceof ScalarType) {
            return getConstantBranchValue(((ScalarType) variableType).scalar().getConstant());
        }
        return Optional.empty();
    }

    private static Optional<Boolean> getConstantBranchValue(Number constant) {
        if (constant == null) {
            return Optional.empty();
        }

        double value = constant.doubleValue();
        return Optional.of(value != 0);
    }

    public static void apply(FunctionBody functionBody, DataStore passData,
            Function<String, Optional<Boolean>> isConstant) {

        while (tryApply(functionBody, passData, isConstant)) {
        }
    }

    private static boolean tryApply(FunctionBody functionBody,
            DataStore passData,
            Function<String, Optional<Boolean>> isConstant) {

        for (int blockId = 0; blockId < functionBody.getBlocks().size(); ++blockId) {

            SsaBlock block = functionBody.getBlock(blockId);

            Optional<BranchInstruction> instruction = block.getEndingInstruction()
                    .filter(BranchInstruction.class::isInstance)
                    .map(BranchInstruction.class::cast);
            if (instruction.isPresent()) {
                BranchInstruction branch = instruction.get();

                Optional<Boolean> conditionValue = isConstant.apply(branch.getConditionVariable());
                if (conditionValue.equals(Optional.of(true))) {
                    removeBranch(functionBody,
                            passData,
                            blockId,
                            branch.getTrueBlock(),
                            branch.getFalseBlock(),
                            branch.getEndBlock());
                    return true;
                } else if (conditionValue.equals(Optional.of(false))) {
                    removeBranch(functionBody,
                            passData,
                            blockId,
                            branch.getFalseBlock(),
                            branch.getTrueBlock(),
                            branch.getEndBlock());
                    return true;
                }
            }
        }

        return false;
    }

    private static void removeBranch(FunctionBody functionBody,
            DataStore passData,
            int blockId,
            int enterBlockId,
            int eraseBlockId,
            int endBlockId) {

        passData.getTry(ProjectPassServices.DATA_PROVIDER).ifPresent(dataProviderService -> {
            dataProviderService.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
            dataProviderService.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        });

        // Remove branch instruction
        SsaBlock containerBlock = functionBody.getBlock(blockId);
        containerBlock.removeLastInstruction();

        // Put all instructions from enterBlock
        SsaBlock enterBlock = functionBody.getBlock(enterBlockId);
        containerBlock.addInstructions(enterBlock.getInstructions());

        // Find which block the container ends with and put the end block instructions there.
        int newEndId = BlockUtils.getBlockEnd(functionBody, blockId);
        SsaBlock newEndBlock = functionBody.getBlock(newEndId);
        SsaBlock endBlock = functionBody.getBlock(endBlockId);
        newEndBlock.addInstructions(endBlock.getInstructions());

        // Fix phi nodes so they no longer reference eraseBlockId, and enterBlockId becomes blockId
        ListIterator<SsaInstruction> iterator = newEndBlock.getInstructions().listIterator();
        while (iterator.hasNext()) {
            SsaInstruction instruction = iterator.next();
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                List<Integer> sourceBlocks = new ArrayList<>(phi.getSourceBlocks());
                List<String> sourceVariables = new ArrayList<>(phi.getInputVariables());

                int indexToRemove = sourceBlocks.indexOf(BlockUtils.getBlockEnd(functionBody, eraseBlockId));
                if (indexToRemove != -1) {
                    sourceBlocks.remove(indexToRemove);
                    sourceVariables.remove(indexToRemove);
                }

                assert sourceBlocks.size() == sourceVariables.size();

                SsaInstruction newInstruction;
                if (sourceBlocks.size() == 1) {
                    newInstruction = AssignmentInstruction.fromVariable(phi.getOutput(), sourceVariables.get(0));
                } else {
                    newInstruction = new PhiInstruction(phi.getOutput(), sourceVariables, sourceBlocks);
                }

                iterator.set(newInstruction);
            }
        }

        // Fix block names
        functionBody.renameBlocks(Arrays.asList(enterBlockId, endBlockId), Arrays.asList(blockId, newEndId));

        // Remove all blocks that are no longer relevant
        Set<Integer> blocksToRemove = new HashSet<>();
        Queue<Integer> blocksToVisit = new LinkedList<>();

        // Remove entered and end block, non-recursively
        blocksToRemove.add(enterBlockId);
        blocksToRemove.add(endBlockId);

        // Remove erased block, recursively
        blocksToVisit.add(eraseBlockId);

        while (!blocksToVisit.isEmpty()) {
            int block = blocksToVisit.poll();
            if (blocksToRemove.add(block)) {
                List<Integer> referencedBlocks = functionBody.getBlock(block)
                        .getEndingInstruction()
                        .map(i -> i.getOwnedBlocks())
                        .orElse(Collections.emptyList());

                blocksToVisit.addAll(referencedBlocks);
            }
        }

        functionBody.removeAndRenameBlocks(new ArrayList<>(blocksToRemove));
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH, // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION); // Explicitly invalidated
    }
}
