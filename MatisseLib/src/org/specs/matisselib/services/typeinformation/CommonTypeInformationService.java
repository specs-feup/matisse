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

package org.specs.matisselib.services.typeinformation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.services.TypeInformationService;

import com.google.common.base.Preconditions;

public class CommonTypeInformationService implements TypeInformationService {

    private Map<String, VariableType> variableTypes = new HashMap<>();

    @Override
    public boolean isVariable(String variableName) {
	Preconditions.checkArgument(variableName != null);

	return variableTypes.containsKey(variableName);
    }

    @Override
    public Optional<VariableType> getVariableType(String variableName) {
	Preconditions.checkArgument(variableName != null);

	return Optional.ofNullable(variableTypes.getOrDefault(variableName, null));
    }

    @Override
    public void createVariable(String variableName, VariableType variableType) {
	Preconditions.checkArgument(variableName != null);
	Preconditions.checkArgument(variableType != null);
	Preconditions.checkState(!isVariable(variableName));

	variableTypes.put(variableName, variableType);
    }

    @Override
    public void changeVariableType(String variableName, VariableType variableType) {
	Preconditions.checkArgument(variableName != null);
	Preconditions.checkArgument(variableType != null);
	Preconditions.checkState(isVariable(variableName));

	variableTypes.put(variableName, variableType);
    }

    @Override
    public MatrixType getMatrixType(VariableType elementType) {
	return DynamicMatrixType.newInstance(elementType);
    }
}
