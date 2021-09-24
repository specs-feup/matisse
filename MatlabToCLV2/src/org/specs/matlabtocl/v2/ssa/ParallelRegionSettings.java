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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.LocalMemorySumReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SimpleSumReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.WorkgroupSumReductionStrategy;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ParallelRegionSettings {
    public ScheduleStrategy schedule = ScheduleStrategy.AUTO;
    public List<String> scheduleNames = Collections.emptyList();
    public List<String> localSizes = Collections.emptyList();
    public Map<ReductionType, List<ReductionStrategy>> reductionStrategies;
    public Map<ReductionType, List<ReductionStrategy>> localReductionStrategies;
    public Map<ReductionType, List<ReductionStrategy>> subgroupReductionStrategies;
    public boolean rangeSetDisabled;

    public ParallelRegionSettings() {
    }

    public ParallelRegionSettings copy() {
        ParallelRegionSettings settings = new ParallelRegionSettings();

        settings.schedule = schedule;
        settings.scheduleNames = new ArrayList<>(scheduleNames);
        settings.localSizes = new ArrayList<>(localSizes);
        settings.rangeSetDisabled = rangeSetDisabled;
        settings.reductionStrategies = reductionStrategies;
        settings.localReductionStrategies = localReductionStrategies;
        settings.subgroupReductionStrategies = subgroupReductionStrategies;

        return settings;
    }

    public boolean isRangeSetDisabled() {
        return rangeSetDisabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        switch (schedule) {
        case AUTO:
            builder.append(" schedule(auto)");
            break;
        case DIRECT:
            builder.append(" schedule(direct)");
            break;
        case COOPERATIVE:
            builder.append(" schedule(cooperative)");
            break;
        case SUBGROUP_COOPERATIVE:
            builder.append(" schedule(subgroup_cooperative)");
            break;
        case COARSE_SEQUENTIAL:
            builder.append(" schedule(coarse_sequential");
            for (String distributionName : scheduleNames) {
                builder.append(", ");
                builder.append(distributionName);
            }
            builder.append(")");
            break;
        case COARSE_GLOBAL_ROTATION:
            builder.append(" schedule(coarse_global_rotation");
            for (String distributionName : scheduleNames) {
                builder.append(", ");
                builder.append(distributionName);
            }
            builder.append(")");
            break;
        case FIXED_WORK_GROUPS_SEQUENTIAL:
            builder.append(" schedule(fixed_work_groups_sequential");
            for (String distributionName : scheduleNames) {
                builder.append(", ");
                builder.append(distributionName);
            }
            builder.append(")");
            break;
        case FIXED_WORK_GROUPS_GLOBAL_ROTATION:
            builder.append(" schedule(fixed_work_groups_global_rotation");
            for (String distributionName : scheduleNames) {
                builder.append(", ");
                builder.append(distributionName);
            }
            builder.append(")");
            break;
        default:
            throw new NotImplementedException(schedule);
        }

        if (!localSizes.isEmpty()) {
            builder.append(" local_size(");
            builder.append(localSizes.get(0));

            for (int i = 1; i < localSizes.size(); ++i) {
                builder.append(", ");
                builder.append(localSizes.get(i));
            }
            builder.append(")");
        }

        List<ReductionStrategy> sumReductionStrategies = reductionStrategies.get(ReductionType.SUM);
        if (sumReductionStrategies.size() == 1) {
            ReductionStrategy sumReductionStrategy = sumReductionStrategies.get(0);
            builder.append(" sum_reduction_strategy(");
            Class<? extends ReductionStrategy> sumReductionClass = sumReductionStrategy.getClass();
            if (sumReductionClass == SimpleSumReductionStrategy.class) {
                builder.append("simple");
            } else if (sumReductionClass == LocalMemorySumReductionStrategy.class) {
                builder.append("local_memory");
            } else if (sumReductionClass == WorkgroupSumReductionStrategy.class) {
                builder.append("workgroup");
            } else {
                throw new NotImplementedException(sumReductionStrategy.getClass());
            }
        }
        builder.append(")");

        return builder.toString();

    }

    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.addAll(scheduleNames);
        inputs.addAll(localSizes);

        return inputs;
    }

    public List<String> getKernelInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.addAll(scheduleNames);
        inputs.addAll(localSizes);

        return inputs;
    }

    public void renameVariables(Map<String, String> newNames) {
        for (int i = 0; i < scheduleNames.size(); ++i) {
            String originalName = scheduleNames.get(i);
            scheduleNames.set(i, newNames.getOrDefault(originalName, originalName));
        }

        for (int i = 0; i < localSizes.size(); ++i) {
            String originalName = localSizes.get(i);
            localSizes.set(i, newNames.getOrDefault(originalName, originalName));
        }
    }
}
