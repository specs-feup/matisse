package org.specs.matisselib.ssa;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class SsaBuilderEndContext {
    public final String cause;
    public final String referencedSsaVariable;
    public final int index;
    public final int numIndices;

    public SsaBuilderEndContext(String cause, String referencedSsaVariable, int index, int numIndices) {
        Preconditions.checkArgument(referencedSsaVariable != null);

        this.cause = cause;
        this.referencedSsaVariable = referencedSsaVariable;
        this.index = index;
        this.numIndices = numIndices;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(SsaBuilderEndContext.class)
                .add("cause", cause)
                .add("referencedSsaVariable", referencedSsaVariable)
                .add("index", index)
                .add("numIndices", numIndices)
                .toString();
    }
}
