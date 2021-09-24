package org.specs.matisselib.ssa.instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

public class CellMakeRow extends ControlFlowIndependentInstruction {

    private final List<String> contents;
    private String output;

    public CellMakeRow(String output, List<String> contents) {
        this.output = output;
        this.contents = new ArrayList<>(contents);
    }

    @Override
    public CellMakeRow copy() {
        return new CellMakeRow(output, contents);
    }

    @Override
    public List<String> getInputVariables() {
        return Collections.unmodifiableList(contents);
    }

    public String getOutput() {
        return output;
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(output);
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        output = newNames.getOrDefault(output, output);
        renameVariableList(newNames, contents);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(output);
        builder.append(" = cell_make_row");
        for (int i = 0; i < contents.size(); ++i) {
            if (i != 0) {
                builder.append(", ");
            } else {
                builder.append(" ");
            }

            builder.append(contents.get(i));
        }

        return builder.toString();
    }

}
