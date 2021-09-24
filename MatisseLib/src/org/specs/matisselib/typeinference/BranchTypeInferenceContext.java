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

package org.specs.matisselib.typeinference;

public class BranchTypeInferenceContext extends ChildTypeInferenceContext {

    private final String condition;
    private final boolean conditionValue;

    private boolean interrupted;
    private boolean foundEnd;
    private int end;

    public BranchTypeInferenceContext(TypeInferenceContext parentContext, String condition, boolean conditionValue) {
        super(parentContext);
        this.condition = condition;
        this.conditionValue = conditionValue;

        this.interrupted = false;
    }

    @Override
    public boolean isKnownAllTrue(String condition) {
        if (condition.equals(this.condition)) {
            return this.conditionValue;
        }

        return super.isKnownAllTrue(condition);
    }

    @Override
    public boolean isKnownAllFalse(String condition) {
        if (condition.equals(this.condition)) {
            return !this.conditionValue;
        }

        return super.isKnownAllFalse(condition);
    }

    @Override
    public boolean isInterrupted() {
        return this.interrupted;
    }

    @Override
    public void markUnreachable() {
        this.interrupted = true;
    }

    @Override
    public void reachEndOfBlock(int blockId) {
        if (!this.foundEnd) {
            this.foundEnd = true;
            this.end = blockId;
        }
    }

    public int getEnd() {
        return this.end;
    }
}
