package org.specs.matlabtocl.v2.ssa.instructions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;

/**
 * Fills the start of an existing buffer with the data of a matrix. The type and size of the buffer are assumed to be
 * sufficient to hold the entire matrix.
 * 
 * @author Luís Reis
 *
 */
public class OverrideGpuBufferContentsInstruction extends ControlFlowIndependentInstruction {

    private String buffer;
    private String matrix;

    public OverrideGpuBufferContentsInstruction(String buffer, String matrix) {
        this.buffer = buffer;
        this.matrix = matrix;
    }

    public String getBuffer() {
        return buffer;
    }

    public String getMatrix() {
        return matrix;
    }

    @Override
    public OverrideGpuBufferContentsInstruction copy() {
        return new OverrideGpuBufferContentsInstruction(buffer, matrix);
    }

    @Override
    public List<String> getInputVariables() {
        return Arrays.asList(buffer, matrix);
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        buffer = newNames.getOrDefault(newNames, buffer);
        matrix = newNames.getOrDefault(newNames, matrix);
    }

    @Override
    public String toString() {
        return "override_gpu_buffer_contents " + buffer + ", " + matrix;
    }

}
