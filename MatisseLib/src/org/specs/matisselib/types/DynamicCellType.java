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

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Pointer.Reference;

import com.google.common.base.Preconditions;

public final class DynamicCellType extends CellArrayType {
    private final VariableType underlyingType;
    private final TypeShape typeShape;
    private FunctionInstance structInstance;
    private final boolean reference;

    public DynamicCellType(VariableType underlyingType, TypeShape typeShape) {
        this(underlyingType, typeShape, false);
    }

    private DynamicCellType(VariableType underlyingType, TypeShape typeShape, boolean reference) {
        Preconditions.checkArgument(underlyingType != null);
        Preconditions.checkArgument(typeShape != null);

        this.underlyingType = underlyingType;
        this.typeShape = typeShape;
        this.reference = reference;
    }

    public VariableType getUnderlyingType() {
        return this.underlyingType;
    }

    @Override
    public TypeShape getTypeShape() {
        return this.typeShape;
    }

    @Override
    public Code code() {
        return new DynamicCellCode(this);
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
                return reference;
            }

            @Override
            public VariableType getType(boolean isByReference) {
                return new DynamicCellType(underlyingType, typeShape, isByReference);
            }
        };
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof DynamicCellType &&
                strictEquals((DynamicCellType) type);
    }

    public boolean strictEquals(DynamicCellType type) {
        if (type == null) {
            return false;
        }

        return this.underlyingType.strictEquals(type.underlyingType) &&
                this.typeShape.equals(type.typeShape);
    }

    @Override
    public String getSmallId() {
        return "cell" + this.underlyingType.getSmallId();
    }

    @Override
    public String toString() {
        return "DynamicCellType(" + this.underlyingType + ", shape=" + this.typeShape + ")";
    }

    @Override
    public boolean usesDynamicAllocation() {
        return true;
    }

    public synchronized FunctionInstance getStructInstance() {
        if (this.structInstance == null) {
            this.structInstance = DynamicCellStruct.newInstance(this.underlyingType);
        }

        return this.structInstance;
    }

    @Override
    public Cell cell() {
        return new DynamicCell(this);
    }
}
