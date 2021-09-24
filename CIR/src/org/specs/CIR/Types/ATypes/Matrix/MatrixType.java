/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIR.Types.ATypes.Matrix;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.Utils.ToScalar;

/**
 * When a VariableType represents a matrix.
 * 
 * @author Joao Bispo
 * 
 */
public abstract class MatrixType extends AVariableType implements ToScalar {

    @Override
    public boolean canBeAssignmentCopied() {
        return false;
    }

    public abstract Matrix matrix();

    @Override
    public TypeShape getTypeShape() {
        return matrix().getShape();
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Scalar.Utils.ToScalar#toScalarType()
     */
    @Override
    public ScalarType toScalarType() {
        return matrix().getElementType();
    }

    @Override
    public MatrixType copy() {
        return (MatrixType) super.copy();
    }

    @Override
    public MatrixFunctions functions() {
        return matrix().functions();
    }
}
