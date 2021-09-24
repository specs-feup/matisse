package org.specs.matisselib.ssa.instructions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

/**
 * Validates that at least one of the supplied matrices is an empty matrix.
 * 
 * @author Luís Reis
 *
 */
public class ValidateAtLeastOneEmptyMatrixInstruction extends ControlFlowIndependentInstruction {

    private final List<String> matrices;

    public ValidateAtLeastOneEmptyMatrixInstruction(List<String> matrices) {
        Preconditions.checkArgument(matrices != null);

        this.matrices = new ArrayList<>(matrices);
    }

    @Override
    public ValidateAtLeastOneEmptyMatrixInstruction copy() {
        return new ValidateAtLeastOneEmptyMatrixInstruction(matrices);
    }

    @Override
    public List<String> getInputVariables() {
        return Collections.unmodifiableList(matrices);
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        renameVariableList(newNames, matrices);
    }

    @Override
    public String toString() {
        return "validate_at_least_one_empty_matrix " + matrices;
    }
}
