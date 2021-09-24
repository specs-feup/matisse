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

package org.specs.CIRTypes.Types.Pointer;

import java.util.List;

import org.specs.CIR.Types.Views.Code.ACode;

public class PointerCode extends ACode {

    private final PointerType pointer;

    public PointerCode(PointerType pointer) {
	super(pointer);
	this.pointer = pointer;
    }

    @Override
    public String getSimpleType() {
	return (pointer.isConst() ? "const " : "") + pointer.getBaseType().code().getSimpleType() + "*";
    }

    @Override
    public String getDeclarationWithInputs(String variableName, List<String> values) {

	StringBuilder builder = new StringBuilder();

	// Append name
	builder.append(super.getDeclarationWithInputs(variableName, values));

	// Append NULL initialization if has memory allocation
	if (pointer.getBaseType().usesDynamicAllocation()) {
	    builder.append(" = NULL");
	}

	return builder.toString();
    }

}
