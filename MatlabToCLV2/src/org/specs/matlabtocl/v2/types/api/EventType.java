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

package org.specs.matlabtocl.v2.types.api;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.Code;

public class EventType extends AVariableType {
    public EventType() {

    }

    @Override
    public EventType setWeakType(boolean isWeakType) {
        return this;
    }

    @Override
    protected EventType copyPrivate() {
        return this;
    }

    @Override
    public String getSmallId() {
        return "et";
    }

    @Override
    public Code code() {
        return new EventCode(this);
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof EventType;
    }
}
