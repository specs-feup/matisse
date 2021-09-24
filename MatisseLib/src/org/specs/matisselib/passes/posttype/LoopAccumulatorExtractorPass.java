package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.ScopeUtils;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Detects matrix positions that are repeatedly modified in a loop, and rewrites the loop so that it uses an accumulator
 * scalar variable.<br>
 * Tends to produce dead code.
 * 
 * @author Luís Reis
 *
 */
public class LoopAccumulatorExtractorPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "loop_accumulator_extractor";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        UsageMap usages = UsageMap.build(instance.getFunctionBody());
        SizeGroupInformation sizeInfo = PassUtils.getData(passData, CompilerDataProviders.SIZE_GROUP_INFORMATION);

        logger.logStart(instance);

        for (int outerBlockId = 0; outerBlockId < instance.getBlocks().size(); ++outerBlockId) {
            SsaBlock outerBlock = instance.getBlocks().get(outerBlockId);
            ForInstruction xfor = outerBlock
                    .getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast)
                    .orElse(null);
            if (xfor == null) {
                continue;
            }

            logger.log("Found loop declared at #" + outerBlockId);

            List<LoopVariable> loopVars = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance, outerBlockId,
                    true);
            for (LoopVariable lv : loopVars) {
                logger.log("Testing loop var: " + lv.loopStart);
                if (!lv.getAfterLoop().isPresent()) {
                    logger.log("Variable is not used after loop (not even in phi statement).");
                    continue;
                }
                String afterLoop = lv.getAfterLoop().get();

                MatrixType matrixType = instance.getVariableType(lv.beforeLoop)
                        .filter(MatrixType.class::isInstance)
                        .map(MatrixType.class::cast)
                        .orElse(null);
                if (matrixType == null) {
                    logger.log("Variable is not a matrix");
                    continue;
                }

                // Variable must only be used in the phi instructions
                // The usage count is 2: the phi inside the loop and the phi after the loop.
                if (usages.getUsageCount(afterLoop) != 2) {
                    logger.log("Invalid count for " + afterLoop);
                    continue;
                }

                if (usages.getUsageCount(lv.loopEnd) != 2) {
                    logger.log("Unrecognized pattern");
                    continue;
                }

                List<String> indices = null;
                int phiLocation = -1;
                List<Integer> getsToChange = new ArrayList<>();
                int setIndex = -1;
                String setValue = null;

                List<SsaInstruction> instructions = instance.getBlock(xfor.getLoopBlock()).getInstructions();
                for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                    SsaInstruction instruction = instructions.get(instructionId);

                    if (instruction instanceof PhiInstruction) {
                        PhiInstruction phi = (PhiInstruction) instruction;

                        if (phi.getOutput().equals(lv.loopStart)) {
                            assert phiLocation < 0;
                            phiLocation = instructionId;
                        }
                    }
                    if (instruction instanceof SimpleGetInstruction) {
                        SimpleGetInstruction get = (SimpleGetInstruction) instruction;

                        if (!get.getInputMatrix().equals(lv.loopStart)) {
                            continue;
                        }

                        indices = validateIndices(indices, get.getIndices(), sizeInfo);
                        if (indices == null) {
                            logger.log("Mismatch in accessed indices");
                            break;
                        }
                        getsToChange.add(instructionId);
                        continue;
                    }
                    if (instruction instanceof SimpleSetInstruction) {
                        SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                        if (!set.getInputMatrix().equals(lv.loopStart)) {
                            continue;
                        }
                        if (!set.getOutput().equals(lv.loopEnd)) {
                            logger.log("Unrecognized pattern");
                            break;
                        }

                        indices = validateIndices(indices, set.getIndices(), sizeInfo);
                        if (indices == null) {
                            logger.log("Mismatch in accessed indices");
                            break;
                        }
                        setIndex = instructionId;
                        setValue = set.getValue();
                        continue;
                    }
                }

                int expectedLoopStartUsages = 1 + getsToChange.size();
                if (usages.getUsageCount(lv.loopStart) != expectedLoopStartUsages) {
                    logger.log("Wrong usages of " + lv.loopStart + "(expected " + expectedLoopStartUsages + ", got "
                            + usages.getUsageCount(lv.loopStart) + ")");
                    continue;
                }
                if (setIndex < 0) {
                    logger.log("Accumulator not set in loop.");
                    continue;
                }

                if (indices.stream()
                        .anyMatch(ScopeUtils.getDeclaredVariables(instance, xfor.getLoopBlock())::contains)) {
                    // Variable declared in loop. Can't handle this case.
                    logger.log("Index variable is declared in loop.");
                    continue;
                }

                logger.log("Optimizing");

                String suggestedAccumulatorName = "acc";
                VariableType accumulatorType = matrixType.matrix().getElementType();
                String accumulatorVarBeforeLoop = instance.makeTemporary(suggestedAccumulatorName, accumulatorType);
                String accumulatorVarLoopStart = instance.makeTemporary(suggestedAccumulatorName, accumulatorType);
                String accumulatorVarAfterLoop = instance.makeTemporary(suggestedAccumulatorName, accumulatorType);

                SsaInstruction accumulatorDefinition = new SimpleGetInstruction(accumulatorVarBeforeLoop, lv.beforeLoop,
                        indices);
                // Insert accumulator definition right before the for instruction.
                outerBlock.insertInstruction(outerBlock.getInstructions().size() - 2, accumulatorDefinition);

                // Valid, now rewrite block
                for (int getToChange : getsToChange) {
                    SimpleGetInstruction oldInstruction = (SimpleGetInstruction) instructions.get(getToChange);
                    SsaInstruction newInstruction = AssignmentInstruction.fromVariable(oldInstruction.getOutput(),
                            getToChange < setIndex ? accumulatorVarLoopStart : setValue);

                    instructions.set(getToChange, newInstruction);
                }
                assert phiLocation >= 0;
                instructions.remove(setIndex);
                SsaInstruction newInLoopPhi = new PhiInstruction(accumulatorVarLoopStart,
                        Arrays.asList(accumulatorVarBeforeLoop, setValue),
                        Arrays.asList(outerBlockId, xfor.getLoopBlock()));
                instructions.set(phiLocation, newInLoopPhi);

                List<SsaInstruction> endInstructions = instance.getBlock(xfor.getEndBlock()).getInstructions();
                List<SsaInstruction> instructionsToInject = new ArrayList<>();
                int lastPhi = -1;
                for (int i = 0; i < endInstructions.size(); ++i) {
                    SsaInstruction instruction = endInstructions.get(i);
                    if (instruction instanceof PhiInstruction) {
                        PhiInstruction phi = (PhiInstruction) instruction;

                        if (!phi.getInputVariables().contains(lv.loopEnd)) {
                            lastPhi = i;
                            continue;
                        }

                        SsaInstruction newInstruction = new SimpleSetInstruction(afterLoop, lv.beforeLoop, indices,
                                accumulatorVarAfterLoop);
                        instructionsToInject.add(newInstruction);

                        SsaInstruction replacementPhi = new PhiInstruction(accumulatorVarAfterLoop,
                                Arrays.asList(accumulatorVarBeforeLoop, setValue),
                                Arrays.asList(outerBlockId, xfor.getLoopBlock()));
                        endInstructions.set(i, replacementPhi);
                        --i;
                    }
                }

                endInstructions.addAll(lastPhi + 1, instructionsToInject);
            }
        }
    }

    private List<String> validateIndices(List<String> indices, List<String> newIndices, SizeGroupInformation sizeInfo) {
        if (indices == null) {
            return newIndices;
        }
        if (indices.size() != newIndices.size()) {
            // Invalid
            return null;
        }
        assert indices.size() == newIndices.size();

        for (int i = 0; i < indices.size(); i++) {
            String index = indices.get(i);
            String otherIndex = newIndices.get(i);

            if (!sizeInfo.areSameValue(index, otherIndex)) {
                return null;
            }
        }

        return indices;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
