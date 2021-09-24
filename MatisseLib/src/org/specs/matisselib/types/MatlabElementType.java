/**
 * Copyright 2016 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matisselib.types;

import java.util.Locale;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Pointer.Reference;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

import com.google.common.base.Preconditions;

public final class MatlabElementType extends ScalarType {

    public static final MatlabElementType DOUBLE = new MatlabElementType(MatlabTypeGroup.DOUBLE);
    public static final MatlabElementType SINGLE = new MatlabElementType(MatlabTypeGroup.SINGLE);

    private final MatlabTypeGroup typeGroup;
    private final String constant;
    private final boolean isReference;

    private MatlabElementType(MatlabTypeGroup typeGroup) {
        this(typeGroup, false, null);
    }

    private MatlabElementType(MatlabTypeGroup typeGroup, boolean isReference, String constant) {
        this.typeGroup = typeGroup;
        this.isReference = isReference;
        this.constant = constant;
    }

    @Override
    protected AVariableType copyPrivate() {
        return new MatlabElementType(typeGroup, isReference, constant);
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof MatlabElementType && strictEquals((MatlabElementType) type);
    }

    public boolean strictEquals(MatlabElementType type) {
        if (type == null) {
            return false;
        }

        if (typeGroup != type.typeGroup) {
            return false;
        }

        if (isReference != type.isReference) {
            return false;
        }

        if (constant != type.constant) {
            return false;
        }

        return true;
    }

    String getConstantString() {
        return constant;
    }

    MatlabElementType setConstantString(String constant) {
        return new MatlabElementType(typeGroup, isReference, constant);
    }

    boolean isReference() {
        return isReference;
    }

    MatlabElementType setReference(boolean reference) {
        return new MatlabElementType(typeGroup, reference, constant);
    }

    @Override
    public Scalar scalar() {
        return new MatlabScalar(this);
    }

    @Override
    public Reference pointer() {
        return new MatlabReference(this);
    }

    @Override
    public Code code() {
        return new MatlabCode(this);
    }

    @Override
    public String getSmallId() {
        return "m" + typeGroup.getSmallId();
    }

    String getSimpleType() {
        return typeGroup.getCode();
    }

    SystemInclude getIncludes() {
        return typeGroup.getInclude();
    }

    boolean isInteger() {
        return typeGroup != MatlabTypeGroup.DOUBLE && typeGroup != MatlabTypeGroup.SINGLE;
    }

    /**
     * 
     * @param defaultReal
     *            a NumericTypeV2
     * @return
     */
    public static MatlabElementType getDefaultNumberType(VariableType defaultReal) {
        Preconditions.checkArgument(defaultReal instanceof NumericTypeV2);

        return ((NumericTypeV2) defaultReal).getCtype() == CTypeV2.DOUBLE
                ? MatlabElementType.DOUBLE
                : MatlabElementType.SINGLE;
    }

    public ScalarType getUnderlyingCType(NumericFactory numericFactory) {
        return typeGroup.getUnderlyingCType(numericFactory);
    }

    public MatlabTypeGroup getTypeGroup() {
        return typeGroup;
    }

    @Override
    public String toString() {
        return "matlab." + typeGroup.toString().toLowerCase(Locale.UK);
    }
}
