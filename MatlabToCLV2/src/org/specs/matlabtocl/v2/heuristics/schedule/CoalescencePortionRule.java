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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.helpers.ParallelUtils;
import org.specs.matlabtocl.v2.heuristics.svm.CoalescedAccessPredictor;

public class CoalescencePortionRule extends BasicPortionCounter<CoalescencePortionRule.CoalescenceData> {

    private boolean considerOnlyReads;
    private boolean considerOnlyWrites;

    public CoalescencePortionRule(boolean considerOnlyReads, boolean considerOnlyWrites) {
        this.considerOnlyReads = considerOnlyReads;
        this.considerOnlyWrites = considerOnlyWrites;
    }

    static class CoalescenceData {
        Set<InstructionLocation> badAccesses;
        double coalesced;
        double uncoalesced;
    }

    @Override
    public double apply(SchedulePredictorContext context) {
        CoalescenceData data = new CoalescenceData();

        List<Integer> nonOneIndices = ParallelUtils.getNonOneIndices(context.typeGetter, context.settings.localSizes);
        int sharedLocalIndex = ParallelUtils.getSharedLocalIndex(nonOneIndices);
        List<CoalescedAccessPredictor.BadAccess> badAccesses = new CoalescedAccessPredictor().computeBadAccesses(
                context.body,
                context.typeGetter,
                context.parallelLoop,
                context.settings,
                context.localReductions,
                context.blockId,
                sharedLocalIndex,
                nonOneIndices);

        data.badAccesses = badAccesses.stream()
                .map(access -> access.location)
                .collect(Collectors.toSet());
        countBody(context, data);

        if (data.uncoalesced == 0) {
            return 1;
        }

        return data.coalesced / (data.coalesced + data.uncoalesced);
    }

    @Override
    protected boolean countInstruction(SchedulePredictorContext context,
            InstructionLocation instructionLocation,
            SsaInstruction instruction,
            double factor,
            CoalescenceData info) {

        if (instruction instanceof IndexedInstruction) {

            if (instruction instanceof SimpleSetInstruction) {
                if (considerOnlyReads) {
                    return false;
                }
            } else {
                if (considerOnlyWrites) {
                    return false;
                }
            }

            if (info.badAccesses.contains(instructionLocation)) {
                info.uncoalesced += factor;
            } else {
                info.coalesced += factor;
            }

            return true;
        }
        return false;
    }

}
