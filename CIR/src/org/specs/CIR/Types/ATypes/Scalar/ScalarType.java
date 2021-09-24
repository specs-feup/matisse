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

package org.specs.CIR.Types.ATypes.Scalar;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.TypeShape;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * When a VariableType represents a numeric scalar.
 * 
 * @author Joao Bispo
 * 
 */
// public abstract class ScalarType<T> extends AVariableType<T> implements Comparable<ScalarType<?>> {
public abstract class ScalarType extends AVariableType implements Comparable<ScalarType> {

    public abstract Scalar scalar();

    /**
     * A ScalarType is considered bigger than a second scalar, if the second one fits into the first one, but the second
     * one does not fit into the first one. <br>
     * 
     * If both fit inside each other, they are considered of equal size. <br>
     * 
     * It can happen that none of the types fit. In this case, the method throws a RuntimeException.
     * 
     * 
     * <p>
     * Note: this class has a natural ordering that is inconsistent with equals.
     * 
     * @param o
     * @return
     */
    @Override
    public int compareTo(ScalarType o) {
	boolean objectFitsIntoThis = ScalarUtils.fitsInto(o, this);
	boolean thisFitsIntoObject = ScalarUtils.fitsInto(this, o);
	// Boolean objectFitsIntoThis = o.scalar().fitsInto(this);
	// Boolean thisFitsIntoObject = this.scalar().fitsInto(o);

	// System.out.println(o + " max:" + o.scalar().getMaxValue());
	// System.out.println(o + " min:" + o.scalar().getMinValue());
	// System.out.println(this + " max:" + this.scalar().getMaxValue());
	// System.out.println(this + " min:" + this.scalar().getMinValue());

	// If both fit each other
	if (objectFitsIntoThis && thisFitsIntoObject) {
	    return 0;
	}

	// If 'object' fits but 'this' does not, 'this' is bigger
	if (objectFitsIntoThis && !thisFitsIntoObject) {
	    return 1;
	}

	// If 'this' fits but 'object' does not, 'this' is smaller
	// if (objectFitsIntoThis && !thisFitsIntoObject) {
	if (!objectFitsIntoThis && thisFitsIntoObject) {
	    return -1;
	}

	SpecsLogs.msgLib("Comparison between types failed:");
	SpecsLogs.msgLib("'" + o + "' fits inside '" + this + "'? " + objectFitsIntoThis);
	SpecsLogs.msgLib("'" + this + "' fits inside '" + o + "'? " + thisFitsIntoObject);

	// If none fit, throw exception
	// throw new RuntimeException("Types '" + this + "' and '" + o + "' cannot be compared");
	SpecsLogs
		.msgLib("- Comparison between types '" + this + "' and '" + o + "'. Considering types equivalent.");
	return 0;
    }

    /**
     * Returns a scalar shape.
     */
    @Override
    public TypeShape getTypeShape() {
	return TypeShape.newScalarShape();
    }
}
