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

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Conversion.Conversion;

public class LogicalConversion implements Conversion {

    @Override
    public boolean isConvertibleTo(VariableType type) {
        return type instanceof LogicalType;
    }

    @Override
    public CNode to(CNode token, VariableType type) {
        if (!isConvertibleTo(type)) {
            return null;
        }

        return token;
    }

    @Override
    public boolean isConvertibleToSelf(VariableType type) {
        return type instanceof LogicalType;
    }

    @Override
    public CNode toSelf(CNode token) {
        if (!isConvertibleToSelf(token.getVariableType())) {
            return null;
        }

        return token;
    }

    @Override
    public boolean isAssignable(VariableType targetType) {
        return targetType instanceof LogicalType;
    }

}
