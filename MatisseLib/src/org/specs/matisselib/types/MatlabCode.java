/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.types;

import java.util.HashSet;
import java.util.Set;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.Views.Code.ACode;

public class MatlabCode extends ACode {

    private final MatlabElementType type;

    public MatlabCode(MatlabElementType type) {
	super(type);

	this.type = type;
    }

    @Override
    public String getSimpleType() {
	return this.type.getSimpleType();
    }

    @Override
    public Set<String> getIncludes() {
	Set<String> includes = new HashSet<>(super.getIncludes());

	SystemInclude header = this.type.getIncludes();
	if (header != null) {
	    includes.add(header.getIncludeName());
	}

	return includes;
    }
}
