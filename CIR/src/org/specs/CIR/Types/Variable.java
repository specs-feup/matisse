/**
 * Copyright 2012 SPeCS Research Group.
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

import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;

/**
 * Contents for CToken of type assignment.
 * 
 * @author Joao Bispo
 * 
 */
// public class Variable<T extends VariableType> {
public class Variable {

    private final String name;
    private final VariableType type;
    private final boolean isGlobal;

    /**
     * @param name
     * @param type
     * @param index
     */
    public Variable(String name, VariableType type, boolean isGlobal) {
        this.name = name;
        this.type = type;
        this.isGlobal = isGlobal;
    }

    public Variable(String name, VariableType type) {
        this(name, type, false);
    }

    /**
     * @return the type
     */
    public VariableType getType() {
        return type;
    }

    /*
    public <T> T getType(Class<T> typeClass) {
    return typeClass.cast(type);
    }
    */

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(name);
        builder.append(" - ");
        builder.append(type);
        if (isGlobal) {
            builder.append(" (global)");
        }

        return builder.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        if (isGlobal) {
            result ^= 0xFFFFFFFF;
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Variable other = (Variable) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        else if (isGlobal != other.isGlobal) {
            return false;
        }
        return true;
    }

    /**
     * If the type of this variable needs pointer conversion when passed as input, converts the type to a pointer.
     */
    /*
    public void convertToPointerType() {
    // Check if convertible
    if (!PointerUtils.supportsPointer(getType())) {
        return;
    }
    
    VariableType pointerType = PointerUtils.getType(getType(), true);
    this.type = pointerType;
    
    }
    */

    /**
     * If the type of this variable needs pointer conversion when passed as input, converts the type to a pointer.
     */
    public Variable getPointerType() {
        // Check if convertible
        if (!ReferenceUtils.supportsPointer(getType())) {
            return this;
        }

        VariableType pointerType = ReferenceUtils.getType(getType(), true);

        return new Variable(getName(), pointerType, isGlobal);
    }

}
