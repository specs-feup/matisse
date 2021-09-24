/**
 * Copyright 2017 SPeCS.
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

package org.specs.CIRTypes.Types.Logical;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNative;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.Types.Views.Pointer.Reference;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

public class LogicalType extends CNativeType {

    private final CNativeType implementation;
    private final Boolean constant;
    private final boolean isByReference;

    private static final LogicalType LOGICAL_TYPE = new LogicalType(null, false);
    private static final LogicalType LOGICAL_TYPE_TRUE = new LogicalType(true, false);
    private static final LogicalType LOGICAL_TYPE_FALSE = new LogicalType(false, false);

    public static LogicalType newInstance() {
        return LOGICAL_TYPE;
    }

    public static LogicalType newInstance(Boolean constant) {
        if (constant == null) {
            return LOGICAL_TYPE;
        }

        return constant ? LOGICAL_TYPE_TRUE : LOGICAL_TYPE_FALSE;
    }

    private LogicalType(Boolean constant, boolean ref) {
        this.implementation = buildInternalType(constant, ref);
        this.constant = constant;
        this.isByReference = ref;
    }

    private static CNativeType buildInternalType(Boolean constant, boolean ref) {
        StdIntType int8 = StdIntFactory.newInt8();

        int8 = int8.setPointer(ref);

        if (constant == null) {
            return int8;
        }

        return constant ? int8.setConstant(1) : int8.setConstant(0);
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof LogicalType;
    }

    @Override
    public Scalar scalar() {
        // return implementation.scalar();
        return new LogicalScalar(this);
    }

    public Boolean getConstant() {
        return constant;
    }

    public CNativeType getImplementation() {
        return implementation;
    }

    @Override
    public Code code() {
        return implementation.code();
    }

    @Override
    public Reference pointer() {
        return new LogicalReference(this);
    }

    public boolean isByReference() {
        return isByReference;
    }

    LogicalType byReference(boolean isByReference) {
        if (!isByReference) {
            return newInstance(constant);
        }
        return new LogicalType(constant, isByReference);
    }

    @Override
    public String getSmallId() {
        return "b" + (isByReference ? "p" : "");
    }

    // // @Override
    // public CNativeType toCNativeType() {
    // return implementation;
    // }

    @Override
    public CNative cnative() {
        return implementation.cnative();
    }

    @Override
    protected AVariableType copyPrivate() {
        return this;
    }

    @Override
    public Conversion conversion() {
        return new LogicalConversion();
    }

    @Override
    public String toString() {
        return (isByReference ? "ref " : "") + (constant == null ? "LogicalType" : "LogicalType(" + constant + ")");
    }

    // @Override
    // public Conversion conversion() {
    // System.out.println("CONVERSION");
    // return super.conversion();
    // // return implementation.conversion();
    // }

}
