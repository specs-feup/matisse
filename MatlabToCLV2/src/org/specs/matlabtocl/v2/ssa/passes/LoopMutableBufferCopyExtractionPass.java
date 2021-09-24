package org.specs.matlabtocl.v2.ssa.passes;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * <p>
 * Finds loops structured like:
 * 
 * <pre>
 * <code>
 * A = ...
 * for ...
 *    B = phi A, C
 *    B_gpu = copy_to_gpu B
 *    ...
 *    C = complete_reduction MATRIX_SET B_gpu, B
 * end
 * D = phi A, C % optional
 * </pre>
 * 
 * <p>
 * Where B and C are not modified or used inside the loop (other than for size/numel operations). If this pattern is found, then it:
 * 
 * <ul>
 * <li>Removes <code>B = phi A, C</code> and changes all references to B into references to A
 * <li>Moves the <code>copy_to_gpu</code> instruction to before the loop
 * <li>Moves the <code>complete_reduction</code> call to after the loop and makes it refer to D instead. If D does not
 * exist, then the instruction is just removed.
 * </ul>
 * 
 * @author Luís Reis
 *
 */
public class LoopMutableBufferCopyExtractionPass implements PostTypeInferencePass {

    private static final String PASS_NAME = "loop_mutable_buffer_copy_extraction";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping " + instance.getFunctionIdentification().getName());
            return;
        }

        logger.log("Starting " + instance.getFunctionIdentification().getName());

        ForLoopHierarchy loopHierarchy = ForLoopHierarchy.identifyLoops(instance.getFunctionBody());
        logger.log("Loops: " + loopHierarchy);

        Map<String, Integer> declarationOf = new HashMap<>();
        for (int blockId = 0; blockId < instance.getBlocks().size(); ++blockId) {
            SsaBlock block = instance.getBlock(blockId);

            for (SsaInstruction instruction : block.getInstructions()) {
                for (String output : instruction.getOutputs()) {
                    declarationOf.put(output, blockId);
                }
            }
        }

        for (int parentBlockId = 0; parentBlockId < instance.getBlocks().size(); ++parentBlockId) {
            Optional<BlockData> optBlockData = loopHierarchy.getBlockData(parentBlockId);
            if (!optBlockData.isPresent()) {
                logger.log("Ignoring block #" + parentBlockId
                        + " because it was not visited by ForLoopHierarchy.identifyLoops.");
                continue;
            }

            List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance,
                    parentBlockId, true);
            if (loopVariables.isEmpty()) {
                continue;
            }

            SsaBlock parentBlock = instance.getBlock(parentBlockId);
            ForInstruction xfor = (ForInstruction) parentBlock.getEndingInstruction().get();
            int blockId = xfor.getLoopBlock();

            for (LoopVariable loopVariable : loopVariables) {
                logger.log("Attempting to optimize " + loopVariable);
                String gpuBufferName = null;
                int phiLocation = -1;
                int completeReductionLocation = -1;
                int copyToGpuLocation = -1;
                CompleteReductionInstruction originalCompleteReduction = null;

                boolean valid = true;
                List<SsaInstruction> instructions = instance.getBlock(blockId).getInstructions();
                for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                    SsaInstruction instruction = instructions.get(instructionId);
                    if (instruction instanceof CopyToGpuInstruction) {
                        CopyToGpuInstruction copyToGpu = (CopyToGpuInstruction) instruction;

                        if (copyToGpu.getInput().equals(loopVariable.loopStart)) {
                            if (gpuBufferName != null) {
                                // Multiple buffers. Stop.
                                logger.log("Variable has multiple GPU buffers: " + gpuBufferName + ", "
                                        + copyToGpu.getOutput());
                                valid = false;
                                break;
                            }
                            copyToGpuLocation = instructionId;
                            gpuBufferName = copyToGpu.getOutput();
                            continue;
                        }
                    } else if (instruction instanceof CompleteReductionInstruction) {
                        CompleteReductionInstruction completeReduction = (CompleteReductionInstruction) instruction;
                        if (completeReduction.getReductionType() != ReductionType.MATRIX_SET) {
                            continue;
                        }

                        if (!completeReduction.getOutput().equals(loopVariable.loopEnd)) {
                            continue;
                        }

                        if (gpuBufferName == null) {
                            logger.log("No available gpu buffer");
                            valid = false;
                            break;
                        }

                        if (!gpuBufferName.equals(completeReduction.getBuffer())) {
                            logger.log("Mismatched gpu buffer names: " + gpuBufferName + " vs " + instruction);
                            valid = false;
                            break;
                        }

                        if (!completeReduction.getInitialValue().equals(loopVariable.loopStart)) {
                            logger.log("Mismatched initial value at " + completeReduction.getInitialValue());
                            valid = false;
                            break;
                        }

                        originalCompleteReduction = completeReduction;
                        completeReductionLocation = instructionId;
                    } else if (instruction.getInputVariables().contains(loopVariable.loopStart) ||
                            instruction.getInputVariables().contains(loopVariable.loopEnd)) {

                        if (instruction instanceof PhiInstruction) {
                            PhiInstruction phi = (PhiInstruction) instruction;
                            if (!phi.getOutput().equals(loopVariable.loopStart)) {
                                logger.log("Bad phi: " + phi);
                                valid = false;
                                break;
                            }

                            phiLocation = instructionId;
                        } else if (instruction instanceof FunctionCallInstruction) {
                            FunctionCallInstruction call = (FunctionCallInstruction) instruction;
                            String functionName = call.getFunctionName();

                            if (functionName.equals("numel") || functionName.equals("size")
                                    || functionName.equals("length")
                                    || functionName.equals("matisse_new_array_from_matrix")) {

                                // TODO: Check in which argument the variable is being used.
                                continue;
                            }

                            valid = false;
                            break;
                        } else if (instruction instanceof EndInstruction) {
                            continue;
                        } else {
                            logger.log("Can't optimize " + loopVariable.loopStart + " due to " + instruction);
                            valid = false;
                            break;
                        }
                    } else if (instruction.isEndingInstruction()) {
                        logger.log("Can't optimize due to " + instruction + " (inner blocks not currently supported)");
                        valid = false;
                        break;
                    }
                }

                if (completeReductionLocation == -1) {
                    logger.log("Missing complete_reduction instruction");
                    valid = false;
                }

                if (!valid) {
                    // Move on to next variable
                    continue;
                }

                logger.log("Can optimize " + loopVariable);

                assert phiLocation != -1;
                assert phiLocation < copyToGpuLocation;
                assert copyToGpuLocation < completeReductionLocation;

                // Step 1: remove starting phi, copy_to_gpu and complete_reduction
                // As we remove instructions, the indices of the next ones change
                logger.log("Delete " + instructions.get(phiLocation));
                instructions.remove(phiLocation);
                logger.log("Delete " + instructions.get(copyToGpuLocation - 1));
                instructions.remove(copyToGpuLocation - 1);
                logger.log("Delete " + instructions.get(completeReductionLocation - 2));
                instructions.remove(completeReductionLocation - 2);

                // Step 2: rename variables
                logger.log("Rename: " + loopVariable.loopStart + " -> " + loopVariable.beforeLoop);
                Map<String, String> newNames = new HashMap<>();
                newNames.put(loopVariable.loopStart, loopVariable.beforeLoop);
                newNames.put(loopVariable.loopEnd, loopVariable.beforeLoop);
                instance.renameVariables(newNames);

                // Step 3: Add copy_to_gpu instruction before loop
                SsaInstruction newCopy = new CopyToGpuInstruction(gpuBufferName, loopVariable.beforeLoop);
                parentBlock.insertInstruction(parentBlock.getInstructions().size() - 1, newCopy);

                // Step 4: If needed, replace phi instruction after loop
                if (!loopVariable.getAfterLoop().isPresent()) {
                    // Not needed
                    continue;
                }

                String afterLoop = loopVariable.getAfterLoop().get();
                int afterLoopBlockId = xfor.getEndBlock();

                SsaBlock afterBlock = instance.getBlock(afterLoopBlockId);
                ListIterator<SsaInstruction> it = afterBlock.getInstructions().listIterator();
                while (it.hasNext()) {
                    SsaInstruction instruction = it.next();
                    if (instruction instanceof PhiInstruction) {
                        PhiInstruction phi = (PhiInstruction) instruction;

                        if (phi.getOutput().equals(afterLoop)) {
                            it.remove();
                            break;
                        }
                    }
                }

                // Now inject the new replacement complete_reduction
                int afterPhi = BlockUtils.getAfterPhiInsertionPoint(afterBlock);
                SsaInstruction newCompleteReduction = new CompleteReductionInstruction(afterLoop,
                        ReductionType.MATRIX_SET, gpuBufferName, originalCompleteReduction.getUnderlyingType(),
                        originalCompleteReduction.getNumGroups(), loopVariable.beforeLoop);
                afterBlock.insertInstruction(afterPhi, newCompleteReduction);
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
