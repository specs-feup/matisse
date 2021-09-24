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

package org.specs.matlabtocl.v2;

import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;
import org.specs.matlabtocl.v2.codegen.MatrixCopyToGpuStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumLocalReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumSubgroupReductionStrategy;
import org.specs.matlabtocl.v2.heuristics.decisiontree.TerminalNode;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleMethod;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMEliminationMode;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

public interface MatisseCLKeys {
    DataKey<SumReductionStrategy> SUM_REDUCTION_STRATEGY = KeyFactory
            .object("sum_reduction_strategy", SumReductionStrategy.class)
            .setDefault(() -> SumReductionStrategy.LOCAL_MEMORY_SUM_REDUCTION);
    DataKey<SumLocalReductionStrategy> SUM_LOCAL_REDUCTION_STRATEGY = KeyFactory
            .object("sum_local_reduction_strategy", SumLocalReductionStrategy.class)
            .setDefault(() -> SumLocalReductionStrategy.SIMPLE_SUM_REDUCTION);
    DataKey<SumSubgroupReductionStrategy> SUM_SUBGROUP_REDUCTION_STRATEGY = KeyFactory
            .object("sum_subgroup_reduction_strategy", SumSubgroupReductionStrategy.class)
            .setDefault(() -> SumSubgroupReductionStrategy.SIMPLE_SUM_REDUCTION);
    DataKey<MatrixCopyToGpuStrategy> MATRIX_COPY_TO_GPU_STRATEGY = KeyFactory
            .object("matrix_copy_to_gpu_strategy", MatrixCopyToGpuStrategy.class)
            .setDefault(() -> MatrixCopyToGpuStrategy.CREATE_BUFFER_COPY_HOST_PTR);
    DataKey<ScheduleStrategy> COARSE_DISTRIBUTION_STRATEGY = KeyFactory
            .object("coarse_distribution_strategy", ScheduleStrategy.class)
            .setDefault(() -> ScheduleStrategy.COARSE_SEQUENTIAL);
    DataKey<ScheduleStrategy> FIXED_WORK_GROUPS_DISTRIBUTION_STRATEGY = KeyFactory
            .object("fixed_work_groups_distribution_strategy", ScheduleStrategy.class)
            .setDefault(() -> ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL);
    DataKey<DeviceMemoryManagementStrategy> DEVICE_MEMORY_MANAGEMENT_STRATEGY = KeyFactory
            .enumeration("device_memory_management_strategy", DeviceMemoryManagementStrategy.class);
    DataKey<Integer> SUB_GROUP_SIZE = KeyFactory.integer("sub_group_size", -1);
    DataKey<Boolean> SUBGROUP_AS_WARP_FALLBACK = KeyFactory.bool("subgroup_as_warp_fallback");

    DataKey<Integer> MAX_WORK_ITEM_DIMENSIONS = KeyFactory.integer("max_work_item_dimensions", 3);
    DataKey<Boolean> RANGE_SET_INSTRUCTION_ENABLED = KeyFactory.bool("range_set_instruction_enabled");
    DataKey<Boolean> SVM_RESTRICT_COALESCED = KeyFactory.bool("svm_restrict_coalesced");
    DataKey<Boolean> SVM_RESTRICT_SEQUENTIAL = KeyFactory.bool("svm_restrict_sequential");
    DataKey<Boolean> SVM_SET_RANGE_FORBIDDEN = KeyFactory.bool("svm_set_range_forbidden");
    DataKey<Boolean> TRY_USE_SCHEDULE_COOPERATIVE = KeyFactory.bool("try_use_schedule_cooperative");
    DataKey<Boolean> PREFER_SUBGROUP_COOPERATIVE = KeyFactory.bool("prefer_subgroup_cooperative");
    DataKey<GpuSVMEliminationMode> SVM_ELIMINATION_MODE = KeyFactory
            .enumeration("svm_elimination_mode", GpuSVMEliminationMode.class);
    DataKey<ScheduleDecisionTree> SCHEDULE_DECISION_TREE = KeyFactory
            .object("schedule_decision_tree", ScheduleDecisionTree.class)
            .setDefault(() -> new ScheduleDecisionTree(new TerminalNode<>((c, r) -> ScheduleMethod.DIRECT)));
    DataKey<String> PROGRAM_COMPILED_NAME = KeyFactory
            .string("program_compiled_name", CLCodeGenUtils.PROGRAM_SOURCE_CODE_NAME);
    DataKey<Boolean> LOAD_PROGRAM_FROM_SOURCE = KeyFactory
            .bool("program_from_source")
            .setDefault(() -> true);
}
