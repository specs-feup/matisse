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

package org.specs.matisselib.helpers;

import java.util.Optional;

import com.google.common.base.Objects;

public final class LoopVariable {
    public final String beforeLoop;
    public final String loopStart;
    public final String loopEnd;
    private final String afterLoop;

    public LoopVariable(String beforeLoop, String loopStart, String loopEnd, String afterLoop) {
        this.beforeLoop = beforeLoop;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
        this.afterLoop = afterLoop;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(LoopVariable.class)
                .add("beforeLoop", beforeLoop)
                .add("loopStart", loopStart)
                .add("loopEnd", loopEnd)
                .add("afterLoop", afterLoop)
                .toString();
    }

    public Optional<String> getAfterLoop() {
        return Optional.ofNullable(afterLoop);
    }
}
