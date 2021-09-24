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

package org.specs.CIR.Types.Views.Pointer;

import java.util.List;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class ReferenceUtils {

    /**
     * TODO: check uses to see if they should be replaced with VariableType.normalize
     * 
     * @param type
     * @param isPointer
     * @return
     */
    public static <T extends VariableType> VariableType getType(T type, boolean isPointer) {
	return type.pointer().getType(isPointer);
    }

    public static <T extends VariableType> List<VariableType> getType(List<T> outputTypes, boolean isPointer) {
	List<VariableType> parsedTypes = SpecsFactory.newArrayList();

	for (VariableType outType : outputTypes) {
	    parsedTypes.add(getType(outType, isPointer));
	}

	return parsedTypes;
    }

    public static boolean supportsPointer(VariableType type) {
	return type.pointer().supportsReference();
    }

    /**
     * Returns true if the given VariableType is a pointer.
     * 
     * <p>
     * The VariableType is a pointer if it is of Numeric or MatrixAlloc CType, and it is a pointer.
     * 
     * TODO: Check if the comment above is still valid
     * 
     * @param variableType
     * @return
     */
    public static boolean isPointer(VariableType type) {
	return type.pointer().isByReference();
    }

    /**
     * Check if the right-hand needs dereferencing, and changes the code appropriately.
     * 
     * @param rightHandCode
     * @param lhType
     * @param rhType
     * @return
     */
    /*
    public static String parseRightHand(String rightHandCode, VariableType lhType, VariableType rhType) {

    if (!isPointer(lhType) && isPointer(rhType)) {
        return "*(" + rightHandCode + ")";
    }

    return rightHandCode;
    }
    */
}
