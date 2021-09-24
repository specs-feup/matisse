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

package org.specs.CIR.FunctionInstance;

import org.specs.CIR.Types.VariableType;

public class OutputData {
    private final VariableType type;
    private final boolean used;

    public OutputData(VariableType type) {
	this(type, true);
    }

    public OutputData(VariableType type, boolean used) {
	this.type = type;
	this.used = used;
    }

    public VariableType getVariableType() {
	return type;
    }

    public boolean isUsed() {
	return used;
    }
}
