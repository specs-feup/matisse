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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.FunctionProperty;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.passes.ssa.BlockReorderingPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.ssa.ParallelRegionId;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeParallelFunctionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelBlockInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class ParallelBlockExtractorPass implements PostTypeInferencePass {

    private static final boolean ENABLE_LOG = false;

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Preconditions.checkArgument(instance != null);
        Preconditions.checkArgument(passData != null);

        log("Starting function " + instance.getFunctionIdentification().getName());

        while (tryExtract(instance, passData)) {
        }
    }

    private static boolean tryExtract(TypedInstance instance, DataStore passData) {
        FunctionBody body = instance.getFunctionBody();

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);

        for (int blockId = 0; blockId < body.getBlocks().size(); ++blockId) {
            SsaBlock block = body.getBlock(blockId);
            LineInstruction line = null;

            List<SsaInstruction> instructions = block.getInstructions();
            for (int i = 0; i < instructions.size(); ++i) {
                SsaInstruction instruction = instructions.get(i);

                if (instruction instanceof LineInstruction) {
                    line = (LineInstruction) instruction;
                }

                if (instruction instanceof ParallelBlockInstruction) {
                    extract(instance, blockId, i, (ParallelBlockInstruction) instruction, line, passData);

                    dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
                    dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
                    return true;
                }
            }
        }

        return false;
    }

    private static void extract(TypedInstance instance,
            int blockId, int instructionId,
            ParallelBlockInstruction instruction,
            LineInstruction line,
            DataStore passData) {
        log("Extracting " + instruction);

        List<Integer> blocksToOutline = new ArrayList<>();

        Queue<Integer> blocksToVisit = new LinkedList<>();
        blocksToVisit.add(instruction.getContentBlock());

        while (!blocksToVisit.isEmpty()) {
            int visitedBlockId = blocksToVisit.poll();

            if (blocksToOutline.contains(visitedBlockId)) {
                continue;
            }

            blocksToOutline.add(visitedBlockId);

            SsaBlock block = instance.getBlock(visitedBlockId);
            block.getEndingInstruction().ifPresent(end -> {
                List<Integer> ownedBlocks = end.getOwnedBlocks();
                blocksToVisit.addAll(ownedBlocks);
            });
        }

        FunctionBody oldBody = instance.getFunctionBody();
        FunctionBody newBody = new FunctionBody(
                oldBody.makeTemporary(instance.getFunctionIdentification().getName()),
                instance.getFirstLine());
        for (FunctionProperty property : oldBody.getProperties()) {
            newBody.addProperty(property);
        }

        for (String temporary : oldBody.getLastTemporaries().keySet()) {
            int value = oldBody.getLastTemporaries().get(temporary);
            for (int i = 0; i < value; ++i) {
                newBody.makeTemporary(temporary);
            }
        }

        for (int blockToOutline : blocksToOutline) {
            newBody.addBlock(instance.getBlock(blockToOutline).copy());
        }

        List<Integer> newNames = new ArrayList<>();
        for (int i = 0; i < blocksToOutline.size(); ++i) {
            newNames.add(i);
        }
        newBody.renameBlocks(blocksToOutline, newNames);

        int endBlockId = instruction.getEndBlock();
        instance.getFunctionBody().renameBlocks(Arrays.asList(endBlockId), Arrays.asList(blockId));

        UsageMap baseMap = UsageMap.build(instance.getFunctionBody());
        UsageMap outlinedMap = UsageMap.build(newBody);
        baseMap.remove(outlinedMap);

        Set<String> usedInOutlined = new HashSet<>();
        List<String> declaredInOutlined = new ArrayList<>();

        for (SsaInstruction outlinedInstruction : newBody.getFlattenedInstructionsIterable()) {
            usedInOutlined.addAll(outlinedInstruction.getInputVariables());
            declaredInOutlined.addAll(outlinedInstruction.getOutputs());
        }

        usedInOutlined.addAll(instruction.getInputVariables());

        List<String> outs = new ArrayList<>();
        for (String out : declaredInOutlined) {
            if (baseMap.getUsageCount(out) > 0 || out.endsWith("$ret")) {
                outs.add(out);
            }
        }

        List<String> ins = new ArrayList<>();
        for (String in : usedInOutlined) {
            if (!declaredInOutlined.contains(in)) {
                ins.add(in);
            }
        }

        Map<String, VariableType> types = new HashMap<>();
        for (String var : declaredInOutlined) {
            instance.getVariableType(var).ifPresent(type -> types.put(var, type));
        }
        for (String var : usedInOutlined) {
            instance.getVariableType(var).ifPresent(type -> types.put(var, type));
        }

        if (line != null) {
            newBody.getBlock(0).prependInstruction(line.copy());
        }

        ParallelRegionInstance regionInstance = new ParallelRegionInstance(instruction.getSettings(),
                newBody,
                ins,
                outs, types);

        ParallelRegionId regionId = passData
                .get(CLServices.PARALLEL_REGION_SINK)
                .emitRegion(regionInstance);

        SsaInstruction newInstruction = new InvokeParallelFunctionInstruction(regionId, outs, ins);
        SsaBlock block = instance.getBlock(blockId);
        block.replaceInstructionAt(instructionId, newInstruction);
        block.addInstructions(instance.getBlock(endBlockId).getInstructions());

        List<Integer> blocksToRemove = new ArrayList<>();
        blocksToRemove.addAll(blocksToOutline);
        blocksToRemove.add(endBlockId);
        instance.getFunctionBody().removeAndRenameBlocks(blocksToOutline);

        new BlockReorderingPass().apply(instance, passData);
    }

    private static void log(String message) {
        if (ParallelBlockExtractorPass.ENABLE_LOG) {
            System.out.print("[parallel_block_extractor] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
