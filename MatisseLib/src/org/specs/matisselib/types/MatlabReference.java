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

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Pointer.Reference;

public final class MatlabReference implements Reference {
    private final MatlabElementType type;

    MatlabReference(MatlabElementType type) {
	this.type = type;
    }

    @Override
    public boolean supportsReference() {
	return true;
    }

    @Override
    public boolean isByReference() {
	return this.type.isReference();
    }

    @Override
    public VariableType getType(boolean isByReference) {
	return this.type.setReference(isByReference);
    }
}
