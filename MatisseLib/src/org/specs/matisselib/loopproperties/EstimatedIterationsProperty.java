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

public final class EstimatedIterationsProperty extends EqualCombinableLoopProperty {
    private final int iterationCount;

    public EstimatedIterationsProperty(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EstimatedIterationsProperty
                && ((EstimatedIterationsProperty) obj).iterationCount == iterationCount;
    }

    @Override
    public String toString() {
        return "[estimated_iterations " + iterationCount + "]";
    }
}
