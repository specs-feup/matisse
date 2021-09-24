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

package org.specs.matisselib.ssa;

import com.google.common.base.Preconditions;

public final class VariableInput implements Input {
    private final String variableName;

    public VariableInput(String variableName) {
	Preconditions.checkArgument(variableName != null);

	this.variableName = variableName;
    }

    public String getName() {
	return variableName;
    }

    @Override
    public String toString() {
	return variableName;
    }
}
