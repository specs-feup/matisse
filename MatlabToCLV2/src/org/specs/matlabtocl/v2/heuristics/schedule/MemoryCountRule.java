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

package org.specs.matlabtocl.v2.heuristics.schedule;

import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class MemoryCountRule extends BasicPortionCounter<MemoryCountRule.Counter> {

    static class Counter {
        double memory;
    }

    @Override
    public double apply(SchedulePredictorContext context) {
        Counter counter = new Counter();
        countBody(context, counter);
        return counter.memory;
    }

    @Override
    protected boolean countInstruction(SchedulePredictorContext context,
            InstructionLocation instructionLocation,
            SsaInstruction instruction,
            double factor,
            Counter counter) {

        if (instruction instanceof IndexedInstruction) {
            counter.memory += factor;
            return true;
        }

        return false;
    }

}