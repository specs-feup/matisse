package org.specs.matlabtocl.v2.codegen.reductionvalidators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public final class GeneralReductionFormatValidator {
    private static final boolean ENABLE_LOG = false;

    private GeneralReductionFormatValidator() {
    }

    private static abstract class ValidationContext {
        String activeName;
        int currentBlockId;

        abstract boolean isBlacklisted(String name);

        abstract void addToBlacklist(String name);
    }

    private static class RootValidationContext extends ValidationContext {
        int headBlockId;
        int loopEndBlockId;
        String initialName;
        String finalLoopName;
        Set<String> blacklist = new HashSet<>();

        RootValidationContext(int headBlockId, int loopEndBlockId, String initialName, String finalLoopName) {
            this.headBlockId = headBlockId;
            this.loopEndBlockId = loopEndBlockId;
            this.initialName = initialName;
            this.finalLoopName = finalLoopName;
        }

        @Override
        boolean isBlacklisted(String name) {
            return this.blacklist.contains(name);
        }

        @Override
        void addToBlacklist(String name) {
            this.blacklist.add(name);
        }
    }

    private static class BranchValidationContext extends ValidationContext {
        ValidationContext parentContext;
        Set<String> blacklist = new HashSet<>();

        BranchValidationContext(ValidationContext parentContext, int currentBlockId) {
            this.activeName = parentContext.activeName;
            this.parentContext = parentContext;
            this.currentBlockId = currentBlockId;
        }

        @Override
        boolean isBlacklisted(String name) {
            return this.blacklist.contains(name) || this.parentContext.isBlacklisted(name);
        }

        @Override
        void addToBlacklist(String name) {
            this.blacklist.add(name);
        }
    }

    private static class LoopValidationContext extends ValidationContext {
        ValidationContext parentContext;
        int endLoopId = -1;
        String endLoopName = null;
        Set<String> blacklist = new HashSet<>();

        LoopValidationContext(ValidationContext parentContext, int currentBlockId) {
            this.parentContext = parentContext;
            this.currentBlockId = currentBlockId;
            this.activeName = parentContext.activeName;
        }

        @Override
        boolean isBlacklisted(String name) {
            if (this.blacklist.contains(name)) {
                return true;
            }
            if (!this.activeName.equals(this.parentContext.activeName) &&
                    name.equals(this.parentContext.activeName)) {
                return true;
            }

            return this.parentContext.isBlacklisted(name);
        }

        @Override
        void addToBlacklist(String name) {
            this.blacklist.add(name);
        }
    }

    private static class MergeBranchValidationContext extends ValidationContext {
        BranchValidationContext ctx1, ctx2;
        Set<String> blacklist = new HashSet<>();

        MergeBranchValidationContext(
                BranchValidationContext ctx1,
                BranchValidationContext ctx2,
                int currentBlockId) {

            this.ctx1 = ctx1;
            this.ctx2 = ctx2;
            this.currentBlockId = currentBlockId;

            if (ctx1.activeName.equals(ctx2.activeName)) {
                this.activeName = ctx1.activeName;
            }
        }

        @Override
        boolean isBlacklisted(String name) {
            return this.blacklist.contains(name) || this.ctx1.isBlacklisted(name) || this.ctx2.isBlacklisted(name);
        }

        @Override
        void addToBlacklist(String name) {
            this.blacklist.add(name);
        }
    }

    private static class MergeLoopValidationContext extends ValidationContext {
        ValidationContext parentContext;
        LoopValidationContext loopContext;
        Set<String> blacklist = new HashSet<>();

        MergeLoopValidationContext(
                ValidationContext parentContext,
                LoopValidationContext loopContext,
                int currentBlockId) {

            this.parentContext = parentContext;
            this.loopContext = loopContext;
            this.currentBlockId = currentBlockId;

            if (parentContext.activeName.equals(loopContext.activeName)) {
                this.activeName = parentContext.activeName;
            }
        }

        @Override
        boolean isBlacklisted(String name) {
            return this.blacklist.contains(name) ||
                    this.parentContext.isBlacklisted(name) ||
                    this.loopContext.isBlacklisted(name);
        }

        @Override
        void addToBlacklist(String name) {
            this.blacklist.add(name);
        }
    }

    public static Optional<GeneralReductionFormatValidationResult> test(
            FunctionBody functionBody,
            List<Integer> blocks,
            List<LoopVariable> loopVariable) {

        String initialName = SpecsCollections.last(loopVariable).beforeLoop;
        String finalLoopName = SpecsCollections.last(loopVariable).loopEnd;

        int headBlockId = SpecsCollections.last(blocks);
        ForInstruction xfor = (ForInstruction) functionBody.getBlock(headBlockId).getEndingInstruction().get();
        int loopBlockId = xfor.getLoopBlock();
        int loopEndBlockId = BlockUtils.getBlockEnd(functionBody, loopBlockId);

        List<InstructionLocation> constructionInstructions = new ArrayList<>();
        List<InstructionLocation> midUsageInstructions = new ArrayList<>();
        List<String> reductionNames = new ArrayList<>();

        reductionNames.add(initialName);

        ValidationContext validationContext = new RootValidationContext(headBlockId, loopEndBlockId,
                initialName, finalLoopName);
        validationContext.currentBlockId = loopBlockId;

        if (!testBlock(functionBody, constructionInstructions, midUsageInstructions, reductionNames,
                validationContext)) {
            return Optional.empty();
        }

        String activeName = validationContext.activeName;
        if (activeName == null) {
            log("Reduction variable not referenced in loop. What's going on?");
            return Optional.empty();
        }
        if (!activeName.equals(finalLoopName)) {
            log("Unsupported reduction format: Reduction variable doesn't lead to final name (last name in loop is "
                    + activeName + ")");
            return Optional.empty();
        }

        GeneralReductionFormatValidationResult result = new GeneralReductionFormatValidationResult(
                constructionInstructions, midUsageInstructions, reductionNames);
        return Optional.of(result);
    }

    private static boolean testBlock(FunctionBody functionBody,
            List<InstructionLocation> constructionInstructions,
            List<InstructionLocation> midUsageInstructions,
            List<String> reductionNames,
            ValidationContext validationContext) {

        int currentBlockId = validationContext.currentBlockId;
        String activeName = validationContext.activeName;

        SsaBlock block = functionBody.getBlock(currentBlockId);
        List<SsaInstruction> instructions = block.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            SsaInstruction instruction = instructions.get(i);

            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                List<Integer> sourceBlocks = phi.getSourceBlocks();

                if (sourceBlocks.size() != 2) {
                    log("Phi node with " + sourceBlocks.size() + " sources. What to do with this?");
                    return false;
                }

                if (validationContext instanceof RootValidationContext) {
                    RootValidationContext rootContext = (RootValidationContext) validationContext;
                    int headBlockId = rootContext.headBlockId;
                    int loopEndBlockId = rootContext.loopEndBlockId;
                    String initialName = rootContext.initialName;
                    String finalLoopName = rootContext.finalLoopName;

                    int headBlockPhiPosition = sourceBlocks.indexOf(headBlockId);
                    if (headBlockPhiPosition < 0) {
                        log("Phi doesn't reference loop header block (" + headBlockId + ") in " + phi);
                        return false;
                    }
                    int loopEndBlockPhiPosition = sourceBlocks.indexOf(loopEndBlockId);
                    if (loopEndBlockPhiPosition < 0) {
                        log("Phi doesn't reference loop end block (" + loopEndBlockId + ") in " + phi);
                        return false;
                    }

                    if (phi.getInputVariables().get(headBlockPhiPosition).equals(initialName)) {
                        if (!phi.getInputVariables().get(loopEndBlockPhiPosition).equals(finalLoopName)) {
                            log("In order to apply reduction, the loop start phi should reference the same ending variable as the after loop phi");
                            return false;
                        }

                        activeName = phi.getOutput();
                        reductionNames.add(activeName);
                    }
                } else if (validationContext instanceof BranchValidationContext) {
                    log("Found phi in if statement.");
                    return false;
                } else if (validationContext instanceof LoopValidationContext) {
                    LoopValidationContext loopContext = (LoopValidationContext) validationContext;

                    int headBlockId = loopContext.parentContext.currentBlockId;
                    int headBlockIndex = sourceBlocks.indexOf(headBlockId);
                    if (headBlockIndex < 0) {
                        log("Phi doesn't reference loop header block.");
                        return false;
                    }

                    int loopEndIndex = headBlockIndex == 0 ? 1 : 0;
                    int loopEndBlockId = sourceBlocks.get(loopEndIndex);

                    if (loopContext.endLoopId == -1) {
                        loopContext.endLoopId = loopEndBlockId;
                    } else if (loopContext.endLoopId != loopEndBlockId) {
                        log("Unable to tell which block ends loop");
                        return false;
                    }

                    String block1Name = phi.getInputVariables().get(headBlockIndex);
                    String block2Name = phi.getInputVariables().get(loopEndIndex);

                    if (block1Name.equals(loopContext.parentContext.activeName)) {
                        if (!activeName.equals(loopContext.parentContext.activeName)) {
                            log("Reduction variable already referenced before phi node");
                            return false;
                        }

                        activeName = phi.getOutput();
                        loopContext.endLoopName = block2Name;
                        reductionNames.add(activeName);
                    }
                } else if (validationContext instanceof MergeBranchValidationContext) {

                    MergeBranchValidationContext merge = (MergeBranchValidationContext) validationContext;

                    int block1Index = sourceBlocks.indexOf(merge.ctx1.currentBlockId);
                    int block2Index = sourceBlocks.indexOf(merge.ctx2.currentBlockId);

                    if (block1Index < 0 || block2Index < 0) {
                        log("Unexpected format of phi node after branch");
                        return false;
                    }

                    String block1Name = phi.getInputVariables().get(block1Index);
                    String block2Name = phi.getInputVariables().get(block2Index);

                    if (block1Name.equals(merge.ctx1.activeName)) {
                        if (block2Name.equals(merge.ctx2.activeName)) {
                            if (activeName != null) {
                                log("Reduction variable referenced in two phi nodes");
                                return false;
                            }

                            validationContext.addToBlacklist(merge.ctx1.activeName);
                            validationContext.addToBlacklist(merge.ctx2.activeName);
                            activeName = phi.getOutput();
                            reductionNames.add(activeName);

                            continue;
                        }

                        log("Unexpected phi format");
                        return false;
                    } else if (block2Name.equals(merge.ctx2.activeName)) {
                        log("Unexpected phi format");
                        return false;
                    }
                } else if (validationContext instanceof MergeLoopValidationContext) {

                    MergeLoopValidationContext merge = (MergeLoopValidationContext) validationContext;

                    int block1Index = sourceBlocks.indexOf(merge.parentContext.currentBlockId);
                    int block2Index = sourceBlocks.indexOf(merge.loopContext.currentBlockId);

                    if (block1Index < 0 || block2Index < 0) {
                        log("Unexpected format of phi node after branch");
                        return false;
                    }

                    String block1Name = phi.getInputVariables().get(block1Index);
                    String block2Name = phi.getInputVariables().get(block2Index);

                    if (block1Name.equals(merge.parentContext.activeName)) {
                        if (block2Name.equals(merge.loopContext.activeName)) {
                            if (activeName != null) {
                                log("Reduction variable referenced in two phi nodes");
                                return false;
                            }

                            validationContext.addToBlacklist(merge.parentContext.activeName);
                            validationContext.addToBlacklist(merge.loopContext.activeName);
                            activeName = phi.getOutput();
                            reductionNames.add(activeName);
                            continue;
                        }

                        log("Unexpected phi format");
                        return false;
                    } else if (block2Name.equals(merge.loopContext.activeName)) {
                        log("Unexpected phi format");
                        return false;
                    }
                } else {
                    throw new NotImplementedException(validationContext.getClass().getSimpleName());
                }
            }

            for (String input : instruction.getInputVariables()) {
                if (validationContext.isBlacklisted(input)) {
                    log("Improper reduction format: Referencing invalidated variable: " + input + "\nIn "
                            + instruction);
                    return false;
                }
            }

            if (instruction.getInputVariables().contains(activeName)) {
                if (instruction instanceof SimpleGetInstruction) {
                    // TODO: instructions like numel should be here too.

                    SimpleGetInstruction get = (SimpleGetInstruction) instruction;

                    if (get.getIndices().contains(activeName)) {
                        log("Instruction " + get + " references reduction variable as index");
                        return false;
                    }

                    midUsageInstructions.add(new InstructionLocation(currentBlockId, i));
                } else {
                    constructionInstructions.add(new InstructionLocation(currentBlockId, i));

                    validationContext.addToBlacklist(activeName);

                    List<String> outputs = instruction.getOutputs();
                    if (outputs.size() != 1) {
                        log("Instruction with " + outputs.size() + " outputs references active.");
                        return false;
                    }

                    activeName = outputs.get(0);
                    reductionNames.add(activeName);
                }
            }

            if (instruction instanceof BranchInstruction) {
                BranchInstruction branch = (BranchInstruction) instruction;

                if (activeName == null) {
                    log("Found branch before phi.");
                    return false;
                }
                validationContext.activeName = activeName;

                int trueBlock = branch.getTrueBlock();
                BranchValidationContext ifContext = new BranchValidationContext(validationContext, trueBlock);
                if (!testBlock(functionBody, constructionInstructions, midUsageInstructions, reductionNames,
                        ifContext)) {
                    return false;
                }

                int falseBlock = branch.getFalseBlock();
                BranchValidationContext elseContext = new BranchValidationContext(validationContext, falseBlock);
                if (!testBlock(functionBody, constructionInstructions, midUsageInstructions, reductionNames,
                        elseContext)) {
                    return false;
                }

                ValidationContext afterIfContext = new MergeBranchValidationContext(
                        ifContext,
                        elseContext,
                        branch.getEndBlock());

                if (!testBlock(functionBody, constructionInstructions, midUsageInstructions, reductionNames,
                        afterIfContext)) {
                    return false;
                }

                validationContext.activeName = afterIfContext.activeName;
                validationContext.currentBlockId = afterIfContext.currentBlockId;
                return true;
            }
            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;

                if (activeName == null) {
                    log("Found loop before phi.");
                    return false;
                }
                validationContext.activeName = activeName;

                int loopBlock = xfor.getLoopBlock();
                LoopValidationContext loopContext = new LoopValidationContext(validationContext, loopBlock);
                if (!testBlock(functionBody, constructionInstructions, midUsageInstructions, reductionNames,
                        loopContext)) {
                    return false;
                }

                if (loopContext.endLoopId != -1 && loopContext.endLoopId != loopContext.currentBlockId) {
                    log("Phis in loop do not reference ending block, id1=" + loopContext.endLoopId + ", id2="
                            + loopContext.currentBlockId);
                    return false;
                }

                if (!loopContext.activeName.equals(activeName)
                        && !loopContext.activeName.equals(loopContext.endLoopName)) {
                    log("Unrecognized reduction format");
                    return false;
                }

                ValidationContext afterLoopContext = new MergeLoopValidationContext(
                        validationContext,
                        loopContext,
                        xfor.getEndBlock());

                if (!testBlock(functionBody, constructionInstructions, midUsageInstructions, reductionNames,
                        afterLoopContext)) {
                    return false;
                }

                validationContext.activeName = afterLoopContext.activeName;
                validationContext.currentBlockId = afterLoopContext.currentBlockId;
                return true;
            }
            if (instruction.getOwnedBlocks().size() != 0) {
                // TODO
                log("TODO: Inner blocks: " + instruction);
                return false;
            }
        }

        validationContext.activeName = activeName;
        return true;
    }

    private static void log(String message) {
        if (GeneralReductionFormatValidator.ENABLE_LOG) {
            System.out.print("[general_reduction_format] ");
            System.out.println(message);
        }
    }
}
