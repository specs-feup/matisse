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

package org.specs.matisselib.ssa;

import com.google.common.base.Preconditions;

public final class InstructionLocation {
    private final int blockId;
    private final int instructionId;

    public InstructionLocation(int blockId, int instructionId) {
        Preconditions.checkArgument(blockId >= 0);
        Preconditions.checkArgument(instructionId >= 0);

        this.blockId = blockId;
        this.instructionId = instructionId;
    }

    public int getBlockId() {
        return this.blockId;
    }

    public int getInstructionId() {
        return this.instructionId;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof InstructionLocation) {
            return equals((InstructionLocation) other);
        }

        return false;
    }

    public boolean equals(InstructionLocation other) {
        if (other == null) {
            return false;
        }

        return this.blockId == other.blockId && this.instructionId == other.instructionId;
    }

    @Override
    public int hashCode() {
        return this.blockId ^ this.instructionId;
    }

    @Override
    public String toString() {
        return "#" + blockId + "@" + instructionId;
    }
}
