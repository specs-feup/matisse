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

package org.specs.matlabtocl.v2.tests;

import java.util.Arrays;
import java.util.HashMap;

import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumReductionStrategy;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;

public class CLTestUtils {
    public static ParallelRegionSettings buildDummySettings() {
        ParallelRegionSettings settings = new ParallelRegionSettings();

        settings.schedule = ScheduleStrategy.DIRECT;
        settings.reductionStrategies = new HashMap<>();
        settings.reductionStrategies.put(ReductionType.SUM,
                Arrays.asList(SumReductionStrategy.SIMPLE_SUM_REDUCTION.buildInstance()));

        return settings;
    }
}
