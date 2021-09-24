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

package org.specs.CIR.Types;

import org.specs.CIR.Types.Views.Code.CodeView;
import org.specs.CIR.Types.Views.Conversion.ConversionView;
import org.specs.CIR.Types.Views.Pointer.ReferenceView;

public interface VariableType extends ReferenceView, ConversionView, CodeView {

    /**
     * Indicates if the type can be returned by a C function. For instance, if a type is composed by separate variables
     * (e.g., an implementation of the complex type), it will need to be always passed as input, and it can imply
     * transformations in the tree (e.g., nested function calls).
     * 
     * @return true if the given type can be returned by a C function, false otherwise
     */
    boolean isReturnType();

    /**
     * By default, returns false.
     * 
     * @return true, if the given type uses dynamic memory allocation
     */
    default boolean usesDynamicAllocation() {
        return false;
    }

    /**
     * Indicates whether A = B is a valid way to copy B to A.
     */
    default boolean canBeAssignmentCopied() {
        return true;
    }

    /**
     * @return a copy of the current VariableType
     */
    VariableType copy();

    /**
     * Small string to be used in function names to identify the types.
     * 
     * <p>
     * This method exists in CodeView.
     * 
     * @return
     */
    String getSmallId();

    /**
     * A type is considered immutable if the contents of the variable associated with it do not change during its
     * lifetime.
     * 
     * TODO: Consider moving this information to ProviderData.
     * 
     * @return the isConstant
     */
    public boolean isImmutable();

    /**
     * Returns a copy of the VariableType, set with value given for 'isImmutable'.
     * 
     * TODO: Consider moving this information to ProviderData.
     * 
     * @param isConstant
     *            the isConstant to set
     */
    public VariableType setImmutable(boolean isImmutable);

    /**
     * A type is considered weak if there are no assurances about it, and is just a best guess.
     * <p>
     * It can be the case, for instance, of a type that was inferred from a literal, since it could be initialized with
     * the value '0' (integer), but intended to be used as a floating point.
     * 
     * @return true if the type is marked as weak, false otherwise
     */
    public boolean isWeakType();

    /**
     * Returns a copy of the VariableType, set with value given for 'isWeakType'.
     * 
     * @param isConstant
     *            the isConstant to set
     */
    public VariableType setWeakType(boolean isWeakType);

    /**
     * By default, performs the following actions:<br>
     * <br>
     * - Sets pointer status to false<br>
     * 
     * @return returns the current type, normalized
     */
    default VariableType normalize() {
        VariableType newType = this;
        if (pointer().isByReference()) {
            newType = newType.pointer().getType(false);
        }

        return newType;
    }

    /**
     * If the given type has the same reference as the current type, returns a copy. Otherwise, returns the type itself.
     * 
     * <p>
     * We can use this method to ensure that after working over a type, we do not return the original type, but a copy.
     * 
     * @return
     */
    default VariableType makeUnique(VariableType type) {
        if (type == this) {
            return this.copy();
        }

        return this;
    }

    /**
     * The shape of the type. As default, returns an Undefined shape.
     * 
     * @return
     */
    default TypeShape getTypeShape() {
        return TypeShape.newUndefinedShape();
    }

    /**
     * An equals method that follows Java's equals semantics. This is not called "equals" due to legacy code that relies
     * on the old behavior for equals.
     */
    boolean strictEquals(VariableType type);

    default CommonFunctions functions() {
        return new DefaultFunctions();
    }

}