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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.passes.posttype.loopinterchange.LoopInterchangeFormat;
import org.specs.matisselib.passes.posttype.loopinterchange.LoopVariableImportContext;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.SpecsCollections;

public class LoopInterchangePass implements PostTypeInferencePass {

    private static final boolean ENABLE_LOG = false;

    public static final DataKey<String> OPTIMIZATION_ID = KeyFactory.string("optimization-id");
    public static final DataKey<Class> INTERCHANGE_FORMAT = KeyFactory
            .object("interchange-format", Class.class);

    private final String optimizationId;
    private final LoopInterchangeFormat interchangeFormat;

    public LoopInterchangePass(String optimizationId, Class<? extends LoopInterchangeFormat> interchangeFormatClass) {
        this.optimizationId = optimizationId;
        try {
            this.interchangeFormat = interchangeFormatClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public LoopInterchangePass(DataView parameters) {
        if (!parameters.hasValue(OPTIMIZATION_ID)) {
            throw new RuntimeException("Parameter optimization-id is missing.");
        }
        if (!parameters.hasValue(INTERCHANGE_FORMAT)) {
            throw new RuntimeException("Parameter interchange-format is missing.");
        }

        this.optimizationId = parameters.getValue(OPTIMIZATION_ID);

        try {
            this.interchangeFormat = (LoopInterchangeFormat) parameters.getValue(INTERCHANGE_FORMAT).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(TypedInstance instance,
            DataStore passData) {

        if (PassUtils.skipPass(instance, optimizationId)) {
            log("Skipping optimization " + optimizationId);
            return;
        }

        log("Applying to function " + instance.getFunctionIdentification().getName());
        while (tryApply(instance, passData)) {
        }
    }

    private boolean tryApply(TypedInstance instance,
            DataStore passData) {

        List<LoopIdentifier> loops = getCandidateLoops(instance);

        for (LoopIdentifier loop : loops) {
            for (int i = loop.loopAddress.size() - 2; i >= 0; --i) {
                if (tryApply(instance, new LoopIdentifier(loop.loopAddress.subList(i, loop.loopAddress.size())))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tryApply(TypedInstance instance, LoopIdentifier loop) {

        log("Trying with " + loop);

        List<LoopVariableImportContext> loopData = new ArrayList<>();
        for (int parentBlockId : loop.loopAddress) {
            ForInstruction xfor = (ForInstruction) instance.getBlock(parentBlockId).getEndingInstruction().get();

            String interval = xfor.getInterval();
            if (!ConstantUtils.isKnownPositiveInteger(instance.getVariableType(interval))) {
                log("Can't handle loops where interval isn't a known positive constant");
                return false;
            }

            int loopStartBlockId = xfor.getLoopBlock();
            LoopVariableImportContext loopCtx = new LoopVariableImportContext(loopStartBlockId);

            int afterLoopBlockId = xfor.getEndBlock();

            List<PhiInstruction> loopStartPhis = BlockUtils.getPhiNodes(instance.getBlock(loopStartBlockId));
            List<PhiInstruction> afterLoopPhis = BlockUtils.getPhiNodes(instance.getBlock(afterLoopBlockId));

            if (loopStartPhis.size() != afterLoopPhis.size()) {
                log("Invalid loop format: Mismatch in number of phis");
                return false;
            }

            if (loopStartPhis.size() == 0) {
                log("Don't know how to handle this case (no phis at loop start)");
                return false;
            }

            for (PhiInstruction loopStartPhi : loopStartPhis) {
                if (loopStartPhi.getSourceBlocks().size() != 2) {
                    log("Unsupported phi format");
                    return false;
                }

                int parentBlockIndex = loopStartPhi.getSourceBlocks().indexOf(parentBlockId);
                if (parentBlockIndex < 0) {
                    log("Unsupported phi format.");
                    return false;
                }
                int endBlockIndex = parentBlockIndex == 0 ? 1 : 0;
                int endBlockId = loopStartPhi.getSourceBlocks().get(endBlockIndex);
                if (loopCtx.endBlockId >= 0) {
                    if (loopCtx.endBlockId != endBlockId) {
                        log("Unexpected phi end blocks: Is this even valid SSA?");
                        return false;
                    }
                } else {
                    loopCtx.endBlockId = endBlockId;
                }

                String parentBlockVariable = loopStartPhi.getInputVariables().get(parentBlockIndex);
                String loopEndVariable = loopStartPhi.getInputVariables().get(endBlockIndex);
                String loopStartVariable = loopStartPhi.getOutput();

                String afterLoopName = null;

                for (PhiInstruction afterLoopPhi : afterLoopPhis) {
                    if (afterLoopPhi.getSourceBlocks().size() != 2) {
                        log("Invalid phi format");
                        return false;
                    }

                    int parentBlockIndex2 = afterLoopPhi.getSourceBlocks().indexOf(parentBlockId);
                    if (parentBlockIndex2 < 0) {
                        log("Unsupported phi format");
                        return false;
                    }
                    int endBlockIndex2 = parentBlockIndex2 == 0 ? 1 : 0;
                    if (afterLoopPhi.getSourceBlocks().get(endBlockIndex2) != endBlockId) {
                        log("Unsupported phi format");
                        return false;
                    }

                    String parentBlockVariable2 = afterLoopPhi.getInputVariables().get(parentBlockIndex2);
                    String loopEndVariable2 = afterLoopPhi.getInputVariables().get(endBlockIndex2);

                    if (!loopEndVariable2.equals(loopEndVariable)) {
                        continue;
                    }

                    if (!parentBlockVariable2.equals(parentBlockVariable)) {
                        log("Unsupported phi format");
                        return false;
                    }

                    afterLoopName = afterLoopPhi.getOutput();
                    break;
                }

                LoopVariable variable = new LoopVariable(parentBlockVariable, loopStartVariable, loopEndVariable,
                        afterLoopName);

                if (!variable.getAfterLoop().isPresent()) {
                    log("Unexpected format: No matching phi after loop, for " + loopStartPhi);
                    return false;
                }

                loopCtx.variables.add(variable);
            }

            loopData.add(loopCtx);
        }

        // We have the loop data.
        // Now we need to see if the data from the inner loops matches the data from the outer loops.
        // First of all, all loops must have the same number of phi variables.

        // TODO: We might be able to use only the "outer" loops, or only the "inner" loops.
        int numVariables = loopData.get(0).variables.size();
        for (LoopVariableImportContext loopCtx : loopData) {
            if (loopCtx.variables.size() != numVariables) {
                log("Mismatch in number of variables: " + loopCtx.variables + ", expected " + numVariables);
                return false;
            }
        }

        // As we check that the variables match, we'll make sure that they are in matching indices.
        List<LoopVariableImportContext> sortedLoopData = new ArrayList<>();
        sortedLoopData.add(loopData.get(0));
        for (int i = 1; i < loopData.size(); ++i) {
            LoopVariableImportContext lastContext = SpecsCollections.last(sortedLoopData);
            LoopVariableImportContext unsortedContext = loopData.get(i);
            LoopVariableImportContext sortedContext = new LoopVariableImportContext(unsortedContext.startBlockId);
            sortedContext.endBlockId = unsortedContext.endBlockId;

            for (LoopVariable variable : lastContext.variables) {
                String variableStartName = variable.loopStart;
                String variableEndName = variable.loopEnd;

                boolean foundVariable = false;
                for (LoopVariable variable2 : unsortedContext.variables) {
                    if (variable2.beforeLoop.equals(variableStartName)
                            && variable2.getAfterLoop().equals(Optional.of(variableEndName))) {

                        sortedContext.variables.add(variable2);
                        foundVariable = true;
                        break;
                    }
                }

                if (!foundVariable) {
                    log("Unsupported phi format.");
                    return false;
                }
            }

            sortedLoopData.add(sortedContext);
        }

        log("SLD=" + sortedLoopData);

        // We'll now get the iteration variables:
        List<String> iterationVars = new ArrayList<>();
        for (int parentBlockId : loop.loopAddress) {
            ForInstruction xfor = (ForInstruction) instance.getBlock(parentBlockId).getEndingInstruction().get();
            int loopBlockId = xfor.getLoopBlock();

            IterInstruction iter = null;
            for (SsaInstruction instruction : instance.getBlock(loopBlockId).getInstructions()) {
                if (instruction instanceof IterInstruction) {
                    iter = (IterInstruction) instruction;
                    break;
                }
            }
            if (iter == null) {
                log("Unsupported: Loop with no iteration variable");
                return false;
            }

            iterationVars.add(iter.getOutput());
        }

        log("Iteration vars: " + iterationVars);

        Set<String> middleLoopBlacklistVariables = new HashSet<>();
        Set<String> illegalUses = new HashSet<>();
        Map<String, SsaInstruction> potentialCandidates = new HashMap<>();
        for (int i = 0; i < loopData.size() - 1; ++i) {
            LoopVariableImportContext loopVariableImportContext = loopData.get(i);
            int loopBlockId = loopVariableImportContext.startBlockId;
            for (SsaInstruction instruction : instance.getBlock(loopBlockId).getInstructions()) {
                if (instruction instanceof PhiInstruction
                        || instruction instanceof IterInstruction
                        || instruction instanceof ForInstruction
                        || instruction.getInstructionType() == InstructionType.HAS_VALIDATION_SIDE_EFFECT
                        || instruction.getInstructionType() == InstructionType.HAS_SIDE_EFFECT) {

                    log("Can't move " + instruction);
                    middleLoopBlacklistVariables.addAll(instruction.getInputVariables());
                } else {
                    for (String output : instruction.getOutputs()) {
                        potentialCandidates.put(output, instruction);
                    }
                }
            }

            // No need to worry about nested loops in the end, etc.
            // Because Loop interchange doesn't apply to loops with complicated intermediate logic.

            for (SsaInstruction instruction : instance.getBlock(loopVariableImportContext.endBlockId)
                    .getInstructions()) {
                illegalUses.addAll(instruction.getInputVariables());
                middleLoopBlacklistVariables.addAll(instruction.getOutputs());
            }
        }

        Set<String> declarations = BlockUtils.getVariablesDeclaredInContainedBlocks(instance,
                loopData.get(0).startBlockId);
        for (String varDecl : new ArrayList<>(middleLoopBlacklistVariables)) {
            if (!declarations.contains(varDecl)) {
                middleLoopBlacklistVariables.remove(varDecl);
            }
        }

        log("Directly illegal to move intermediate variables: " + middleLoopBlacklistVariables);

        // We now have the directly illegal variables, see which variables are "indirectly" illegal.
        Queue<String> pendingVariables = new LinkedList<>();
        pendingVariables.addAll(illegalUses);
        while (!pendingVariables.isEmpty()) {
            String illegalVariable = pendingVariables.poll();

            for (String potentialCandidate : new ArrayList<>(potentialCandidates.keySet())) {
                SsaInstruction instruction = potentialCandidates.get(potentialCandidate);

                if (instruction.getOutputs().contains(illegalVariable)) {
                    // Indirectly illegal. Reason: illegalVariable is used by variable that can't be moved into the
                    // loop.

                    for (String output : instruction.getOutputs()) {
                        potentialCandidates.remove(output);
                    }

                    // All our inputs are now illegal as well.
                    pendingVariables.addAll(instruction.getInputVariables());
                }
            }
        }

        log("Variables that are safe to move to the inner loop: " + potentialCandidates.keySet());

        for (String iter : iterationVars) {
            if (middleLoopBlacklistVariables.contains(iter)) {
                log("Iter variable used in loop other than the inner-most one (" + iter + ")");
                return false;
            }
        }

        // See if the for loops depend on the variables declared in the loops.
        for (int parentBlockId : loop.loopAddress) {
            ForInstruction xfor = (ForInstruction) instance.getBlock(parentBlockId).getEndingInstruction().get();

            for (String usedVariable : xfor.getInputVariables()) {
                if (middleLoopBlacklistVariables.contains(usedVariable)) {
                    log("For loop depends on variable declared in outer loop: " + usedVariable);
                    return false;
                }
            }
        }

        List<String> iterIndices = getPreferredIndexOrder(instance, iterationVars,
                sortedLoopData);
        if (iterIndices == null) {
            return false;
        }

        log("Accesses: " + iterIndices);

        List<String> recommendedFormat = new ArrayList<>(iterIndices);
        Collections.reverse(recommendedFormat);
        if (recommendedFormat.size() != iterationVars.size()) {
            log("Not all iteration vars are referenced in order. Don't know how to handle this case.");
            return false;
        }

        assert recommendedFormat.size() == iterationVars.size();

        if (recommendedFormat.equals(iterationVars)) {
            log("Loop already has the preferred interchange format");
            return false;
        }

        List<Integer> interchangeOrder = new ArrayList<>();
        for (String i : recommendedFormat) {
            interchangeOrder.add(iterationVars.indexOf(i));
        }

        log("Interchanging. Recommended format: " + recommendedFormat + ", current format: " + iterationVars);
        log("Variables to extract: " + potentialCandidates.keySet());
        log("Interchange indices: " + interchangeOrder);

        injectVariables(instance, sortedLoopData, potentialCandidates.keySet());

        List<ForInstruction> originalFors = new ArrayList<>();
        for (int parentBlockId : loop.loopAddress) {
            originalFors.add((ForInstruction) instance.getBlock(parentBlockId).getEndingInstruction().get().copy());
        }

        // Switch the fors and iters
        for (int i = 0; i < loop.loopAddress.size(); ++i) {
            int parentBlockId = loop.loopAddress.get(i);
            SsaBlock block = instance.getBlock(parentBlockId);

            int newId = interchangeOrder.get(i);
            if (newId == i) {
                // No interchange needed.
                continue;
            }
            ForInstruction originalFor = originalFors.get(i);
            ForInstruction interchangeFor = originalFors.get(newId);

            // FIXME: Line instructions?
            int loopBlockId = originalFor.getLoopBlock();
            ForInstruction newFor = new ForInstruction(
                    interchangeFor.getStart(),
                    interchangeFor.getInterval(),
                    interchangeFor.getEnd(),
                    loopBlockId,
                    originalFor.getEndBlock(),
                    interchangeFor.getLoopProperties());

            block.getInstructions().set(block.getInstructions().size() - 1, newFor);

            boolean foundIter = false;
            ListIterator<SsaInstruction> iterator = instance.getBlock(loopBlockId).getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof IterInstruction) {
                    IterInstruction newIter = new IterInstruction(iterationVars.get(newId));
                    iterator.set(newIter);
                    foundIter = true;
                    break;
                }
            }
            assert foundIter;
        }

        log("Performed interchange successfully");
        return true;
    }

    private List<String> getPreferredIndexOrder(TypedInstance instance,
            List<String> iterationVars,
            List<LoopVariableImportContext> sortedLoopData) {

        log("Finding preferred loop order");
        Map<String, String> derivedFromIndex = new HashMap<>();
        Map<String, String> safeDerivedFrom = new HashMap<>();
        for (String iter : iterationVars) {
            derivedFromIndex.put(iter, iter);
            safeDerivedFrom.put(iter, iter);
        }

        if (!computeDerivedIndices(instance, sortedLoopData, derivedFromIndex)) {
            log("Could not compute derived indices");
            return null;
        }
        if (!computeStrictlySmallerDerivedIndices(instance, sortedLoopData, derivedFromIndex.keySet(),
                safeDerivedFrom)) {
            log("Could not compute strictly smaller derived indices");
            return null;
        }

        List<String> derivedIndices = interchangeFormat.computeAccessIndices(instance,
                SpecsCollections.last(sortedLoopData).startBlockId,
                iterationVars, null,
                derivedFromIndex,
                safeDerivedFrom,
                sortedLoopData);

        if (derivedIndices == null) {
            log("Could not determine access indices");
            return null;
        }

        List<String> sourceIndices = new ArrayList<>();
        for (int i = 0; i < derivedIndices.size(); ++i) {
            String sourceIndex = derivedFromIndex.get(derivedIndices.get(i));

            if (iterationVars.contains(sourceIndex)) {
                sourceIndices.add(sourceIndex);
            }
        }

        return sourceIndices;
    }

    private boolean computeDerivedIndices(TypedInstance instance,
            List<LoopVariableImportContext> sortedLoopData,
            Map<String, String> derivedFromIndex) {

        for (int i = 0; i < sortedLoopData.size(); ++i) {
            LoopVariableImportContext loopData = sortedLoopData.get(i);

            int blockId = loopData.startBlockId;
            if (!computeDerivedIndicesInBlock(instance, blockId, derivedFromIndex)) {
                return false;
            }
        }

        return true;
    }

    private boolean computeStrictlySmallerDerivedIndices(TypedInstance instance,
            List<LoopVariableImportContext> sortedLoopData,
            Set<String> derivedFromIndex,
            Map<String, String> safeDerivedFrom) {

        for (int i = 0; i < sortedLoopData.size(); ++i) {
            LoopVariableImportContext loopData = sortedLoopData.get(i);

            int blockId = loopData.startBlockId;
            if (!computeStrictlySmallerDerivedIndicesInBlock(instance, blockId, derivedFromIndex, safeDerivedFrom)) {
                return false;
            }
        }

        return true;
    }

    private boolean computeDerivedIndicesInBlock(TypedInstance instance, int blockId,
            Map<String, String> derivedFromIndex) {
        for (SsaInstruction instruction : instance.getBlock(blockId).getInstructions()) {
            List<String> sources = instruction.getInputVariables()
                    .stream()
                    .filter(derivedFromIndex::containsKey)
                    .map(derivedFromIndex::get)
                    .distinct()
                    .collect(Collectors.toList());
            if (!sources.isEmpty()) {
                String source = sources.size() == 1 ? sources.get(0) : null;
                for (String output : instruction.getOutputs()) {
                    derivedFromIndex.put(output, source);
                    break;
                }
            }

            if (instruction instanceof BranchInstruction) {

                BranchInstruction branch = (BranchInstruction) instruction;

                boolean markAllAsUsed = derivedFromIndex.containsKey(branch.getConditionVariable());

                if (!handleInnerBlockAccesses(instance, branch.getTrueBlock(), derivedFromIndex, markAllAsUsed)) {
                    return false;
                }
                if (!handleInnerBlockAccesses(instance, branch.getFalseBlock(), derivedFromIndex, markAllAsUsed)) {
                    return false;
                }

                return computeDerivedIndicesInBlock(instance, branch.getEndBlock(), derivedFromIndex);
            }
        }

        return true;
    }

    private boolean computeStrictlySmallerDerivedIndicesInBlock(TypedInstance instance, int blockId,
            Set<String> derivedFromIndex,
            Map<String, String> safeDerivedFrom) {
        for (SsaInstruction instruction : instance.getBlock(blockId).getInstructions()) {
            List<String> sources = instruction.getInputVariables()
                    .stream()
                    .filter(derivedFromIndex::contains)
                    .map(value -> safeDerivedFrom.getOrDefault(value, value))
                    .distinct()
                    .collect(Collectors.toList());
            if (!sources.isEmpty()) {
                String source = sources.size() == 1 ? sources.get(0) : null;
                for (String output : instruction.getOutputs()) {
                    if (isStrictlyLessThan(instance, instruction, source)) {
                        safeDerivedFrom.put(output, source);
                    }
                    break;
                }
            }

            if (instruction instanceof BranchInstruction) {

                BranchInstruction branch = (BranchInstruction) instruction;

                return computeStrictlySmallerDerivedIndicesInBlock(instance, branch.getEndBlock(), derivedFromIndex,
                        safeDerivedFrom);
            }
        }

        return true;
    }

    private boolean isStrictlyLessThan(TypedInstance instance, SsaInstruction instruction, String source) {
        log("Checking " + instruction);

        if (!(instruction instanceof FunctionCallInstruction)) {
            return false;
        }

        FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

        return functionCall.getFunctionName().equals("minus") &&
                functionCall.getInputVariables().size() == 2 &&
                functionCall.getInputVariables().get(0).equals(source) &&
                ConstantUtils.isKnownPositiveInteger(instance.getVariableType(functionCall.getInputVariables().get(1)));
    }

    private static boolean handleInnerBlockAccesses(TypedInstance instance, int blockId,
            Map<String, String> derivedFromIndex,
            boolean markAllAsUsed) {
        SsaBlock block = instance.getBlock(blockId);

        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof SimpleGetInstruction ||
                    instruction instanceof GetOrFirstInstruction ||
                    instruction instanceof SimpleSetInstruction) {

                log("Matrix access in inner block " + instruction);
                return false;
            }

            for (String usedVar : instruction.getInputVariables()) {
                if (markAllAsUsed || derivedFromIndex.containsKey(usedVar)) {
                    for (String output : instruction.getOutputs()) {
                        // TODO: We can probably do better than this.
                        derivedFromIndex.put(output, null);
                    }
                    break;
                }
            }
        }

        return true;
    }

    private static void injectVariables(TypedInstance instance,
            List<LoopVariableImportContext> sortedLoopData,
            Set<String> variablesToExtract) {

        Set<String> extractedVariables = new HashSet<>();
        List<SsaInstruction> extractedInstructions = new ArrayList<>();
        for (LoopVariableImportContext loop : sortedLoopData) {
            for (ListIterator<SsaInstruction> iterator = instance.getBlock(loop.startBlockId).getInstructions()
                    .listIterator(); iterator.hasNext();) {

                SsaInstruction instruction = iterator.next();
                if (instruction.getOutputs().stream().anyMatch(variablesToExtract::contains)) {
                    extractedInstructions.add(instruction);
                    extractedVariables.addAll(instruction.getOutputs());

                    iterator.remove();
                }
            }
        }

        assert extractedVariables.equals(variablesToExtract);

        // Also move inner-most iteration
        LoopVariableImportContext innerMostLoop = SpecsCollections.last(sortedLoopData);
        SsaBlock block = instance.getBlock(innerMostLoop.startBlockId);

        for (ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator(); iterator.hasNext();) {
            SsaInstruction instruction = iterator.next();

            if (instruction instanceof IterInstruction) {
                extractedInstructions.add(0, instruction);
                iterator.remove();
            }
        }

        int insertionPoint = BlockUtils.getAfterPhiInsertionPoint(block);

        block.insertInstructions(insertionPoint, extractedInstructions);
    }

    private static List<LoopIdentifier> getCandidateLoops(TypedInstance instance) {
        List<LoopIdentifier> loops = getLoops(instance);

        log("Initially identified loops " + loops);

        Set<Integer> invalidLoops = new HashSet<>();
        ListIterator<LoopIdentifier> iterator = loops.listIterator();
        while (iterator.hasNext()) {
            LoopIdentifier loop = iterator.next();

            int parentId = loop.getInnerMostId();
            int blockId = ((ForInstruction) instance.getBlock(parentId).getEndingInstruction().get()).getLoopBlock();
            if (BlockUtils.hasNestedSideEffects(instance, blockId) || !BlockUtils.isSimpleSection(instance, blockId)) {
                log("Excluding " + loop + ", because it is not in a recognized format");
                invalidLoops.add(parentId);
                iterator.remove();
            }
        }

        for (LoopIdentifier identifier : loops) {
            identifier.trimTo(invalidLoops);
        }

        // If the depth is just 1, then it doesn't make sense to interchange anything.
        iterator = loops.listIterator();
        while (iterator.hasNext()) {
            LoopIdentifier loop = iterator.next();

            if (loop.getDepth() < 2) {
                log("Excluding " + loop + ", because interchange requires loops to be at least 2D.");
                iterator.remove();
            }
        }
        return loops;
    }

    private static List<LoopIdentifier> getLoops(TypedInstance instance) {
        List<LoopIdentifier> loops = new ArrayList<>();

        getLoops(loops, instance, Collections.emptyList(), 0);

        return loops;
    }

    private static void getLoops(List<LoopIdentifier> loops,
            TypedInstance instance,
            List<Integer> parentLoopAddress,
            int blockId) {

        // We'll get the list of loops. If a loop A is the only child of a loop B, then the
        // address of A includes B.
        // If B is a root loop, the address of A would be [B A].
        // But if B had any if statements or more than one loops, then A would have address of just [A].

        SsaBlock block = instance.getBlock(blockId);
        block.getEndingInstruction().ifPresent(instruction -> {
            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;

                if (!instance.getBlock(xfor.getEndBlock()).getEndingInstruction().isPresent()) {

                    List<Integer> loopAccess = new ArrayList<>();
                    loopAccess.addAll(parentLoopAddress);
                    loopAccess.add(blockId);
                    loops.add(new LoopIdentifier(loopAccess));

                    getLoops(loops, instance, loopAccess, xfor.getLoopBlock());
                    return;
                }

                List<Integer> loopAccess = new ArrayList<>();
                loopAccess.add(blockId);
                loops.add(new LoopIdentifier(loopAccess));

                getLoops(loops, instance, loopAccess, xfor.getLoopBlock());
                getLoops(loops, instance, Collections.emptyList(), xfor.getEndBlock());
                return;
            }

            for (int childBlockId : instruction.getOwnedBlocks()) {
                getLoops(loops, instance, Collections.emptyList(), childBlockId);
            }
        });
    }

    static class LoopIdentifier {
        List<Integer> loopAddress;

        LoopIdentifier(List<Integer> address) {
            this.loopAddress = new ArrayList<>(address);
        }

        public void trimTo(Set<Integer> loopsToRemove) {
            int i;
            for (i = this.loopAddress.size() - 2; i >= 0; --i) {
                if (loopsToRemove.contains(i)) {
                    break;
                }
            }

            if (i >= 0) {
                this.loopAddress.subList(0, i + 1).clear();
            }
        }

        public int getDepth() {
            return this.loopAddress.size();
        }

        public int getInnerMostId() {
            return SpecsCollections.last(this.loopAddress);
        }

        @Override
        public String toString() {
            return this.loopAddress.toString();
        }
    }

    private static void log(String message) {
        if (LoopInterchangePass.ENABLE_LOG) {
            System.out.print("[loop_interchange] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

    @Override
    public DataView getParameters() {
        DataStore parameters = new SimpleDataStore("interchange-data");
        parameters.add(OPTIMIZATION_ID, optimizationId);
        parameters.add(INTERCHANGE_FORMAT, interchangeFormat.getClass());

        return DataView.newInstance(parameters);
    }

    public static List<DataKey<?>> getRequiredParameters() {
        return Arrays.asList(INTERCHANGE_FORMAT, OPTIMIZATION_ID);
    }
}
