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

package org.specs.matlabtocl.v2.codegen;

import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Logical.LogicalType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StdInd.StdIntType;
import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class CLTypeDecorator {

    private final MatrixTypeChooser matrixTypeChooser;

    public CLTypeDecorator(MatrixTypeChooser matrixTypeChooser) {
        this.matrixTypeChooser = matrixTypeChooser;
    }

    public VariableType decorateType(String variableName, VariableType type) {
        if (type instanceof MatrixType) {
            CLNativeType underlyingCLType = processScalarType(variableName,
                    ((MatrixType) type).matrix().getElementType());

            return this.matrixTypeChooser.buildMatrixType(variableName, AddressSpace.GLOBAL, underlyingCLType);
        }
        if (type instanceof ScalarType) {
            return processScalarType(variableName, (ScalarType) type);
        }

        return type;
    }

    public CLNativeType processScalarType(String variableName, ScalarType type) {
        Number constant = type.scalar().getConstant();

        return getSimpleProcessedScalarType(variableName, type)
                .setConstant(constant);
    }

    private static CLNativeType getSimpleProcessedScalarType(String variableName, ScalarType type) {
        if (type instanceof CLNativeType) {
            return (CLNativeType) type;
        }

        if (type instanceof NumericTypeV2) {
            NumericTypeV2 numericType = (NumericTypeV2) type;
            CTypeV2 cType = numericType.cnative().getCType();

            if (!cType.isInteger()) {
                if (cType.getAtLeastBits() == 32) {
                    return CLNativeType.FLOAT;
                }
                if (cType.getAtLeastBits() == 64) {
                    return CLNativeType.DOUBLE;
                }
            } else {
                if (cType.isUnsigned()) {
                    switch (cType.getAtLeastBits()) {
                    case 8:
                        return CLNativeType.UCHAR;
                    case 16:
                        return CLNativeType.UINT; // force C unsigned to uint
                    case 32:
                        return CLNativeType.UINT;
                    case 64:
                        return CLNativeType.ULONG;
                    default:
                        throw new NotImplementedException("Unsigned with " + cType.getAtLeastBits() + " bits.");
                    }
                }

                switch (cType.getAtLeastBits()) {
                case 8:
                    return CLNativeType.CHAR;
                case 16:
                    return CLNativeType.INT; // Force C int to OpenCL int
                case 32:
                    return CLNativeType.INT;
                case 64:
                    return CLNativeType.LONG;
                default:
                    throw new NotImplementedException("Integer with " + cType.getAtLeastBits() + " bits.");
                }
            }
        }
        if (type instanceof StdIntType) {
            StdIntType stdIntType = (StdIntType) type;

            if (stdIntType.isUnsigned()) {
                switch (stdIntType.getnBits()) {
                case 8:
                    return CLNativeType.UCHAR;
                case 16:
                    return CLNativeType.USHORT;
                case 32:
                    return CLNativeType.UINT;
                case 64:
                    return CLNativeType.ULONG;
                default:
                    throw new NotImplementedException("Unsigned Integer with " + stdIntType.getnBits() + " bits.");
                }
            }

            switch (stdIntType.getnBits()) {
            case 8:
                return CLNativeType.CHAR;
            case 16:
                return CLNativeType.SHORT;
            case 32:
                return CLNativeType.INT;
            case 64:
                return CLNativeType.LONG;
            default:
                throw new NotImplementedException("Integer with " + stdIntType.getnBits() + " bits.");
            }
        }
        if (type instanceof LogicalType) {
            return CLNativeType.UCHAR;
        }

        throw new NotImplementedException(variableName + ", of type " + type + ", class " + type.getClass() + "");
    }
}
