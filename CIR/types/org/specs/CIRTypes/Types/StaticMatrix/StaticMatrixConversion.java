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

package org.specs.CIRTypes.Types.StaticMatrix;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixConversion;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

public class StaticMatrixConversion extends MatrixConversion {

    private final StaticMatrixType selfType;

    public StaticMatrixConversion(StaticMatrixType type) {
	super(type, null, null);

	this.selfType = type;
    }

    @Override
    public boolean isAssignable(VariableType targetType) {
	// Call super
	boolean isAssignableBase = super.isAssignable(targetType);
	if (!isAssignableBase) {
	    return false;
	}

	// If target type is not a declared matrix, return true
	StaticMatrixType targetMatrix = SpecsStrings.cast(targetType, StaticMatrixType.class);
	if (targetMatrix == null) {
	    return true;
	}

	// If both types are declared matrixes, check if they have the same number of elements
	if (!targetMatrix.getTypeShape().getNumElements().equals(selfType.getTypeShape().getNumElements())) {
	    SpecsLogs.msgInfo(" -> It is not possible assignments between declared matrices "
		    + "if they don't have the name number of elements (" + selfType.getTypeShape() + " vs "
		    + targetMatrix.getTypeShape() + ").");

	    return false;
	}

	return true;

    }

}
