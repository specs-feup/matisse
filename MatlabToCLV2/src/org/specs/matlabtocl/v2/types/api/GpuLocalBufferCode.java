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

package org.specs.matlabtocl.v2.types.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.ACode;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;

public class GpuLocalBufferCode extends ACode {

    public GpuLocalBufferCode(VariableType type) {
	super(type);
    }

    @Override
    public String getSimpleType() {
	return "size_t";
    }

    @Override
    public Set<String> getIncludes() {
	return new HashSet<>(Arrays.asList(CLCodeGenUtils.HEADER_NAME));
    }
}
