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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;

public class ScheduleMethod {
    public static final ScheduleMethod DIRECT = new ScheduleMethod(ScheduleStrategy.DIRECT,
            Collections.emptyList());

    private ScheduleStrategy schedule;
    private List<Integer> parameters;

    public ScheduleMethod(ScheduleStrategy schedule, List<Integer> parameters) {
        this.schedule = schedule;
        this.parameters = new ArrayList<>(parameters);
    }

    public ScheduleStrategy getSchedule() {
        return schedule;
    }

    public List<Integer> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    @Override
    public String toString() {
        return schedule.toString() + parameters;
    }
}
