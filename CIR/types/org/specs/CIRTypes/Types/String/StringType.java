/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIRTypes.Types.String;

import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.Matrix;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Pointer.Reference;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

/**
 * @author Joao Bispo
 * 
 */
public final class StringType extends MatrixType {

    private final String string;
    private final NumericTypeV2 charType;
    private final boolean isView;
    private final boolean isConstant;
    private final boolean isReference;

    private StringType(String string, int charBitSize, boolean isView, boolean isConstant, boolean isReference) {
        this.string = string;
        this.charType = NumericTypeV2.newInstance(CTypeV2.CHAR, charBitSize);
        this.isView = isView;
        this.isConstant = isConstant;
        this.isReference = isReference;
    }

    /**
     * Helper constructor where 'isConstant' is set to false.
     * 
     * @param string
     * @param charBitSize
     * @param isView
     */
    private StringType(String string, int charBitSize, boolean isView) {
        this(string, charBitSize, isView, false, false);
    }

    /**
     * Creates a new string which uses 8 bits per character.
     * 
     * @param string
     * @param charBits
     * @return
     */
    /*
    public static VariableType create(String string) {
    return StringType.create(string, 8);
    }
    */

    @Override
    public boolean canBeAssignmentCopied() {
        return true;
    }

    public static StringType create(String string, int charBitSize, boolean isConstant) {
        return new StringType(string, charBitSize, false, isConstant, false);
    }

    public static StringType create(String string, int charBitSize) {
        return new StringType(string, charBitSize, false, false, false);
    }

    public static StringType createView(StringType type, boolean isView) {
        return new StringType(type.string, type.charType.scalar().getBits(), isView);
    }

    public String getString() {
        return this.string;
    }

    public ScalarType getCharType() {
        return this.charType;
    }

    public boolean isView() {
        return this.isView;
    }

    public boolean isConstant() {
        return this.isConstant;
    }

    @Override
    public String getSmallId() {
        return "s";
    }

    @Override
    public Code code() {
        return new StringCode(this);
    }

    @Override
    public Matrix matrix() {
        return new StringMatrix(this);
    }

    @Override
    public Reference pointer() {
        return new Reference() {

            @Override
            public boolean supportsReference() {
                return true;
            }

            @Override
            public boolean isByReference() {
                return isReference;
            }

            @Override
            public VariableType getType(boolean isByReference) {
                return new StringType(string, charType.getBits(), isView, isConstant, isByReference);
            }
        };
    }

    @Override
    public boolean strictEquals(VariableType type) {
        if (type instanceof StringType) {
            return strictEquals((StringType) type);
        }

        return false;
    }

    public boolean strictEquals(StringType type) {
        /*     private final String string;
        private final ScalarType charType;
        private final boolean isView;
        private final boolean isConstant;
         */

        if (type == null) {
            return false;
        }

        if (this.string == null) {
            if (type.string != null) {
                return false;
            }
        } else if (!this.string.equals(type.string)) {
            return false;
        }

        if (!this.charType.strictEquals(type.charType)) {
            return false;
        }

        if (this.isView != type.isView) {
            return false;
        }

        if (this.isReference != type.isReference) {
            return false;
        }

        return this.isConstant == type.isConstant;
    }

}
