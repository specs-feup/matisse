package org.specs.matlabtocl.v2.codegen.reductionvalidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.matisselib.ssa.InstructionLocation;

public final class GeneralReductionFormatValidationResult {
    private final List<InstructionLocation> constructionInstructions;
    private final List<InstructionLocation> midUsageInstructions;
    private final List<String> reductionNames;

    public GeneralReductionFormatValidationResult(List<InstructionLocation> constructionInstructions,
            List<InstructionLocation> midUsageInstructions,
            List<String> reductionNames) {

        this.constructionInstructions = new ArrayList<>(constructionInstructions);
        this.midUsageInstructions = new ArrayList<>(midUsageInstructions);
        this.reductionNames = reductionNames;
    }

    public List<InstructionLocation> getConstructionInstructions() {
        return Collections.unmodifiableList(constructionInstructions);
    }

    public List<InstructionLocation> getMidUsageInstructions() {
        return Collections.unmodifiableList(midUsageInstructions);
    }

    public List<String> getReductionNames() {
        return Collections.unmodifiableList(reductionNames);
    }
}
