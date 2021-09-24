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

import org.specs.CIR.Types.ATypes.Scalar.AScalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

/**
 * 
 * @author JoaoBispo
 */
class LogicalScalar extends AScalar {

    private final LogicalType type;

    public LogicalScalar(LogicalType type) {
        super(type);

        this.type = type;
    }

    @Override
    public ScalarType removeConstant() {
        return LogicalType.newInstance();
    }

    @Override
    protected ScalarType setLiteralPrivate(boolean isLiteral) {
        return type;
    }

    @Override
    protected ScalarType setConstantPrivate(String constant) {
        if (constant == null) {
            return LogicalType.newInstance();
        }

        return LogicalType.newInstance(Double.parseDouble(constant) != 0);
    }

    @Override
    public boolean hasConstant() {
        return type.getConstant() != null;
    }

    @Override
    public String getConstantString() {
        if (type.getConstant() == null) {
            return null;
        }

        return type.getConstant() ? "1" : "0";
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public int getBits() {
        // return 1;
        return type.getImplementation().scalar().getBits();
    }

    @Override
    public boolean isUnsigned() {
        return type.getImplementation().scalar().isUnsigned();
    }

}
