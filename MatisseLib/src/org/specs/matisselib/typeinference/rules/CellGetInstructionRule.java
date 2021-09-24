package org.specs.matisselib.typeinference.rules;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.CellGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class CellGetInstructionRule implements TypeInferenceRule {
    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof CellGetInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
        CellGetInstruction get = (CellGetInstruction) instruction;

        String cell = get.getInputCell();
        VariableType type = context.requireVariableType(cell);

        if (type instanceof DynamicCellType) {
            handleDynamicCell(context, get, (DynamicCellType) type);
        } else {
            throw new NotImplementedException(type.getClass());
        }
    }

    private void handleDynamicCell(TypeInferenceContext context, CellGetInstruction get, DynamicCellType cellType) {
        String output = get.getOutput();

        context.addVariable(output, cellType.getUnderlyingType(), context.getDefaultVariableType(output));
    }
}
