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

package org.specs.matisselib.services;

import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;

import com.google.common.base.Preconditions;

public interface TypeInformationService {
    boolean isVariable(String variableName);

    Optional<VariableType> getVariableType(String variableName);

    void changeVariableType(String variableName, VariableType variableType);

    void createVariable(String variableName, VariableType variableType);

    default void createVariableIfNotExists(String variableName, VariableType variableType) {
	Preconditions.checkArgument(variableName != null);
	Preconditions.checkArgument(variableType != null);

	if (!isVariable(variableName)) {
	    createVariable(variableName, variableType);
	}
    }

    MatrixType getMatrixType(VariableType elementType);
}
