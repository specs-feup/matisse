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

package org.specs.CIR.Types.Views.Pointer;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Pointer implementation for types which do not support pointers.
 * 
 * @author Joao Bispo
 * 
 */
public class DummyReference implements Reference {

    private final VariableType type;

    public DummyReference(VariableType type) {
	this.type = type;
    }

    @Override
    public boolean isByReference() {
	return false;
    }

    @Override
    public boolean supportsReference() {
	return false;
    }

    @Override
    public VariableType getType(boolean isByReference) {
	if (isByReference) {
	    SpecsLogs.warn("[CHECK] Trying to make a pointer out a type ('" + type
		    + "') that does not support pointers. Check if ok.");
	}

	return type;
    }

}
