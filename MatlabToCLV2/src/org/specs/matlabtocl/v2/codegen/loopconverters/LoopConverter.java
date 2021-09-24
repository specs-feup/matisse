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

package org.specs.matlabtocl.v2.codegen.loopconverters;

import java.util.Optional;
import java.util.function.BiFunction;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.codegen.GeneratedCodeSegment;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.services.KernelInstanceSink;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.reporting.Reporter;

public interface LoopConverter {
    Optional<GeneratedCodeSegment> generateCode(
            TypedInstance containerInstance,
            ParallelRegionInstance parallelInstance,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings parallelSettings,
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            KernelInstanceSink kernelSink,
            DataStore passData,
            BiFunction<String, VariableType, String> makeTemporary,
            ProviderData providerData,
            Reporter reporter);
}
