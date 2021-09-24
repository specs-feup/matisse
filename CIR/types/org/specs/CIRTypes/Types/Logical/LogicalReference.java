/**
 * Copyright 2017 SPeCS.
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

package org.specs.CIRTypes.Types.Logical;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Pointer.Reference;

public class LogicalReference implements Reference {

    private final LogicalType logical;

    LogicalReference(LogicalType logical) {
        this.logical = logical;
    }

    @Override
    public boolean isByReference() {
        return logical.isByReference();
    }

    @Override
    public boolean supportsReference() {
        return true;
    }

    @Override
    public VariableType getType(boolean isByReference) {
        if (logical.isByReference() == isByReference) {
            return logical;
        }
        return logical.byReference(isByReference);
    }

}
