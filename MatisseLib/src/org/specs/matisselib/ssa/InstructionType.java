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

import org.specs.matisselib.ssa.instructions.AssumeMatrixIndicesInRangeDirectiveInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;

public enum InstructionType {
    /**
     * The instruction has no side effect whatsoever.
     */
    NO_SIDE_EFFECT,
    /**
     * The instruction has the side effect of performing validation which can fail.<br/>
     * If validation is ignored, then this instruction is equivalent to {@link #NO_SIDE_EFFECT}
     */
    HAS_VALIDATION_SIDE_EFFECT,
    /**
     * The instruction has side effects, such as printing data.
     */
    HAS_SIDE_EFFECT,
    /**
     * This instruction controls the flow of the program, but other than that has no side-effects.
     */
    CONTROL_FLOW,
    /**
     * The instruction has no side effects, but should not be removed because we want it to be output anyway. This is
     * the case for comments and directives.
     * 
     * @see CommentInstruction
     * @see AssumeMatrixIndicesInRangeDirectiveInstruction
     */
    DECORATOR,
    /**
     * Similar to {@link #DECORATOR}, but specific to line information.
     */
    LINE;

    public boolean mayHaveSideEffects() {
        return this == HAS_SIDE_EFFECT || this == HAS_VALIDATION_SIDE_EFFECT;
    }
}
