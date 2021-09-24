/**
 * Copyright 2016 SPeCS.
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

package org.specs.matlabtocl.v2.codegen.reductionstrategies;

import java.util.List;
import java.util.Map;

import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.MatrixCopyToGpuStrategy;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMEliminationMode;

/**
 * 
 * @author Luís Reis
 * @see CommonCodeGenerationStrategyProvider
 * @see CLServices#CODE_GENERATION_STRATEGY_PROVIDER
 */
public interface CodeGenerationStrategyProvider {
    Map<ReductionType, List<ReductionStrategy>> getLocalReductionStrategies();

    Map<ReductionType, List<ReductionStrategy>> getSubgroupReductionStrategies();

    Map<ReductionType, List<ReductionStrategy>> getReductionStrategies();

    MatrixCopyToGpuStrategy getMatrixCopyStrategy();

    ScheduleStrategy getCoarseScheduleStrategy();

    ScheduleStrategy getFixedWorkGroupsScheduleStrategy();

    DeviceMemoryManagementStrategy getDeviceMemoryManagementStrategy();

    int getMaxWorkItemDimensions();

    int getSubGroupSize();

    boolean isRangeSetInstructionEnabled();

    boolean isSvmRestrictedToSequentialAccesses();

    boolean isSvmRestrictedToCoalescedAccesses();

    boolean isSvmSetRangeForbidden();

    GpuSVMEliminationMode getSVMEliminationMode();

    String getProgramFileName();

    boolean loadProgramFromSource();

    ScheduleDecisionTree getScheduleDecisionTree();

    boolean getTryUseScheduleCooperative();

    boolean getPreferSubGroupCooperativeSchedule();

    boolean getNvidiaSubgroupAsWarpFallback();
}
