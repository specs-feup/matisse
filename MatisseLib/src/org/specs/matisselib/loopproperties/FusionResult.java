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

package org.specs.matisselib.loopproperties;

import java.util.Optional;

public class FusionResult {
    private final boolean success;
    private final LoopProperty property;

    private FusionResult(boolean success, LoopProperty property) {
        this.success = success;
        this.property = property;
    }

    public static FusionResult returning(LoopProperty property) {
        return new FusionResult(true, property);
    }

    public static FusionResult returnErased() {
        return new FusionResult(true, null);
    }

    public static FusionResult rejectFusion() {
        return new FusionResult(false, null);
    }

    public boolean allowFusion() {
        return success;
    }

    public Optional<LoopProperty> getFusedProperty() {
        return Optional.ofNullable(property);
    }
}
