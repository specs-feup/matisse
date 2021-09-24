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

package org.specs.matisselib.helpers;

import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.typeinference.TypedInstance;

import com.google.common.base.Preconditions;

public class ConstantUtils {
    private ConstantUtils() {
    }

    public static boolean isConstantOne(TypedInstance instance, String variableName) {
        return isConstantOne(instance.getVariableType(variableName));
    }

    public static boolean isConstantOne(VariableType type) {
        return isEqualToValue(type, 1);
    }

    public static boolean isKnownPositiveInteger(VariableType type) {
        Preconditions.checkArgument(type != null);

        if (!ScalarUtils.isScalar(type)) {
            return false;
        }

        ScalarType scalarType = (ScalarType) type;
        Number constant = scalarType.scalar().getConstant();
        if (constant == null) {
            return false;
        }

        if (constant.intValue() != constant.doubleValue()) {
            return false;
        }

        return constant.intValue() > 0;
    }

    public static boolean isEqualToValue(VariableType type, int value) {
        Preconditions.checkArgument(type != null);

        if (!ScalarUtils.isScalar(type)) {
            return false;
        }

        ScalarType scalarType = (ScalarType) type;
        Number constant = scalarType.scalar().getConstant();
        if (constant == null) {
            return false;
        }

        return constant.doubleValue() == value;
    }

    public static boolean isPotentiallyNegative(VariableType type) {
        Preconditions.checkArgument(type != null);

        if (!ScalarUtils.isScalar(type)) {
            return true;
        }

        ScalarType scalarType = (ScalarType) type;

        if (scalarType.scalar().isUnsigned()) {
            return false;
        }

        Number constant = scalarType.scalar().getConstant();
        if (constant == null) {
            return true;
        }

        return constant.doubleValue() < 0;
    }

    public static boolean isConstantOne(Optional<VariableType> type) {
        return type.map(ConstantUtils::isConstantOne)
                .orElse(false);
    }

    public static boolean isKnownPositiveInteger(Optional<VariableType> type) {
        return type.map(ConstantUtils::isKnownPositiveInteger)
                .orElse(false);
    }

    public static boolean hasSameConstantValue(Optional<VariableType> type1, Optional<VariableType> type2) {
        if (!type1.isPresent()) {
            return false;
        }
        if (!type2.isPresent()) {
            return false;
        }

        return hasSameConstantValue(type1.get(), type2.get());
    }

    private static boolean hasSameConstantValue(VariableType type1, VariableType type2) {
        if (!ScalarUtils.hasConstant(type1) || !ScalarUtils.hasConstant(type2)) {
            return false;
        }

        return ScalarUtils.getConstant(type1).doubleValue() == ScalarUtils.getConstant(type2).doubleValue();
    }
}
