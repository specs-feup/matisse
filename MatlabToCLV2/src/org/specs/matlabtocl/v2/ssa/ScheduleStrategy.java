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

package org.specs.matlabtocl.v2.ssa;

public enum ScheduleStrategy {
    AUTO(false, false, false, false, false),

    DIRECT(false /*N/A*/, false, true, false, false),
    COOPERATIVE(false /*N/A*/, false, true /* N/A */, true, true),
    SUBGROUP_COOPERATIVE(false /*N/A*/, false, true /* N/A */, true, true),
    COARSE_SEQUENTIAL(false, false, false, false, false),
    COARSE_GLOBAL_ROTATION(true, false, true, false, false),
    FIXED_WORK_GROUPS_SEQUENTIAL(true, true, false, false, false),
    FIXED_WORK_GROUPS_GLOBAL_ROTATION(false, false, true, false, false);

    private final boolean prefixParameter;
    private final boolean prefixLocalSize;
    private final boolean coalescenceFriendly;
    private final boolean localConstantIndex;
    private final boolean distributeLoop;

    private ScheduleStrategy(boolean prefixParameter, boolean prefixLocalSize, boolean coalescenceFriendly,
            boolean localConstantIndex, boolean distributeLoop) {
        this.prefixParameter = prefixParameter;
        this.prefixLocalSize = prefixLocalSize;
        this.coalescenceFriendly = coalescenceFriendly;
        this.localConstantIndex = localConstantIndex;
        this.distributeLoop = distributeLoop;
    }

    public boolean isPrefixParameterType() {
        return prefixParameter;
    }

    public boolean isPrefixLocalSize() {
        return prefixLocalSize;
    }

    public boolean isCoalescenceFriendly() {
        return coalescenceFriendly;
    }

    public boolean isLocalConstantIndex() {
        return localConstantIndex;
    }

    public boolean distributesLoop() {
        return distributeLoop;
    }
}
