/**
 * Copyright 2014 SPeCS.
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

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.AMatrix;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public class StringMatrix extends AMatrix {

    private final StringType type;
    private final TypeShape shape;

    public StringMatrix(StringType type) {
        this.type = type;
        shape = TypeShape.newInstance(1, type.getString().length());
    }

    @Override
    public MatrixFunctions functions() {
        return new StringFunctions(type);
    }

    /**
     * The shape will be 1 by (<size of string> + 1). The additional 1 is to account for the terminator '\0'.
     */
    @Override
    public TypeShape getShape() {
        return shape;
    }

    @Override
    public boolean isView() {
        return type.isView();
    }

    @Override
    public MatrixType getView() {
        if (type.isView()) {
            return type;
        }

        return StringType.createView(type, true);
    }

    /**
     * The element is always a char.
     */
    @Override
    public ScalarType getElementType() {
        return type.getCharType();
    }

    @Override
    public boolean usesDynamicAllocation() {
        return false;
    }

}
