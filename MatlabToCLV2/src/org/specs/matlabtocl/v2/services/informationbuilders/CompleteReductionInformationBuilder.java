package org.specs.matlabtocl.v2.services.informationbuilders;

import org.specs.matisselib.helpers.sizeinfo.InstructionInformationBuilder;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.helpers.sizeinfo.SizeInfoBuilderContext;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;

public class CompleteReductionInformationBuilder implements InstructionInformationBuilder {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof CompleteReductionInstruction;
    }

    @Override
    public SizeGroupInformation apply(SizeInfoBuilderContext ctx, SsaInstruction instruction) {
        CompleteReductionInstruction complete = (CompleteReductionInstruction) instruction;

        // Treat complete reduction as a matrix copy
        SsaInstruction replacementInstruction = AssignmentInstruction.fromVariable(complete.getOutput(),
                complete.getInitialValue());
        return ctx.handleInstruction(ctx, ctx.getCurrentInfo(), -1, null, replacementInstruction);
    }

}
