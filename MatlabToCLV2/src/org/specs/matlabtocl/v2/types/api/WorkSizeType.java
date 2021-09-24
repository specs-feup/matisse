/**
 * Copyright 2015 SPeCS.
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

package org.specs.matlabtocl.v2.types.api;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.AScalar;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;

import com.google.common.base.Preconditions;

public final class WorkSizeType extends ScalarType {
    private final Integer constant;

    public static final WorkSizeType BASE_TYPE = new WorkSizeType(null);

    private WorkSizeType(Integer constant) {
        this.constant = constant;
    }

    public static WorkSizeType ofValue(int constant) {
        Preconditions.checkArgument(constant > 0);

        return new WorkSizeType(constant);
    }

    @Override
    public Scalar scalar() {
        return new AScalar(this) {

            @Override
            protected ScalarType setLiteralPrivate(boolean isLiteral) {
                return WorkSizeType.this;
            }

            @Override
            protected ScalarType setConstantPrivate(String constant) {
                if (constant.contains(".")) {
                    constant = constant.substring(0, constant.indexOf('.'));
                }

                int constantValue = Integer.parseInt(constant, 10);

                return new WorkSizeType(constantValue);
            }

            @Override
            public Number getConstant() {
                return constant;
            }

            @Override
            public boolean hasConstant() {
                return constant != null;
            }

            @Override
            public ScalarType removeConstant() {
                return BASE_TYPE;
            }

            @Override
            public ScalarType setLiteral(boolean isLiteral) {
                return WorkSizeType.this;
            }

            @Override
            public boolean isInteger() {
                return true;
            }
        };
    }

    @Override
    public WorkSizeType setWeakType(boolean isWeakType) {
        return this;
    }

    @Override
    protected WorkSizeType copyPrivate() {
        return this;
    }

    @Override
    public String getSmallId() {
        return "st";
    }

    @Override
    public Code code() {
        return new WorkSizeCode(this);
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof WorkSizeType;
    }

    @Override
    public String toString() {
        if (constant == null) {
            return "WorkSizeType";
        }
        return "WorkSizeType(" + constant + ")";
    }
}
