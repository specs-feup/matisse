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

package org.specs.matisselib.matlabinference;

import org.specs.CIR.Types.TypeShape;
import org.specs.MatlabIR.MatlabLanguage.MatlabClass;

public class MatlabType {

    private final Boolean isInteger;
    private final Boolean isUnsigned;
    private final Boolean isComplex;
    private final TypeShape shape;
    private final MatlabClass mclass;

    private MatlabType(Boolean isInteger, Boolean isUnsigned, Boolean isComplex, TypeShape shape, MatlabClass mclass) {
        this.isInteger = isInteger;
        this.isUnsigned = isUnsigned;
        this.isComplex = isComplex;
        this.shape = shape;
        this.mclass = mclass;
    }

    /**
     * Creates a new scalar type.
     * 
     * @param mclass
     * @return
     */
    public static MatlabType newScalar(MatlabClass mclass) {
        Boolean isInteger = mclass.isInteger() ? true : null;
        Boolean isUnsigned = mclass.isUnsigned() ? true : null;
        return new MatlabType(isInteger, isUnsigned, null, TypeShape.newScalarShape(), mclass);
    }

    public Boolean isComplex() {
        return isComplex;
    }

    public Boolean isInteger() {
        return isInteger;
    }

    public Boolean isUnsigned() {
        return isUnsigned;
    }

    public MatlabClass getMclass() {
        return mclass;
    }

    public TypeShape getShape() {
        return shape;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(mclass).append("(").append(")");

        return builder.toString();
    }

    public MatlabType setInteger(Boolean isInteger) {
        return new MatlabType(isInteger, isUnsigned, isComplex, shape, mclass);
    }
}
