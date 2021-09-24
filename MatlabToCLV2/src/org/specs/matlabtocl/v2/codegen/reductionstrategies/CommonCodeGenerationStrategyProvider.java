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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.matlabtocl.v2.MatisseCLKeys;
import org.specs.matlabtocl.v2.codegen.MatrixCopyToGpuStrategy;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMEliminationMode;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.persistence.XmlPersistence;

public class CommonCodeGenerationStrategyProvider implements CodeGenerationStrategyProvider {

    private final File weaverFilePath;
    private DataStore dataStore;

    public CommonCodeGenerationStrategyProvider(File weaverFilePath) {
        this.weaverFilePath = weaverFilePath;
    }

    @Override
    public Map<ReductionType, List<ReductionStrategy>> getReductionStrategies() {
        DataStore dataStore = getDataStore();

        Map<ReductionType, List<ReductionStrategy>> strategies = new HashMap<>();
        strategies.put(ReductionType.SUM,
                Arrays.asList(dataStore.get(MatisseCLKeys.SUM_REDUCTION_STRATEGY).buildInstance()));

        strategies.put(ReductionType.MATRIX_SET, Arrays.asList(new MatrixSetReductionStrategy()));

        return strategies;
    }

    @Override
    public Map<ReductionType, List<ReductionStrategy>> getLocalReductionStrategies() {
        DataStore dataStore = getDataStore();

        Map<ReductionType, List<ReductionStrategy>> strategies = new HashMap<>();
        strategies.put(ReductionType.SUM,
                Arrays.asList(dataStore.get(MatisseCLKeys.SUM_LOCAL_REDUCTION_STRATEGY).buildInstance()));

        return strategies;
    }

    @Override
    public Map<ReductionType, List<ReductionStrategy>> getSubgroupReductionStrategies() {
        DataStore dataStore = getDataStore();

        Map<ReductionType, List<ReductionStrategy>> strategies = new HashMap<>();
        strategies.put(ReductionType.SUM,
                Arrays.asList(dataStore.get(MatisseCLKeys.SUM_SUBGROUP_REDUCTION_STRATEGY).buildInstance()));

        return strategies;
    }

    @Override
    public MatrixCopyToGpuStrategy getMatrixCopyStrategy() {
        return getDataStore().get(MatisseCLKeys.MATRIX_COPY_TO_GPU_STRATEGY);
    }

    @Override
    public ScheduleStrategy getCoarseScheduleStrategy() {
        return getDataStore().get(MatisseCLKeys.COARSE_DISTRIBUTION_STRATEGY);
    }

    @Override
    public int getSubGroupSize() {
        return getDataStore().get(MatisseCLKeys.SUB_GROUP_SIZE);
    }

    public ScheduleStrategy getFixedWorkGroupsScheduleStrategy() {
        return getDataStore().get(MatisseCLKeys.FIXED_WORK_GROUPS_DISTRIBUTION_STRATEGY);
    }

    @Override
    public int getMaxWorkItemDimensions() {
        return getDataStore().get(MatisseCLKeys.MAX_WORK_ITEM_DIMENSIONS);
    }

    @Override
    public DeviceMemoryManagementStrategy getDeviceMemoryManagementStrategy() {
        return getDataStore().get(MatisseCLKeys.DEVICE_MEMORY_MANAGEMENT_STRATEGY);
    }

    @Override
    public boolean isRangeSetInstructionEnabled() {
        return getDataStore().get(MatisseCLKeys.RANGE_SET_INSTRUCTION_ENABLED);
    }

    @Override
    public boolean isSvmRestrictedToCoalescedAccesses() {
        return getDataStore().get(MatisseCLKeys.SVM_RESTRICT_COALESCED);
    }

    @Override
    public boolean isSvmRestrictedToSequentialAccesses() {
        return getDataStore().get(MatisseCLKeys.SVM_RESTRICT_SEQUENTIAL);
    }

    @Override
    public boolean isSvmSetRangeForbidden() {
        return getDataStore().get(MatisseCLKeys.SVM_SET_RANGE_FORBIDDEN);
    }

    @Override
    public GpuSVMEliminationMode getSVMEliminationMode() {
        return getDataStore().get(MatisseCLKeys.SVM_ELIMINATION_MODE);
    }

    @Override
    public ScheduleDecisionTree getScheduleDecisionTree() {
        return getDataStore().get(MatisseCLKeys.SCHEDULE_DECISION_TREE);
    }

    @Override
    public String getProgramFileName() {
        return getDataStore().get(MatisseCLKeys.PROGRAM_COMPILED_NAME);
    }

    @Override
    public boolean loadProgramFromSource() {
        return getDataStore().get(MatisseCLKeys.LOAD_PROGRAM_FROM_SOURCE);
    }

    @Override
    public boolean getTryUseScheduleCooperative() {
        return getDataStore().get(MatisseCLKeys.TRY_USE_SCHEDULE_COOPERATIVE);
    }

    @Override
    public boolean getPreferSubGroupCooperativeSchedule() {
        return getDataStore().get(MatisseCLKeys.PREFER_SUBGROUP_COOPERATIVE);
    }

    @Override
    public boolean getNvidiaSubgroupAsWarpFallback() {
        return getDataStore().get(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK);
    }

    public DataStore getDataStore() {
        if (this.dataStore == null) {
            if (weaverFilePath.isFile()) {
                this.dataStore = new XmlPersistence().loadData(weaverFilePath);
            } else {
                System.err.println("Weaver result not found.");
                this.dataStore = new SimpleDataStore("dummy-data-store");
            }
        }
        return this.dataStore;
    }
}
