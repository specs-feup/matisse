package org.specs.matisselib.passes.posttype;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.passes.ssa.ArrayAccessSimplifierPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * In loops where a matrix is read and modified, but the final result is never used, see if we can remove the phi and
 * instead refer to the starting matrix instead.
 * 
 * @see ArrayAccessSimplifierPass
 * @see DuplicatedReadEliminationPass
 * @author Luís Reis
 *
 */
public class UselessMatrixAssignmentRemovalPass implements PostTypeInferencePass {

    private static final boolean ENABLE_LOG = false;

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        log("Starting " + instance.getFunctionIdentification().getName());

        List<SsaBlock> blocks = instance.getBlocks();

        for (int containerBlockId = 0; containerBlockId < blocks.size(); ++containerBlockId) {
            SsaBlock containerBlock = blocks.get(containerBlockId);
            Optional<ForInstruction> maybeFor = containerBlock
                    .getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);

            if (!maybeFor.isPresent()) {
                continue;
            }

            ForInstruction xfor = maybeFor.get();

            List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance,
                    containerBlockId, true);
            for (LoopVariable loopVariable : loopVariables) {
                if (loopVariable.getAfterLoop().isPresent()) {
                    continue;
                }

                log("Checking " + loopVariable);

                // Found potentially interesting variable
                // <LoopStart> must only be used in phi and simple_get/size/numel/etc.
                // <LoopEnd> must only be used in phi and simple_set/size/numel/etc.

                String beforeLoop = loopVariable.beforeLoop;
                String loopStart = loopVariable.loopStart;
                String loopEnd = loopVariable.loopEnd;

                if (!analyzeLoop(instance, xfor.getLoopBlock(), loopStart, loopEnd, null, true)) {
                    continue;
                }

                // Ok
                // Delete phi, simple_set
                log("Delete obsoleted instructions");
                deleteObsoletedInstructions(instance, xfor.getLoopBlock(), loopStart, loopEnd);
                // Rename matrices
                Map<String, String> newNames = new HashMap<>();
                newNames.put(loopStart, beforeLoop);
                newNames.put(loopEnd, beforeLoop);
                instance.renameVariables(newNames);
            }
        }
    }

    private void deleteObsoletedInstructions(TypedInstance instance, int blockId, String loopStart, String loopEnd) {
        SsaBlock block = instance.getBlock(blockId);
        ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();

        while (iterator.hasNext()) {
            SsaInstruction instruction = iterator.next();

            if (instruction instanceof PhiInstruction) {
                if (((PhiInstruction) instruction).getOutput().equals(loopStart)) {
                    log("Deleting " + instruction);
                    iterator.remove();
                }
                continue;
            }

            if (instruction instanceof SimpleSetInstruction) {
                if (((SimpleSetInstruction) instruction).getOutput().equals(loopEnd)) {
                    log("Deleting " + instruction);
                    iterator.remove();
                }
                continue;
            }

            for (int ownedBlock : instruction.getOwnedBlocks()) {
                deleteObsoletedInstructions(instance, ownedBlock, loopStart, loopEnd);
            }
        }
    }

    private boolean analyzeLoop(TypedInstance instance, int blockId, String loopStart, String loopEnd,
            String iterVariable,
            boolean isLoopEntry) {

        SsaBlock block = instance.getBlock(blockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;
                if (phi.getOutput().equals(loopStart)) {
                    continue;
                }
            }

            if (instruction instanceof IterInstruction) {
                if (isLoopEntry) {
                    iterVariable = ((IterInstruction) instruction).getOutput();
                    continue;
                }
            }

            if (instruction instanceof SimpleGetInstruction) {
                SimpleGetInstruction simpleGet = (SimpleGetInstruction) instruction;
                if (simpleGet.getInputMatrix().equals(loopStart)) {
                    if (simpleGet.getIndices().size() == 1 && simpleGet.getIndices().get(0).equals(iterVariable)) {
                        // Ok
                        continue;
                    } else {
                        log("Can't whitelist " + instruction + " (IV=" + iterVariable + ")");
                    }
                }
            }

            if (instruction instanceof SimpleSetInstruction) {
                SimpleSetInstruction simpleSet = (SimpleSetInstruction) instruction;
                if (simpleSet.getInputMatrix().equals(loopStart) && simpleSet.getOutput().equals(loopEnd)) {
                    if (simpleSet.getIndices().size() == 1 && simpleSet.getIndices().get(0).equals(iterVariable)) {
                        // Ok
                        continue;
                    } else {
                        log("Can't whitelist " + instruction + " (IV=" + iterVariable + ", LS=" + loopStart + ", LE="
                                + loopEnd + ")");
                    }
                }
            }

            // TODO: Some operations, such as size/numel, are safe.
            // Allow them.

            List<String> inputVariables = instruction.getInputVariables();
            List<String> outputs = instruction.getOutputs();
            if (inputVariables.contains(loopStart) || inputVariables.contains(loopEnd) ||
                    outputs.contains(loopStart) || outputs.contains(loopEnd)) {
                // Bad
                log("Can't optimize due to " + instruction + " (with IV=" + iterVariable + ")");
                return false;
            }

            for (int ownedBlock : instruction.getOwnedBlocks()) {
                if (!analyzeLoop(instance, ownedBlock, loopStart, loopEnd,
                        iterVariable,
                        false)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.println("[useless_matrix_assignment_removal] " + message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
