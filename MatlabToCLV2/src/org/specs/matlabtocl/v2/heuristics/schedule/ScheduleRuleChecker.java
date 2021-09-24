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

import org.specs.matlabtocl.v2.heuristics.decisiontree.RuleChecker;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ScheduleRuleChecker implements RuleChecker<SchedulePredictorContext> {

    private ScheduleRule getRule(String rule) {
        switch (rule) {
        case "compute_portion":
            return new ComputePortionRule();
        case "compute_count":
            return new ComputeCountRule();
        case "memory_count":
            return new MemoryCountRule();
        case "operation_count":
            return new OperationCountRule();
        case "coalescence_portion":
            return new CoalescencePortionRule(false, false);
        case "coalescence_portion_reads":
            return new CoalescencePortionRule(true, false);
        case "coalescence_portion_writes":
            return new CoalescencePortionRule(false, true);
        default:
            throw new NotImplementedException(rule);
        }
    }

    @Override
    public boolean meetsThreshold(SchedulePredictorContext context, String rule, double minimum) {
        double result = getRule(rule).apply(context);
        return result >= minimum;
    }

}
