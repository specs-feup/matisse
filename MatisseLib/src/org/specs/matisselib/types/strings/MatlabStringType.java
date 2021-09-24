package org.specs.matisselib.types.strings;

import java.util.Optional;

import org.specs.CIR.Types.CommonFunctions;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.AScalar;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Pointer.Reference;

public final class MatlabStringType extends ScalarType {

    private boolean isPointer;

    public static final MatlabStringType STRING_TYPE = new MatlabStringType(false);
    public static final MatlabStringType STRING_REFERENCE_TYPE = new MatlabStringType(true);
    public static final String DECLARATION_FILENAME = "lib/matlabstring_struct";

    private MatlabStringType(boolean isPointer) {
        this.isPointer = isPointer;
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof MatlabStringType;
    }

    @Override
    public boolean canBeAssignmentCopied() {
        return false;
    }

    @Override
    public Scalar scalar() {
        return new AScalar(this) {

            @Override
            public ScalarType removeConstant() {
                return MatlabStringType.this;
            }

            @Override
            protected ScalarType setLiteralPrivate(boolean isLiteral) {
                return MatlabStringType.this;
            }

            @Override
            protected ScalarType setConstantPrivate(String constant) {
                return MatlabStringType.this;
            }

            @Override
            public boolean isInteger() {
                return false;
            }

            @Override
            public boolean isNumber() {
                return false;
            }

            @Override
            public Optional<Boolean> fitsInto(ScalarType targetType) {
                return Optional.of(targetType instanceof MatlabStringType);
            }
        };
    }

    @Override
    public Reference pointer() {
        return new MatlabStringReference(this);
    }

    public boolean isByReference() {
        return isPointer;
    }

    @Override
    public boolean usesDynamicAllocation() {
        return true;
    }

    @Override
    public Code code() {
        return new MatlabStringCode(this);
    }

    @Override
    public String getSmallId() {
        return "S";
    }

    @Override
    public CommonFunctions functions() {
        return new MatlabStringFunctions();
    }
}
