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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.loopproperties.FusionResult;
import org.specs.matisselib.loopproperties.LoopProperty;
import org.specs.matisselib.passes.posttype.loopfusion.MatrixAccessPattern;
import org.specs.matisselib.passes.posttype.loopfusion.MatrixIndex;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsCollections;

public class LoopFusionPass implements PostTypeInferencePass {
    private static final boolean ENABLE_DIAGNOSTICS = false;
    private static final boolean VALIDATE_EVERY_ITERATION = false;

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        apply(instance.getFunctionBody(),
                instance.getProviderData(),
                instance::getVariableType,
                instance::makeTemporary,
                passData);

    }

    /**
     * This function is public so that unit tests can call it directly.
     */
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        if (PassUtils.skipPass(body, "loop_fusion")) {
            log("Pass disabled");
            return;
        }

        SizeGroupInformation info = PassUtils.getData(passData, CompilerDataProviders.SIZE_GROUP_INFORMATION);

        while (tryCombineLoops(body, providerData, typeGetter, makeTemporary, passData, info)) {
        }

    }

    private static void log(String message) {
        if (LoopFusionPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[loop_fusion] ");
            System.out.println(message);
        }
    }

    private static boolean tryCombineLoops(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData,
            SizeGroupInformation info) {

        if (LoopFusionPass.VALIDATE_EVERY_ITERATION) {
            new SsaValidatorPass("mid-pass").apply(body, passData);
        }

        // The basic form of loops to combine is

        // block #StartBlock ends in for <RANGE>, @FirstLoop, @Middle
        // block #FirstLoop is a "unit block", meaning that it ends in an implicit continue
        // block #Middle ends in for <RANGE_2>, @SecondLoop, @Ending
        // block #SecondLoop is a "unit block".
        // block #Ending can have any format

        List<SsaBlock> blocks = body.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); ++blockId) {
            // We'll see if block #i is a good candidate for being the StartBlock
            SsaBlock candidateStartBlock = blocks.get(blockId);
            Optional<SsaInstruction> candidateStartLastInstruction = candidateStartBlock.getEndingInstruction();
            if (!candidateStartLastInstruction.isPresent()) {
                continue;
            }
            SsaInstruction startLastInstruction = candidateStartLastInstruction.get();
            if (!(startLastInstruction instanceof ForInstruction)) {
                continue;
            }

            ForInstruction firstForInstruction = (ForInstruction) startLastInstruction;
            int candidateMiddleBlockId = firstForInstruction.getEndBlock();
            SsaBlock candidateMiddleBlock = blocks.get(candidateMiddleBlockId);

            Optional<SsaInstruction> candidateMiddleBlockLastInstruction = candidateMiddleBlock.getEndingInstruction();
            if (!candidateMiddleBlockLastInstruction.isPresent()) {
                continue;
            }
            SsaInstruction middleBlockLastInstruction = candidateMiddleBlockLastInstruction.get();
            if (!(middleBlockLastInstruction instanceof ForInstruction)) {
                continue;
            }

            ForInstruction secondForInstruction = (ForInstruction) middleBlockLastInstruction;

            // We found a set of loops that matches the basic format.
            if (tryCombineLoops(body,
                    providerData,
                    typeGetter,
                    makeTemporary,
                    passData,
                    blockId,
                    firstForInstruction,
                    secondForInstruction,
                    info)) {
                return true;
            }
        }

        log("No loops left to combine");
        return false;
    }

    private static boolean tryCombineLoops(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData,
            int blockId,
            ForInstruction firstForInstruction,
            ForInstruction secondForInstruction,
            SizeGroupInformation info) {

        List<LoopProperty> combinedProperties = combineLoopProperties(firstForInstruction, secondForInstruction);
        if (combinedProperties == null) {
            return false;
        }

        // First of all, let's make sure none of the loop blocks have side-effects
        int firstLoopBlockId = firstForInstruction.getLoopBlock();
        boolean firstLoopHasSideEffects = BlockUtils.hasNestedSideEffects(body, firstLoopBlockId);
        if (firstLoopHasSideEffects) {
            log("First loop has side effects");
            // We might be able to proceed anyway
        }
        if (!BlockUtils.isSimpleSection(body, firstLoopBlockId)) {
            log("First loop is not simple.");
            return false;
        }

        int secondLoopBlockId = secondForInstruction.getLoopBlock();
        boolean secondLoopHasSideEffects = BlockUtils.hasNestedSideEffects(body, secondLoopBlockId);
        if (secondLoopHasSideEffects) {
            log("Second loop has side-effects");
            // We might be able to proceed anyway
        }
        if (!BlockUtils.isSimpleSection(body, secondLoopBlockId)) {
            log("Second loop is not simple.");
            return false;
        }

        if (firstLoopHasSideEffects && secondLoopHasSideEffects) {
            log("Can't fuse loops: Both have side effects");
            return false;
        }

        // Let's examine the first loop
        FirstLoopInformation firstLoopInformation = examineFirstLoop(
                body,
                firstForInstruction,
                typeGetter,
                blockId);

        if (!firstLoopInformation.isLegal) {
            log("Can't fuse loops due to problems in the first loop");
            return false;
        }

        MiddleBlockInformation middleBlockInformation = examineMiddleBlock(
                body.getBlock(firstForInstruction.getEndBlock()),
                info,
                firstLoopInformation,
                firstLoopHasSideEffects,
                secondLoopHasSideEffects,
                typeGetter,
                blockId,
                firstLoopBlockId);
        if (!middleBlockInformation.isLegal) {
            log("Can't fuse loops due to problems in the middle block");
            return false;
        }

        if (firstLoopHasSideEffects) {
            for (SsaInstruction prefix : middleBlockInformation.prefixInstructions) {
                if (prefix.getInstructionType() == InstructionType.HAS_SIDE_EFFECT
                        || prefix.getInstructionType() == InstructionType.HAS_VALIDATION_SIDE_EFFECT) {
                    log("Can't fuse loops: Since first loop has side effects, instruction " + prefix
                            + " (that also has side effects) can't be moved before it");
                    return false;
                }
            }
        }
        if (secondLoopHasSideEffects) {
            for (SsaInstruction postfix : middleBlockInformation.postfixInstructions) {
                if (postfix.getInstructionType() == InstructionType.HAS_SIDE_EFFECT
                        || postfix.getInstructionType() == InstructionType.HAS_VALIDATION_SIDE_EFFECT) {
                    log("Can't fuse loops: Since second loop has side effects, instruction " + postfix
                            + " (that also has side effects) can't be moved after it");
                    return false;
                }
            }
        }

        SecondLoopInformation secondLoopInformation = validateSecondBlock(body,
                secondForInstruction,
                firstLoopInformation,
                middleBlockInformation,
                info);
        if (secondLoopInformation.isLegal) {
            if (firstLoopInformation.starts.size() == secondLoopInformation.starts.size()) {
                for (int i = 0; i < firstLoopInformation.starts.size(); ++i) {
                    if (!info.areSameValue(firstLoopInformation.starts.get(i), secondLoopInformation.starts.get(i))) {
                        log("Can't fuse loops: Can't prove both loops start at same point: "
                                + firstForInstruction.getStart()
                                + ", "
                                + secondForInstruction.getStart());
                        return false;
                    }

                    if (!info.areSameValue(firstLoopInformation.intervals.get(i),
                            secondLoopInformation.intervals.get(i))) {
                        log("Can't fuse loops: Can't prove both loops have same interval");
                        return false;
                    }

                    String end1 = firstLoopInformation.ends.get(i);
                    String end2 = secondLoopInformation.ends.get(i);
                    if (!info.areSameValue(end1, end2)) {
                        log("Can't fuse loops: Can't prove both loops end at same point (" + end1 + ", " + end2 + ")");
                        return false;
                    }
                }

                if (!PassUtils.skipPass(body, "loop_fusion_only_related")
                        && !areLoopsRelated(firstLoopInformation, middleBlockInformation, secondLoopInformation)) {
                    log("Not fusing loops, because they are unrelated.");
                    return false;
                }

                log("Merging loops");
                combineLoops(body,
                        blockId,
                        combinedProperties,
                        firstForInstruction,
                        secondForInstruction,
                        info,
                        firstLoopInformation,
                        middleBlockInformation,
                        secondLoopInformation);

                return true;
            }

            log("Unrecognized loop fusion pattern: Access pattern matches but loops have different number of dimensions.");
            return false;
        }

        if (firstLoopInformation.starts.size() > 1) {
            log("Trying merge of loops with different dimensions.");

            if (!combinedProperties.isEmpty()) {
                log("Don't know how to fuse loops with different dimensions that have properties.");
                return false;
            }

            return tryDifferentDimensionMerge(body,
                    blockId,
                    firstForInstruction,
                    secondForInstruction,
                    info,
                    firstLoopInformation,
                    middleBlockInformation,
                    makeTemporary,
                    typeGetter);
        }

        return false;
    }

    private static List<LoopProperty> combineLoopProperties(
            ForInstruction firstForInstruction,
            ForInstruction secondForInstruction) {

        List<LoopProperty> loopProperties = new ArrayList<>();

        List<LoopProperty> properties1 = firstForInstruction.getLoopProperties();
        List<LoopProperty> properties2 = new ArrayList<>(secondForInstruction.getLoopProperties());

        for (LoopProperty property1 : properties1) {

            boolean foundProperty = false;
            for (int i = 0; i < properties2.size(); ++i) {
                LoopProperty property2 = properties2.get(i);

                if (property1.getClass().equals(property2.getClass())) {
                    FusionResult result = property1.getFusionResultWith(property2);
                    if (!result.allowFusion()) {
                        log("Can't fuse loop due to " + property1 + "/" + property2);
                        return null;
                    }

                    result.getFusedProperty().ifPresent(loopProperties::add);
                    properties2.remove(i);
                    foundProperty = true;
                    break;
                }
            }

            if (!foundProperty) {
                FusionResult result = property1.getFusionResultWithoutEquivalent();
                if (!result.allowFusion()) {
                    log("Can't fuse loop due to " + property1);
                    return null;
                }

                result.getFusedProperty().ifPresent(loopProperties::add);
            }
        }

        // No remaining properties in properties2 have a firstLoop counterpart
        for (LoopProperty property2 : properties2) {
            FusionResult result = property2.getFusionResultWithoutEquivalent();
            if (!result.allowFusion()) {
                log("Can't fuse loop due to " + property2);
                return null;
            }

            result.getFusedProperty().ifPresent(loopProperties::add);
        }

        return loopProperties;
    }

    private static boolean areLoopsRelated(
            FirstLoopInformation firstLoopInformation,
            MiddleBlockInformation middleInformation,
            SecondLoopInformation secondLoopInformation) {

        for (String usedMatrix : firstLoopInformation.readMatrices) {
            if (secondLoopInformation.readMatrices.contains(usedMatrix)) {
                return true;
            }
        }

        for (String builtMatrix : middleInformation.cooperativelyBuiltMatrices.keySet()) {
            if (secondLoopInformation.readMatrices.contains(builtMatrix)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryDifferentDimensionMerge(FunctionBody body,
            int blockId,
            ForInstruction firstForInstruction,
            ForInstruction secondForInstruction,
            SizeGroupInformation info,
            FirstLoopInformation firstLoopInformation,
            MiddleBlockInformation middleBlockInformation,
            BiFunction<String, VariableType, String> makeTemporary,
            Function<String, Optional<VariableType>> typeGetter) {

        for (int i = 0; i < firstLoopInformation.starts.size() - 1; ++i) {
            assert firstLoopInformation.startBlockId.get(i) != firstLoopInformation.endBlockId
                    .get(i) : "Invalid block ID specification:\nStarts:" + firstLoopInformation.startBlockId
                            + "\nEnds: " + firstLoopInformation.endBlockId;
        }

        for (String start : firstLoopInformation.starts) {
            if (!info.areSameValue(start, 1)) {
                log("This type of fusion only applies to loops starting at 1.");
                return false;
            }
        }
        for (String interval : firstLoopInformation.intervals) {
            if (!info.areSameValue(interval, 1)) {
                log("This type of fusion only applies to loops with intervals of 1.");
                return false;
            }
        }

        if (!info.areSameValue(secondForInstruction.getStart(), 1)) {
            log("This type of fusion only applies to loops starting at 1.");
            return false;
        }

        if (!info.areSameValue(secondForInstruction.getInterval(), 1)) {
            log("This type of fusion only applies to loops with intervals of 1.");
            return false;
        }

        int secondLoopBlockId = secondForInstruction.getLoopBlock();
        SsaBlock secondLoopBlock = body.getBlock(secondLoopBlockId);

        if (secondLoopBlock.getEndingInstruction().isPresent()) {
            log("Second for loop is not in the expected format.");
            return false;
        }

        SsaBlock startBlock = body.getBlock(blockId);
        SsaBlock firstLoopStart = body.getBlock(firstForInstruction.getLoopBlock());
        int middleBlockId = firstForInstruction.getEndBlock();
        SsaBlock middleBlock = body.getBlock(middleBlockId);
        SsaBlock endBlock = body.getBlock(secondForInstruction.getEndBlock());

        int innerMostLoopBlockId = SpecsCollections.last(firstLoopInformation.startBlockId);
        SsaBlock innerMostLoopBlock = body.getBlock(innerMostLoopBlockId);

        String iterVariable = null;
        Set<SimpleGetInstruction> getsWithIter = new HashSet<>();
        Set<SimpleSetInstruction> setsWithIter = new HashSet<>();
        Set<String> setMatrices = new HashSet<>();
        Map<String, String> sourceMatrix = new HashMap<>();
        Map<String, String> generatedMatrix = new HashMap<>();

        log("Blacklisted names: " + middleBlockInformation.blacklistedNames);

        for (SsaInstruction instruction : secondLoopBlock.getInstructions()) {
            for (String input : instruction.getInputVariables()) {
                if (middleBlockInformation.blacklistedNames.contains(input)) {
                    log("Second loop uses blacklisted variable " + input);
                    return false;
                }
            }

            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                String output = phi.getOutput();

                int prevIndex = phi.getSourceBlocks().indexOf(middleBlockId);
                int activeIndex = phi.getSourceBlocks().indexOf(secondForInstruction.getLoopBlock());

                sourceMatrix.put(output, phi.getInputVariables().get(prevIndex));
                generatedMatrix.put(output, phi.getInputVariables().get(activeIndex));
            }

            if (instruction instanceof IterInstruction) {
                if (iterVariable != null) {
                    log("Found multiple iter instructions");
                    return false;
                }

                iterVariable = ((IterInstruction) instruction).getOutput();
                continue;
            }

            if (instruction.getInputVariables().contains(iterVariable)) {
                if (instruction instanceof SimpleGetInstruction) {
                    getsWithIter.add((SimpleGetInstruction) instruction);
                    continue;
                }
                if (instruction instanceof SimpleSetInstruction) {
                    setsWithIter.add((SimpleSetInstruction) instruction);
                    continue;
                }
                log("In fusion of loops with different dimensions, the 1D loop can't use the iter variable outside of memory accesses: "
                        + instruction);
                return false;
            }
        }

        if (getsWithIter.isEmpty()) {
            log("No gets in loop");
            return false;
        }

        Set<String> sizeValidation = new HashSet<>();

        for (SimpleGetInstruction get : getsWithIter) {
            if (get.getIndices().size() != 1) {
                log("Unrecognized pattern: Gets must have a single index, instead got " + get);
                return false;
            }

            MatrixAccessPattern originalPattern = middleBlockInformation.cooperativelyBuiltMatrices
                    .get(get.getInputMatrix());
            if (originalPattern != null) {
                if (originalPattern.numIndices() < firstLoopInformation.starts.size()) {
                    log("Unrecognized set pattern: Source matrix " + get.getInputMatrix()
                            + " must be constructed using all the indices, got " + originalPattern.toString());
                    return false;
                }

                for (int i = 0; i < firstLoopInformation.starts.size(); ++i) {
                    MatrixIndex index = originalPattern.getIndexAt(i);
                    if (index.getType() != MatrixIndex.IndexType.ITER
                            || index.getDepth() != firstLoopInformation.starts.size() - i - 1) {
                        log("Unrecognized set pattern: Source matrix " + get.getInputMatrix()
                                + " should be constructed using the indices, in a reversed sequence. Instead got " + get
                                + ", with index " + index);
                        return false;
                    }
                }

                for (int i = firstLoopInformation.starts.size(); i < originalPattern.numIndices(); ++i) {
                    MatrixIndex index = originalPattern.getIndexAt(i);
                    if (index.getType() != MatrixIndex.IndexType.VARIABLE) {
                        log("Unrecognized set pattern: Source matrix " + get.getInputMatrix()
                                + " should be constructed using the indices, in sequence. Additional indices should be 1, got "
                                + index);
                        return false;
                    }

                    String var = index.getVar();
                    if (!info.areSameValue(var, 1)) {
                        log("Unrecognized set pattern: Source matrix " + get.getInputMatrix()
                                + " should be constructed using the indices, in sequence. Additional indices should be 1, got "
                                + index);
                        return false;
                    }
                }
            }

            sizeValidation.add(get.getInputMatrix());
        }
        for (SimpleSetInstruction set : setsWithIter) {
            if (set.getIndices().size() != 1) {
                log("Unrecognized pattern: Sets must have a single index, instead got " + set);
                return false;
            }
            String inputMatrix = set.getInputMatrix();
            if (!generatedMatrix.containsKey(inputMatrix)) {
                log("Unrecognized pattern: Sets not referencing phi node directly.");
                return false;
            }

            if (!generatedMatrix.get(inputMatrix).equals(set.getOutput())) {
                log("Unrecognized pattern: Set output is not used in the previous corresponding phi node.");
                return false;
            }

            setMatrices.add(inputMatrix);
            sizeValidation.add(inputMatrix);
        }

        int numDims = firstLoopInformation.ends.size();
        for (int dim = 0; dim < numDims; ++dim) {
            String end = firstLoopInformation.ends.get(numDims - dim - 1);

            for (String matrix : sizeValidation) {
                String size = dim == numDims - 1 ? info.getSizeSinceResult(matrix, dim)
                        : info.getSizeResult(matrix, dim);

                if (!info.areSameValue(end, size)) {
                    log("Can't prove size of " + matrix + " at dimension " + dim + " of "
                            + numDims + " is " + end);
                    info.logAll();
                    return false;
                }
            }
        }

        String expectedNumel = secondForInstruction.getEnd();
        // At this point, we know that all matrices have the same size.
        // All we need to do is prove that *any* matrix has numel 'expectedNumel'.
        boolean hasCorrectNumel = false;
        for (String matrix : sizeValidation) {
            if (info.areSameValue(info.getNumelResult(matrix), expectedNumel)) {
                hasCorrectNumel = true;
                break;
            }
        }

        if (!hasCorrectNumel) {
            log("Could not prove that matrices have numel " + expectedNumel);
            return false;
        }

        log("Combining the two loops");

        renamePrefixInstructionVariables(firstLoopInformation, middleBlockInformation);
        startBlock.getInstructions().addAll(startBlock.getInstructions().indexOf(firstForInstruction),
                middleBlockInformation.prefixInstructions);

        List<SsaInstruction> postfixPhiSegmentInstructions = new ArrayList<>();
        List<SsaInstruction> postfixNonPhiInstructions = new ArrayList<>();
        boolean insertedNonPhi = false;

        for (SsaInstruction postfixInstruction : middleBlockInformation.postfixInstructions) {

            // Line -> phi and non-phi segments
            // Phi -> phi segment only
            // Other -> non-phi segment only

            if (postfixInstruction instanceof LineInstruction || postfixInstruction instanceof PhiInstruction) {
                postfixPhiSegmentInstructions.add(postfixInstruction);
            }
            if (!(postfixInstruction instanceof PhiInstruction)) {
                insertedNonPhi |= !(postfixInstruction instanceof LineInstruction);

                postfixNonPhiInstructions.add(postfixInstruction);
            }
        }

        endBlock.prependInstructions(postfixPhiSegmentInstructions);
        if (insertedNonPhi) {
            int insertionPoint = BlockUtils.getAfterPhiInsertionPoint(endBlock);

            endBlock.insertInstructions(insertionPoint, postfixNonPhiInstructions);
        }

        Map<String, List<String>> entryTemporaries = new HashMap<>();
        Map<String, List<String>> exitTemporaries = new HashMap<>();

        for (String var : setMatrices) {
            // Since the "dimensionality" of the second loop is changing, we need to add
            // new phi nodes and temporaries to cover the extra loops.

            List<String> varEntryTemporaries = new ArrayList<>();
            entryTemporaries.put(var, varEntryTemporaries);
            List<String> varExitTemporaries = new ArrayList<>();
            exitTemporaries.put(var, varExitTemporaries);

            String proposedName = NameUtils.getSuggestedName(var);
            VariableType type = typeGetter.apply(var).get();

            for (int depth = 0; depth < firstLoopInformation.starts.size() - 1; ++depth) {
                varEntryTemporaries.add(makeTemporary.apply(proposedName, type));
                varExitTemporaries.add(makeTemporary.apply(proposedName, type));
            }

            varEntryTemporaries.add(sourceMatrix.get(var));
            varExitTemporaries.add(generatedMatrix.get(var));

            for (int depth = 0; depth < firstLoopInformation.startBlockId.size() - 1; ++depth) {
                int parentBlockId = firstLoopInformation.parentBlockId.get(depth);
                int startBlockId = firstLoopInformation.startBlockId.get(depth);
                int endBlockId = firstLoopInformation.endBlockId.get(depth);
                int nestedLoopEndBlockId = firstLoopInformation.endBlockId.get(depth + 1);

                String beforeLoopVar = depth == 0 ? sourceMatrix.get(var) : varEntryTemporaries.get(depth - 1);
                String loopStartVar = varEntryTemporaries.get(depth);
                String loopEndVar = varExitTemporaries.get(depth);
                String nestedLoopEndVar = varExitTemporaries.get(depth + 1);

                PhiInstruction startPhi = new PhiInstruction(loopStartVar, Arrays.asList(beforeLoopVar, loopEndVar),
                        Arrays.asList(parentBlockId, endBlockId));
                body.getBlock(startBlockId).prependInstruction(startPhi);
                PhiInstruction endPhi = new PhiInstruction(loopEndVar, Arrays.asList(loopStartVar, nestedLoopEndVar),
                        Arrays.asList(startBlockId, nestedLoopEndBlockId));
                body.getBlock(endBlockId).prependInstruction(endPhi);
            }
        }

        // We still need to place all the inner instructions in the first for loop
        int innerMostContainerStartId = firstLoopInformation.startBlockId
                .get(firstLoopInformation.startBlockId.size() - 2);
        for (SsaInstruction instruction : secondLoopBlock.getInstructions()) {
            instruction.renameBlocks(Arrays.asList(middleBlockId, secondLoopBlockId),
                    Arrays.asList(innerMostContainerStartId, SpecsCollections.last(firstLoopInformation.endBlockId)));

            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                String phiOutput = phi.getOutput();
                List<String> varEntryTemporaries = entryTemporaries.get(phiOutput);

                Map<String, String> newNames = new HashMap<>();
                newNames.put(SpecsCollections.last(varEntryTemporaries),
                        varEntryTemporaries.get(varEntryTemporaries.size() - 2));

                instruction.renameVariables(newNames);
                innerMostLoopBlock.prependInstruction(instruction);
            } else if (instruction instanceof IterInstruction) {
                handleIter(firstLoopInformation, firstLoopStart, instruction);
            } else if (instruction instanceof SimpleGetInstruction) {
                SimpleGetInstruction originalGet = (SimpleGetInstruction) instruction;
                String inputMatrix = originalGet.getInputMatrix();

                MatrixAccessPattern pattern = middleBlockInformation.cooperativelyBuiltMatrices.get(inputMatrix);
                if (pattern != null) {
                    inputMatrix = pattern.getSetMatrix();
                }

                List<String> iters = new ArrayList<>(firstLoopInformation.iterVariable);
                Collections.reverse(iters);
                SimpleGetInstruction newGet = new SimpleGetInstruction(originalGet.getOutput(),
                        inputMatrix, iters);

                innerMostLoopBlock.addInstruction(newGet);
            } else if (instruction instanceof SimpleSetInstruction) {
                SimpleSetInstruction originalSet = (SimpleSetInstruction) instruction;
                List<String> iters = new ArrayList<>(firstLoopInformation.iterVariable);
                Collections.reverse(iters);
                SimpleSetInstruction newSet = new SimpleSetInstruction(originalSet.getOutput(),
                        originalSet.getInputMatrix(), iters, originalSet.getValue());

                innerMostLoopBlock.addInstruction(newSet);
            } else {
                innerMostLoopBlock.addInstruction(instruction);
            }
        }
        for (SsaInstruction endInstruction : endBlock.getInstructions()) {
            if (endInstruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) endInstruction;

                if (phi.getSourceBlocks().contains(middleBlockId)) {
                    int loopIndex = phi.getSourceBlocks().indexOf(secondLoopBlockId);
                    String loopName = phi.getInputVariables().get(loopIndex);

                    phi.renameBlocks(Arrays.asList(middleBlockId, secondLoopBlockId),
                            Arrays.asList(firstLoopInformation.parentBlockId.get(0),
                                    firstLoopInformation.endBlockId.get(0)));

                    String newName = null;
                    for (List<String> values : exitTemporaries.values()) {
                        if (SpecsCollections.last(values).equals(loopName)) {
                            newName = values.get(0);
                            break;
                        }
                    }
                    assert newName != null;

                    Map<String, String> newNames = new HashMap<>();
                    newNames.put(loopName, newName);
                    phi.renameVariables(newNames);
                }
            }
        }

        // endBlock.renameBlocks(Arrays.asList(middleBlockId, secondLoopBlockId),
        // Arrays.asList(firstLoopInformation.parentBlockId.get(0),
        // firstLoopInformation.endBlockId.get(0)));

        firstForInstruction.renameBlocks(
                Arrays.asList(middleBlockId),
                Arrays.asList(secondForInstruction.getEndBlock()));

        middleBlock.getInstructions().clear();
        secondLoopBlock.getInstructions().clear();

        return true;
    }

    private static SecondLoopInformation validateSecondBlock(FunctionBody body,
            ForInstruction xfor,
            FirstLoopInformation firstLoopInformation,
            MiddleBlockInformation middleBlockInformation,
            SizeGroupInformation sizes) {

        SecondLoopInformation info = new SecondLoopInformation();
        info.isLegal = true;
        info.starts = new ArrayList<>();
        info.intervals = new ArrayList<>();
        info.ends = new ArrayList<>();
        info.iterVariable = new ArrayList<>();
        info.endBlockId = new ArrayList<>();
        info.readMatrices = new HashSet<>();

        return validateSecondBlock(body, xfor, firstLoopInformation, middleBlockInformation, sizes, info);
    }

    private static SecondLoopInformation validateSecondBlock(FunctionBody body,
            ForInstruction xfor,
            FirstLoopInformation firstLoopInformation,
            MiddleBlockInformation middleBlockInformation,
            SizeGroupInformation sizes,
            SecondLoopInformation info) {

        info.starts.add(xfor.getStart());
        info.intervals.add(xfor.getInterval());
        info.ends.add(xfor.getEnd());
        info.endBlockId.add(xfor.getLoopBlock());
        info.iterVariable.add(null);

        int nextBlockId = xfor.getLoopBlock();

        while (nextBlockId >= 0) {
            int currentBlockId = nextBlockId;
            SsaBlock currentBlock = body.getBlock(currentBlockId);
            nextBlockId = -1;

            for (SsaInstruction instruction : currentBlock.getInstructions()) {
                for (String blacklistedVariable : middleBlockInformation.blacklistedNames) {
                    if (instruction.getInputVariables().contains(blacklistedVariable)) {
                        log("[validate second block] Using blacklisted variable: " + blacklistedVariable);
                        info.isLegal = false;
                        return info;
                    }
                }

                SecondLoopInformation capturedInfo = info;
                getReadMatrix(instruction).ifPresent(matrixName -> {
                    capturedInfo.readMatrices.add(matrixName);
                });

                if (instruction instanceof SimpleGetInstruction) {
                    SimpleGetInstruction get = (SimpleGetInstruction) instruction;

                    MatrixAccessPattern basePattern = middleBlockInformation.cooperativelyBuiltMatrices
                            .get(get.getInputMatrix());
                    if (basePattern == null || !basePattern.hasIndices()) {
                        continue;
                    }
                    if (basePattern.numIndices() != get.getIndices().size()) {
                        log("[validate second block] Access pattern does not match");
                        info.isLegal = false;
                        return info;
                    }

                    for (int i = 0; i < get.getIndices().size(); ++i) {
                        String index = get.getIndices().get(i);
                        MatrixIndex baseIndex = basePattern.getIndexAt(i);

                        switch (baseIndex.getType()) {
                        case ITER:
                            if (info.iterVariable.size() <= baseIndex.getDepth()) {
                                log("Don't know how to handle this case");
                                info.isLegal = false;
                                return info;
                            }

                            if (!index.equals(info.iterVariable.get(baseIndex.getDepth()))) {
                                log("[validate second block] Access pattern does not match. Expected iteration, got "
                                        + index);
                                info.isLegal = false;
                                return info;
                            }
                            break;
                        case VARIABLE:
                            if (!sizes.areSameValue(index, baseIndex.getVar())) {
                                log("[validate second block] Access pattern does not match. Could not prove that "
                                        + index + " == "
                                        + baseIndex.getVar());
                                info.isLegal = false;
                                return info;
                            }
                            break;
                        default:
                            assert false;
                        }
                    }
                    continue;
                }

                for (String cooperativelyBuiltMatrix : middleBlockInformation.cooperativelyBuiltInstructions.keySet()) {
                    if (instruction.getInputVariables().contains(cooperativelyBuiltMatrix)) {
                        log("[validate second block] Using cooperatively built matrix outside of simple get: "
                                + instruction);
                        info.isLegal = false;
                        return info;
                    }
                }

                if (instruction instanceof IterInstruction) {
                    IterInstruction iter = (IterInstruction) instruction;
                    if (SpecsCollections.last(info.iterVariable) == null) {
                        info.iterVariable.set(info.iterVariable.size() - 1, iter.getOutput());
                        continue;
                    }

                    log("[validate second block] Multiple iter instructions");
                    info.isLegal = false;
                    return info;
                }

                if (instruction instanceof ForInstruction) {
                    ForInstruction nestedFor = (ForInstruction) instruction;

                    info.endBlockId.set(info.endBlockId.size() - 1, nestedFor.getEndBlock());

                    SecondLoopInformation nestedInfo = info.copy();
                    nestedInfo = validateSecondBlock(body, nestedFor, firstLoopInformation, middleBlockInformation,
                            sizes,
                            nestedInfo);

                    if (nestedInfo.isLegal) {
                        info = nestedInfo;
                        nextBlockId = nestedFor.getEndBlock();
                        continue;
                    }

                    log("[validate second block] Nested for instruction in second for is not in the expected format.");
                    info.isLegal = false;
                    return info;
                }

                if (instruction.getOwnedBlocks().size() != 0) {
                    log("[validate second block] Not supported. Blocks in second loop");
                    info.isLegal = false;
                    return info;
                }
            }
        }

        return info;
    }

    private static void combineLoops(FunctionBody body,
            int startBlockId,
            List<LoopProperty> combinedProperties,
            ForInstruction firstForInstruction,
            ForInstruction secondForInstruction,
            SizeGroupInformation sizeGroupInformation,
            FirstLoopInformation firstLoopInformation,
            MiddleBlockInformation middleBlockInformation,
            SecondLoopInformation secondLoopInformation) {

        SsaBlock startBlock = body.getBlock(startBlockId);

        SsaBlock firstLoopStart = body.getBlock(firstLoopInformation.startBlockId.get(0));
        SsaBlock firstLoopEnd = body.getBlock(firstLoopInformation.endBlockId.get(0));

        int middleBlockId = firstForInstruction.getEndBlock();
        SsaBlock middleBlock = body.getBlock(middleBlockId);

        int secondLoopBlockId = secondForInstruction.getLoopBlock();
        SsaBlock secondLoop = body.getBlock(secondLoopBlockId);

        int endBlockId = secondForInstruction.getEndBlock();
        SsaBlock endBlock = body.getBlock(endBlockId);

        Map<String, String> newNames = new HashMap<>(middleBlockInformation.cooperativelyBuiltInstructions);
        if (firstLoopInformation.iterVariable.get(0) != null && secondLoopInformation.iterVariable.get(0) != null) {
            newNames.put(secondLoopInformation.iterVariable.get(0), firstLoopInformation.iterVariable.get(0));
        }
        BlockUtils.renameVariablesNested(body, secondLoopBlockId, newNames);

        List<SsaInstruction> phiNodes = new ArrayList<>();
        List<SsaInstruction> nonPhiNodes = new ArrayList<>();

        int newSecondLoopEnd = secondLoopInformation.endBlockId.get(0);
        if (newSecondLoopEnd == secondLoopBlockId) {
            newSecondLoopEnd = firstLoopInformation.endBlockId.get(0);
        }

        for (SsaInstruction instruction : secondLoop.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                // Make them look like the already existent phi nodes, which haven't been fixed yet.
                phi.renameBlocks(Arrays.asList(middleBlockId, secondLoopInformation.endBlockId.get(0)),
                        Arrays.asList(startBlockId, firstLoopInformation.endBlockId.get(0)));

                phiNodes.add(phi);
            } else if (instruction instanceof IterInstruction) {
                handleIter(firstLoopInformation, firstLoopStart, instruction);
            } else {
                nonPhiNodes.add(instruction);
            }
        }

        firstLoopStart.prependInstructions(phiNodes);
        firstLoopEnd.addInstructions(nonPhiNodes);

        ForInstruction firstFor = (ForInstruction) startBlock.getEndingInstruction().get();
        firstFor.replaceProperties(combinedProperties);

        renamePrefixInstructionVariables(firstLoopInformation, middleBlockInformation);
        startBlock.getInstructions().addAll(startBlock.getInstructions().indexOf(firstFor),
                middleBlockInformation.prefixInstructions);

        int phiInjectionPoint = 0;
        int nonPhiInjectionPoint = 0;
        for (int i = 0; i < endBlock.getInstructions().size(); ++i) {
            if (endBlock.getInstructions().get(i) instanceof PhiInstruction) {
                nonPhiInjectionPoint = i + 1;
            }
        }

        for (SsaInstruction instruction : middleBlockInformation.postfixInstructions) {
            if (instruction instanceof PhiInstruction) {
                endBlock.insertInstruction(phiInjectionPoint, instruction);
                ++phiInjectionPoint;
                ++nonPhiInjectionPoint;
            } else {
                endBlock.insertInstruction(nonPhiInjectionPoint, instruction);
                ++nonPhiInjectionPoint;
            }
        }

        secondLoop.getInstructions().clear();
        middleBlock.getInstructions().clear();

        firstFor.renameBlocks(Arrays.asList(middleBlockId), Arrays.asList(endBlockId));
        body.renameBlocks(Arrays.asList(secondForInstruction.getLoopBlock()),
                Arrays.asList(firstLoopInformation.endBlockId.get(0)));
        endBlock.renameBlocks(Arrays.asList(middleBlockId, firstLoopInformation.endBlockId.get(0)),
                Arrays.asList(startBlockId, newSecondLoopEnd));
        firstLoopStart.breakBlock(firstLoopInformation.endBlockId.get(0),
                firstLoopInformation.endBlockId.get(0),
                newSecondLoopEnd);
    }

    private static void handleIter(FirstLoopInformation firstLoopInformation, SsaBlock firstLoopStart,
            SsaInstruction instruction) {
        if (firstLoopInformation.iterVariable.get(0) == null) {
            firstLoopStart.insertInstruction(BlockUtils.getAfterPhiInsertionPoint(firstLoopStart), instruction);
        } else {
            // Use existing iter
        }
    }

    private static void renamePrefixInstructionVariables(FirstLoopInformation firstLoopInformation,
            MiddleBlockInformation middleBlockInformation) {
        for (SsaInstruction instruction : middleBlockInformation.prefixInstructions) {
            Map<String, String> newCooperativelyBuildMatrixNames = new HashMap<>();

            for (String finalName : middleBlockInformation.cooperativelyBuiltInstructions.keySet()) {
                String finalLoopName = middleBlockInformation.cooperativelyBuiltInstructions.get(finalName);
                MatrixAccessPattern pattern = firstLoopInformation.cooperativelyBuiltMatrices.get(finalLoopName);
                String initialName = pattern.getSourceMatrix();

                newCooperativelyBuildMatrixNames.put(finalName, initialName);
            }

            instruction.renameVariables(newCooperativelyBuildMatrixNames);
        }
    }

    private static FirstLoopInformation examineFirstLoop(FunctionBody body,
            ForInstruction forInstruction,
            Function<String, Optional<VariableType>> typeGetter,
            int startBlockId) {

        FirstLoopInformation loopInformation = new FirstLoopInformation();
        loopInformation.isLegal = true;
        loopInformation.parentBlockId = new ArrayList<>();
        loopInformation.startBlockId = new ArrayList<>();
        loopInformation.endBlockId = new ArrayList<>();
        loopInformation.starts = new ArrayList<>();
        loopInformation.intervals = new ArrayList<>();
        loopInformation.ends = new ArrayList<>();
        loopInformation.iterVariable = new ArrayList<>();
        loopInformation.readMatrices = new HashSet<>();

        return examineFirstLoop(body, forInstruction, typeGetter, loopInformation, null, startBlockId, 0);

    }

    private static FirstLoopInformation examineFirstLoop(FunctionBody body,
            ForInstruction forInstruction,
            Function<String, Optional<VariableType>> typeGetter,
            FirstLoopInformation loopInformation,
            Map<String, MatrixAccessPattern> candidateCooperativelyBuiltMatrices,
            int startBlockId,
            int depth) {

        int loopBlockId = forInstruction.getLoopBlock();

        loopInformation.startBlockId.add(loopBlockId);
        loopInformation.endBlockId.add(loopBlockId);
        loopInformation.parentBlockId.add(startBlockId);
        loopInformation.starts.add(forInstruction.getStart());
        loopInformation.intervals.add(forInstruction.getInterval());
        loopInformation.ends.add(forInstruction.getEnd());
        loopInformation.iterVariable.add(null);

        if (candidateCooperativelyBuiltMatrices == null) {
            candidateCooperativelyBuiltMatrices = new HashMap<>();
        }

        List<PhiInstruction> startMatrixPhiInstructions = new ArrayList<>();
        Map<String, MatrixAccessPattern> childBuiltMatrices = null;

        int nextBlockId = loopBlockId;
        boolean isStart = true;
        boolean afterNestedLoop = false;
        int nestedLoopParent = -1;

        while (nextBlockId >= 0) {
            int currentBlockId = nextBlockId;
            SsaBlock currentBlock = body.getBlock(currentBlockId);
            nextBlockId = -1;

            loopInformation.endBlockId.set(depth, currentBlockId);

            for (SsaInstruction instruction : currentBlock.getInstructions()) {
                if (instruction instanceof IterInstruction) {
                    IterInstruction iter = (IterInstruction) instruction;

                    assert isStart;

                    if (SpecsCollections.last(loopInformation.iterVariable) != null) {
                        log("Multiple iter variables");
                        loopInformation.isLegal = false;
                        return loopInformation;
                    }

                    loopInformation.iterVariable.set(loopInformation.iterVariable.size() - 1, iter.getOutput());
                    continue;
                }
                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    String output = phi.getOutput();
                    Optional<VariableType> outputType = typeGetter.apply(output);
                    if (MatrixUtils.isMatrix(outputType)) {
                        if (isStart) {
                            startMatrixPhiInstructions.add(phi);
                            candidateCooperativelyBuiltMatrices.put(output,
                                    new MatrixAccessPattern(
                                            phi.getInputVariables().get(phi.getSourceBlocks().indexOf(startBlockId)),
                                            null, null));
                        } else if (afterNestedLoop) {
                            log("New phi");

                            int beforeLoopIndex = phi.getSourceBlocks().indexOf(nestedLoopParent);
                            if (beforeLoopIndex >= 0) {

                                String beforeLoopVariable = phi.getInputVariables().get(beforeLoopIndex);
                                String loopEndVariable = phi.getInputVariables().get(beforeLoopIndex == 0 ? 1 : 0);

                                assert childBuiltMatrices != null;

                                MatrixAccessPattern outerPattern = candidateCooperativelyBuiltMatrices
                                        .get(beforeLoopVariable);
                                MatrixAccessPattern loopPattern = childBuiltMatrices.get(loopEndVariable);
                                if (loopPattern != null && loopPattern.getBuiltMatrix() != null &&
                                        outerPattern != null && outerPattern.getBuiltMatrix() == null) {

                                    String sourceMatrix = outerPattern.getSourceMatrix();

                                    MatrixAccessPattern newPattern = new MatrixAccessPattern(
                                            sourceMatrix, output, loopPattern.getSetMatrix());
                                    newPattern.setIndices(loopPattern);
                                    candidateCooperativelyBuiltMatrices.put(output, newPattern);
                                } else {
                                    log("Ignoring " + output + ": Unrecognized format");
                                }

                            } else {
                                // Although we are after the nested loop, we are not *immediately* after it.
                            }
                        }
                    }

                    continue;
                }

                if (instruction instanceof SimpleSetInstruction && !afterNestedLoop) {
                    SimpleSetInstruction simpleSet = (SimpleSetInstruction) instruction;

                    String inputMatrix = simpleSet.getInputMatrix();
                    List<String> indices = simpleSet.getIndices();
                    String outputMatrix = simpleSet.getOutput();

                    if (!candidateCooperativelyBuiltMatrices.containsKey(inputMatrix)) {
                        continue;
                    }

                    if (indices.size() == 0) {
                        log("Found simple_get with no indices");
                        continue;
                    }

                    MatrixAccessPattern previousCandidate = candidateCooperativelyBuiltMatrices.get(inputMatrix);
                    if (previousCandidate == null) {
                        continue;
                    }
                    if (previousCandidate.getBuiltMatrix() != null) {
                        continue;
                    }

                    MatrixAccessPattern candidate = new MatrixAccessPattern(previousCandidate.getSourceMatrix(),
                            simpleSet.getOutput(), simpleSet.getOutput());
                    List<MatrixIndex> matrixIndices = new ArrayList<>();
                    for (String index : indices) {
                        int indexVariable = loopInformation.iterVariable.indexOf(index);

                        if (indexVariable >= 0) {
                            matrixIndices.add(MatrixIndex.newIter(indexVariable));
                        } else {
                            matrixIndices.add(MatrixIndex.newVar(index));
                        }
                    }
                    candidate.setIndices(matrixIndices);

                    candidateCooperativelyBuiltMatrices.put(outputMatrix, candidate);
                }

                FirstLoopInformation capturedInfo = loopInformation;
                getReadMatrix(instruction).ifPresent(matrixName -> {
                    capturedInfo.readMatrices.add(matrixName);
                });

                if (instruction instanceof ForInstruction) {
                    if (afterNestedLoop) {
                        log("Already found nested for loop. Treating new one as if it doesn't contribute to matrix access patterns.");
                    } else {
                        ForInstruction nestedFor = (ForInstruction) instruction;

                        FirstLoopInformation nestedInfo = loopInformation.copyBlockInfo();
                        childBuiltMatrices = new HashMap<>();
                        nestedInfo = examineFirstLoop(body, nestedFor, typeGetter, nestedInfo, childBuiltMatrices,
                                currentBlockId, depth + 1);
                        if (nestedInfo.isLegal) {
                            loopInformation = nestedInfo;
                            afterNestedLoop = true;
                            nestedLoopParent = currentBlockId;
                        } else {
                            log("Nested for instruction is not in the expected format.");
                            // Go to the next if statement
                        }
                    }
                }

                if (instruction.isEndingInstruction()) {
                    nextBlockId = instruction.tryGetEndBlock().orElse(-1);
                    break;
                }
            }

            isStart = false;
        }

        loopInformation.cooperativelyBuiltMatrices = new HashMap<>();
        loopInformation.cooperativelyBuiltMatrices.putAll(candidateCooperativelyBuiltMatrices);

        return loopInformation;
    }

    private static Optional<String> getReadMatrix(SsaInstruction instruction) {

        if (instruction instanceof SimpleGetInstruction || instruction instanceof GetOrFirstInstruction
                || instruction instanceof MatrixGetInstruction) {
            String matrixName = ((IndexedInstruction) instruction).getInputMatrix();
            return Optional.of(matrixName);
        }
        return Optional.empty();
    }

    private static MiddleBlockInformation examineMiddleBlock(SsaBlock middleBlock,
            SizeGroupInformation sizeGroupInformation,
            FirstLoopInformation firstLoopInformation,
            boolean firstLoopHasSideEffects,
            boolean secondLoopHasSideEffects,
            Function<String, Optional<VariableType>> typeGetter,
            int startBlockId,
            int firstLoopBlockId) {

        MiddleBlockInformation info = new MiddleBlockInformation();
        info.isLegal = true;

        for (SsaInstruction instruction : middleBlock.getInstructions()) {
            if (instruction instanceof LineInstruction || instruction instanceof CommentInstruction) {
                // TODO We'll need to decide how to deal with this.
                continue;
            }
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;
                info.postfixInstructions.add(phi);

                List<Integer> sourceBlocks = phi.getSourceBlocks();
                String output = phi.getOutput();
                int startIndex = sourceBlocks.indexOf(startBlockId);
                int loopIndex = sourceBlocks.indexOf(firstLoopInformation.endBlockId.get(0));

                if (startIndex == -1 || loopIndex == -1) {
                    log("Adding " + output + " to blacklist: Phi has weird sources: " + sourceBlocks);
                    info.blacklistedNames.add(output);
                    continue;
                }

                String startValue = phi.getInputVariables().get(startIndex);
                String loopValue = phi.getInputVariables().get(loopIndex);

                MatrixAccessPattern matrixAccessPattern = firstLoopInformation.cooperativelyBuiltMatrices
                        .get(loopValue);
                if (matrixAccessPattern != null && startValue.equals(matrixAccessPattern.getSourceMatrix())) {
                    info.cooperativelyBuiltInstructions.put(output, loopValue);
                    info.cooperativelyBuiltMatrices.put(output, matrixAccessPattern);
                } else {
                    log("Adding " + output + " to blacklist: Not cooperatively built.");
                    log("Could not find access pattern for " + loopValue);
                    log(String.valueOf(matrixAccessPattern));
                    info.blacklistedNames.add(output);
                }
                continue;
            }
            if (instruction.isEndingInstruction()) {
                ForInstruction forInstruction = (ForInstruction) instruction;

                String forEnd = forInstruction.getEnd();

                if (info.blacklistedNames.contains(forEnd)) {
                    log("Middle block uses blacklisted variable " + forEnd);
                    info.isLegal = false;
                    continue;
                }

                continue;
            }

            boolean instructionCanUseCooperativelyBuiltVariables;
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                String functionName = functionCall.getFunctionName();
                switch (functionName) {
                case "matisse_new_array_from_matrix":
                case "numel":
                case "size":
                    instructionCanUseCooperativelyBuiltVariables = true;
                    break;
                default:
                    instructionCanUseCooperativelyBuiltVariables = false;
                }
            } else {
                instructionCanUseCooperativelyBuiltVariables = false;
            }
            boolean blacklist = false;
            for (String input : instruction.getInputVariables()) {
                if (info.blacklistedNames.contains(input)) {

                    log("Blacklisting " + instruction.getOutputs() + ": Depend on blacklisted variable " + input);
                    blacklist = true;
                    break;
                }
                if (!instructionCanUseCooperativelyBuiltVariables &&
                        info.cooperativelyBuiltInstructions.containsKey(input)) {

                    log("Blacklisting " + instruction.getOutputs() + ": Use cooperatively built " + input
                            + " in a non-recognized pattern");
                    blacklist = true;
                    break;
                }
            }

            if (instruction.getInstructionType().mayHaveSideEffects() && firstLoopHasSideEffects) {
                blacklist = true;
            }

            if (blacklist) {
                info.postfixInstructions.add(instruction);
                info.blacklistedNames.addAll(instruction.getOutputs());
                continue;
            }
            info.prefixInstructions.add(instruction);
            log("adding " + instruction + " to aliasing information");
            sizeGroupInformation.addInstructionInformation(instruction);
        }

        for (SsaInstruction instruction : info.postfixInstructions) {
            if (instruction.getInstructionType().mayHaveSideEffects() && secondLoopHasSideEffects) {
                log("Middle block instruction that should be moved after the second loop has side-effects, but so does the second loop.");
                info.isLegal = false;
                return info;
            }
        }

        return info;
    }

    static class FirstLoopInformation {
        boolean isLegal;
        List<Integer> parentBlockId;
        List<Integer> startBlockId;
        List<Integer> endBlockId;
        List<String> starts;
        List<String> intervals;
        List<String> ends;
        List<String> iterVariable;
        Set<String> readMatrices;
        Map<String, MatrixAccessPattern> cooperativelyBuiltMatrices;

        public FirstLoopInformation copyBlockInfo() {
            FirstLoopInformation info = new FirstLoopInformation();

            info.isLegal = this.isLegal;
            info.parentBlockId = new ArrayList<>(this.parentBlockId);
            info.startBlockId = new ArrayList<>(this.startBlockId);
            info.endBlockId = new ArrayList<>(this.endBlockId);
            info.starts = new ArrayList<>(this.starts);
            info.intervals = new ArrayList<>(this.intervals);
            info.ends = new ArrayList<>(this.ends);
            info.iterVariable = new ArrayList<>(this.iterVariable);
            info.readMatrices = new HashSet<>(this.readMatrices);

            return info;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("[FirstLoopInformation ");
            builder.append(this.isLegal ? "legal" : "illegal");
            builder.append(", ");
            builder.append(this.startBlockId);
            builder.append(", ");
            builder.append(this.endBlockId);
            builder.append(", ");
            builder.append(this.starts);
            builder.append(", ");
            builder.append(this.intervals);
            builder.append(", ");
            builder.append(this.ends);
            builder.append(", ");
            builder.append(this.iterVariable);
            builder.append(", ");
            builder.append(this.cooperativelyBuiltMatrices);

            return builder.toString();
        }
    }

    static class MiddleBlockInformation {
        boolean isLegal;
        Map<String, String> cooperativelyBuiltInstructions = new HashMap<>();
        Map<String, MatrixAccessPattern> cooperativelyBuiltMatrices = new HashMap<>();
        List<SsaInstruction> prefixInstructions = new ArrayList<>();
        List<SsaInstruction> postfixInstructions = new ArrayList<>();
        List<String> blacklistedNames = new ArrayList<>();
    }

    static class SecondLoopInformation {
        boolean isLegal;
        List<String> starts;
        List<String> intervals;
        List<String> ends;
        List<String> iterVariable;
        Set<String> readMatrices;
        List<Integer> endBlockId;

        public SecondLoopInformation copy() {
            SecondLoopInformation info = new SecondLoopInformation();

            info.isLegal = this.isLegal;
            info.starts = new ArrayList<>(this.starts);
            info.intervals = new ArrayList<>(this.intervals);
            info.ends = new ArrayList<>(this.ends);
            info.iterVariable = new ArrayList<>(this.iterVariable);
            info.endBlockId = new ArrayList<>(this.endBlockId);
            info.readMatrices = new HashSet<>(this.readMatrices);

            return info;
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
