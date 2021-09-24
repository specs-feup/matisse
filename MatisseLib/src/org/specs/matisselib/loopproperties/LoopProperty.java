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

public abstract class LoopProperty {
    /**
     * Two loops are fused, but only one of them has this property.
     */
    public abstract FusionResult getFusionResultWithoutEquivalent();

    /**
     * Two loops are fused, and they both have this property
     */
    public abstract FusionResult getFusionResultWith(LoopProperty property);
}
