package org.specs.matisselib.types.strings;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Pointer.Reference;

public class MatlabStringReference implements Reference {

    private MatlabStringType type;

    public MatlabStringReference(MatlabStringType type) {
        this.type = type;
    }

    @Override
    public boolean isByReference() {
        return type.isByReference();
    }

    @Override
    public boolean supportsReference() {
        return true;
    }

    @Override
    public VariableType getType(boolean isByReference) {
        return isByReference ? MatlabStringType.STRING_REFERENCE_TYPE : MatlabStringType.STRING_TYPE;
    }

}
