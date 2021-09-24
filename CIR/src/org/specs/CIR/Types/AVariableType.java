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

import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.Types.Views.Conversion.DefaultConversion;
import org.specs.CIR.Types.Views.Pointer.DummyReference;
import org.specs.CIR.Types.Views.Pointer.Reference;

/**
 * @author Joao Bispo
 * 
 */
// public abstract class AVariableType<T> implements VariableType<T> {
public abstract class AVariableType implements VariableType {

    private boolean isImmutable;
    private boolean isWeakType;

    public AVariableType() {
	this.isImmutable = false;
	this.isWeakType = false;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    /**
     * As default, VariableType does not support pointer types.
     */
    @Override
    public Reference pointer() {
	return new DummyReference(this);
    }

    @Override
    public Conversion conversion() {
	return new DefaultConversion(this);
    }

    /**
     * As default, returns true.
     */
    @Override
    public boolean isReturnType() {
	return true;
    }

    /**
     * As default, returns false.
     */
    // @Override
    // public boolean usesDynamicAllocation() {
    // return false;
    // }

    @Override
    public int hashCode() {
	String id = getSmallId();
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	return result;
    }

    /**
     * Uses code().getSmallId() as unique identifier
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	AVariableType other = (AVariableType) obj;

	String id = getSmallId();
	String idOther = other.getSmallId();

	if (id == null) {
	    if (idOther != null) {
		return false;
	    }
	} else if (!id.equals(idOther)) {
	    return false;
	}
	return true;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#getSmallId()
     */
    @Override
    public String getSmallId() {
	throw new UnsupportedOperationException("At class " + getClass().getName());
	// return code().getSmallId();
    }

    /**
     * Classes extending this class must implement copyPrivate() for this function to work.
     * 
     * <p>
     */
    @Override
    public AVariableType copy() {
	// Copy type
	AVariableType copy = copyPrivate();

	// Set fields of abstract class
	copy.setImmutablePrivate(this.isImmutable);
	copy.setWeakTypePrivate(this.isWeakType);

	// Return copy
	return copy;
    }

    protected AVariableType copyPrivate() {
	throw new UnsupportedOperationException("Not implemented yet: " + getClass().getName());
    }

    @Override
    public Code code() {
	throw new UnsupportedOperationException("For type: " + getClass().getName());
    }

    /**
     * As default, it is set to false.
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#isImmutable()
     */
    @Override
    public boolean isImmutable() {
	return this.isImmutable;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#setImmutable(boolean)
     */
    @Override
    public VariableType setImmutable(boolean isImmutable) {
	// Create a copy
	AVariableType copy = copy();
	// Set the value of immutable
	copy.setImmutablePrivate(isImmutable);
	// Return copy
	return copy;
    }

    private void setImmutablePrivate(boolean isImmutable) {
	this.isImmutable = isImmutable;
    }

    /**
     * By default, returns 'false'
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#isWeakType()
     */
    @Override
    public boolean isWeakType() {
	return this.isWeakType;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#setWeakType(boolean)
     */
    @Override
    public VariableType setWeakType(boolean isWeakType) {
	// Create a copy
	AVariableType copy = copy();
	// Set the value of immutable
	copy.setWeakTypePrivate(isWeakType);
	// Return copy
	return copy;
    }

    private void setWeakTypePrivate(boolean isWeakType) {
	this.isWeakType = isWeakType;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#getContent()
     */
    /*
    @Override
    public Object getContent() {
    throw new UnsupportedOperationException();
    }
     */

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#getTypeId()
     */
    /*
    @Override
    public String getTypeId() {
    throw new UnsupportedOperationException();
    }
     */

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#getIncludes()
     */
    /*
    @Override
    public Set<String> getIncludes() {
    throw new UnsupportedOperationException();
    }
     */

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.VariableType#getType()
     */
    /*
    @Override
    public CType getType() {
    throw new UnsupportedOperationException();
    }
     */

}
