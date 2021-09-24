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

package org.specs.matlabtocl.v2.types.kernel;

import java.util.Optional;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNative;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.matlabtocl.v2.functions.builtins.CastOperator;

public class CLNativeConversion implements Conversion {

    private final CLNativeType self;

    CLNativeConversion(CLNativeType self) {
        this.self = self;
    }

    @Override
    public boolean isConvertibleTo(VariableType type) {
        return false;
    }

    @Override
    public CNode to(CNode token, VariableType type) {
        return null;
    }

    @Override
    public boolean isConvertibleToSelf(VariableType type) {
        if (type instanceof CNativeType) {
            return isConvertibleToSelf((CNativeType) type);
        }

        return false;
    }

    public boolean isConvertibleToSelf(CNativeType type) {
        CNative cnative = type.cnative();
        if (cnative.getCType().isInteger() != this.self.isInteger()) {
            return false;
        }
        boolean selfUnsigned = this.self.isUnsigned();
        boolean unsigned = cnative.getCType().isUnsigned();
        if (!unsigned && selfUnsigned) {
            // I bet we can handle this better
            if (!type.scalar().hasConstant() || type.scalar().getConstantString().contains("-")) {
                return false;
            }
        }

        Optional<Integer> selfbits = this.self.getBits();
        int nbits = cnative.getCType().getAtLeastBits();

        if (selfbits.isPresent()) {
            if (selfUnsigned != unsigned) {
                return selfbits.get() > nbits;
            }
            return selfbits.get() >= nbits;
        }

        // Casting to size_t
        if (unsigned) {
            return nbits <= 32;
        }
        return nbits < 32;
    }

    @Override
    public CNode toSelf(CNode token) {
        Optional<String> suffix = self.getSuffix();
        if (token instanceof CNumberNode && suffix.isPresent()) {
            String suffixVal = suffix.get();

            CNumber cNumber = ((CNumberNode) token).getCNumber();
            if (cNumber.isInteger()) {
                if (self.isInteger()) {
                    Long value = Long.parseLong(cNumber.toCString());
                    if (value >= 0 && value < 256) {
                        return CNodeFactory.newCNumber((int) (long) value, self);
                    }
                    // TODO
                } else {
                    return CNodeFactory.newLiteral(cNumber.toCString() + suffixVal, self, PrecedenceLevel.Atom);
                }
            } else {
                if (self.isInteger()) {
                    // TODO
                } else {
                    return CNodeFactory.newCNumber(cNumber.getNumber(), self);
                }
            }
        }

        return CastOperator.build(token.getVariableType(), self).newFunctionCall(token);
    }

    @Override
    public boolean isAssignable(VariableType targetType) {
        return false;
    }

}
