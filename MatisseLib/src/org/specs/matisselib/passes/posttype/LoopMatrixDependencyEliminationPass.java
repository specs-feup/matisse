package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.collections.MultiMap;

public class LoopMatrixDependencyEliminationPass extends TypeTransparentSsaPass {

    public static final String PASS_NAME = "loop_matrix_dependency_elimination";

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        logger.log("Starting " + body.getName());

        MultiMap<String, String> derivedIndices = getAuthorizedDerivedIndices(body, typeGetter);
        logger.log("Derived indices: " + derivedIndices);

        ForLoopHierarchy hierarchy = ForLoopHierarchy.identifyLoops(body);

        List<SsaBlock> blocks = body.getBlocks();
        for (BlockData blockData : hierarchy.getForLoops()) {
            int blockId = blockData.getBlockId();
            ForInstruction xfor = blocks.get(blockId)
                    .getEndingInstruction()
                    .map(ForInstruction.class::cast)
                    .get();
            SsaBlock block = blocks.get(xfor.getLoopBlock());

            List<Integer> blockIds = new ArrayList<>(blockData.getNesting());
            blockIds.add(blockData.getBlockId());

            processFor(derivedIndices, body, typeGetter,
                    block,
                    blockIds,
                    logger);
        }
    }

    private static void processFor(MultiMap<String, String> derivedIndices,
            FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            SsaBlock block,
            List<Integer> blockIds,
            Logger logger) {

        for (int i = 0; i < blockIds.size(); ++i) {
            List<Integer> workingBlockIds = blockIds.subList(i, blockIds.size());
            logger.log("Block Ids: " + workingBlockIds);

            List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(body, typeGetter,
                    workingBlockIds.get(0), true);
            logger.log("Outer loop variables: " + loopVariables);

            for (int blockId : workingBlockIds.subList(1, workingBlockIds.size())) {
                List<LoopVariable> nestedLoopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(body,
                        typeGetter,
                        blockId, true);
                logger.log("Nested vars: " + nestedLoopVariables);

                ListIterator<LoopVariable> iterator = loopVariables.listIterator();
                while (iterator.hasNext()) {
                    LoopVariable currentVar = iterator.next();

                    Optional<LoopVariable> fusedVar = fuseVars(currentVar, nestedLoopVariables, logger);
                    if (fusedVar.isPresent()) {
                        iterator.set(fusedVar.get());
                    } else {
                        iterator.remove();
                    }
                }
            }
            logger.log("Testing " + loopVariables);

            Map<String, String> directOutputs = loopVariables.stream()
                    .collect(Collectors.toMap(lv -> lv.loopStart, lv -> lv.loopEnd));
            logger.log("Direct outputs: " + directOutputs);

            MultiMap<String, Integer> gets = new MultiMap<>();

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof SimpleGetInstruction) {
                    SimpleGetInstruction get = (SimpleGetInstruction) instruction;

                    gets.put(get.getInputMatrix(), instructionId);
                    logger.log("Found get " + get);
                    continue;
                }

                if (instruction instanceof SimpleSetInstruction) {
                    SimpleSetInstruction set = (SimpleSetInstruction) instruction;
                    logger.log("Found set " + set);

                    String input = set.getInputMatrix();
                    String output = set.getOutput();

                    String directOutput = directOutputs.get(input);
                    if (!Objects.equals(directOutput, output)) {
                        logger.log("Output mismatch: " + directOutput + " vs " + output);
                        continue;
                    }

                    Optional<String> maybeParentMatrix = getParentMatrixOf(loopVariables, input);
                    if (!maybeParentMatrix.isPresent()) {
                        logger.log(input + " not in a conventionally set variable");
                        continue;
                    }
                    String parentMatrix = maybeParentMatrix.get();
                    logger.log(input + " built from " + parentMatrix);

                    logger.log("Searching for gets of " + parentMatrix);
                    for (int getPosition : gets.get(parentMatrix)) {
                        SimpleGetInstruction get = (SimpleGetInstruction) block.getInstructions().get(getPosition);

                        if (isFalseDependencyGet(get, typeGetter, derivedIndices, set, logger)) {
                            logger.log("Eliminating false dependency get");
                            block.replaceInstructionAt(getPosition,
                                    new SimpleGetInstruction(get.getOutput(), input, get.getIndices()));
                        }
                    }
                }
            }
        }
    }

    private static Optional<String> getParentMatrixOf(List<LoopVariable> loopVariables, String input) {
        return loopVariables.stream()
                .filter(lv -> lv.loopStart.equals(input))
                .map(lv -> lv.beforeLoop)
                .findFirst();
    }

    private static Optional<LoopVariable> fuseVars(LoopVariable currentVar,
            List<LoopVariable> nestedLoopVariables,
            Logger logger) {
        for (LoopVariable lv : nestedLoopVariables) {
            if (lv.beforeLoop.equals(currentVar.loopStart)
                    && lv.getAfterLoop().equals(Optional.of(currentVar.loopEnd))) {
                return Optional.of(new LoopVariable(currentVar.beforeLoop, lv.loopStart, lv.loopEnd,
                        currentVar.getAfterLoop().orElse(null)));
            }
        }

        logger.log("No match for " + currentVar);
        return Optional.empty();
    }

    private static boolean isFalseDependencyGet(SimpleGetInstruction get,
            Function<String, Optional<VariableType>> typeGetter,
            MultiMap<String, String> derivedIndices,
            SimpleSetInstruction set,
            Logger logger) {

        if (get.getIndices().size() != set.getIndices().size()) {
            logger.log("Number of indices does not match");
            return false;
        }

        List<String> getIndices = get.getIndices();
        List<String> setIndices = set.getIndices();
        for (int i = 0; i < getIndices.size(); i++) {
            String getIndex = getIndices.get(i);
            String setIndex = setIndices.get(i);

            if (getIndex.equals(setIndex)) {
                continue;
            }

            if (ConstantUtils.hasSameConstantValue(typeGetter.apply(getIndex), typeGetter.apply(setIndex))) {
                continue;
            }

            if (!derivedIndices.get(setIndex).contains(getIndex)) {
                logger.log("At " + set + ": " + setIndex + " not derived from " + getIndex);
                return false;
            }
        }

        return true;
    }

    public static MultiMap<String, String> getAuthorizedDerivedIndices(
            FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter) {

        MultiMap<String, String> result = new MultiMap<>();

        Map<Integer, String> relevantFors = new HashMap<>();
        for (SsaBlock block : body.getBlocks()) {
            block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast)
                    .ifPresent(forInstruction -> {

                        String step = forInstruction.getInterval();
                        if (!ConstantUtils.isConstantOne(typeGetter.apply(step))) {
                            return;
                        }

                        String end = forInstruction.getEnd();
                        relevantFors.put(forInstruction.getLoopBlock(), end);

                    });
        }

        for (int forBlockId : relevantFors.keySet()) {
            SsaBlock forBlock = body.getBlock(forBlockId);

            String iter = null;
            for (SsaInstruction instruction : forBlock.getInstructions()) {
                if (instruction instanceof IterInstruction) {
                    iter = ((IterInstruction) instruction).getOutput();
                }
                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                    String functionName = functionCall.getFunctionName();
                    if (functionName.equals("minus") &&
                            functionCall.getOutputs().size() == 1 &&
                            functionCall.getInputVariables().size() == 2 &&
                            ConstantUtils.isKnownPositiveInteger(
                                    typeGetter.apply(functionCall.getInputVariables().get(1)))) {

                        String base = functionCall.getInputVariables().get(0);
                        String output = functionCall.getOutputs().get(0);

                        result.put(base, output);
                        if (base.equals(iter) && !result.get(iter).contains(output)) {
                            result.put(iter, output);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
