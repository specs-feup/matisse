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

import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.services.KernelInstanceSink;
import org.specs.matlabtocl.v2.services.ParallelRegionSink;
import org.specs.matlabtocl.v2.services.ParallelRegionSource;
import org.specs.matlabtocl.v2.services.cl.TemporaryAllocatorService;
import org.specs.matlabtocl.v2.services.cl.TypeGetterService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

public class CLServices {
    public static final DataKey<ParallelRegionSink> PARALLEL_REGION_SINK = KeyFactory
            .object("parallel_region_sink", ParallelRegionSink.class);
    public static final DataKey<ParallelRegionSource> PARALLEL_REGION_SOURCE = KeyFactory
            .object("parallel_region_source", ParallelRegionSource.class);
    public static final DataKey<KernelInstanceSink> KERNEL_INSTANCE_SINK = KeyFactory.object("kernel_instance_sink",
            KernelInstanceSink.class);
    public static final DataKey<CodeGenerationStrategyProvider> CODE_GENERATION_STRATEGY_PROVIDER = KeyFactory.object(
            "reduction_strategy_provider", CodeGenerationStrategyProvider.class);
    public static final DataKey<ProfilingOptions> PROFILING_OPTIONS = KeyFactory.object(
            "code_generation_options", ProfilingOptions.class);

    public static DataKey<TemporaryAllocatorService> TEMPORARY_ALLOCATOR = KeyFactory.object("temporary-allocator",
            TemporaryAllocatorService.class);
    public static DataKey<TypeGetterService> TYPE_GETTER = KeyFactory.object("type-getter", TypeGetterService.class);
}
