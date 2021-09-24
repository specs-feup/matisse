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

import java.util.function.Supplier;

import org.specs.matlabtocl.v2.codegen.localreductionstrategies.InterleavedLocalSumReductionStrategy;
import org.specs.matlabtocl.v2.codegen.localreductionstrategies.NativeLocalSumReductionStrategy;
import org.specs.matlabtocl.v2.codegen.localreductionstrategies.SimpleLocalSumReductionStrategy;

public enum SumLocalReductionStrategy {
    SIMPLE_SUM_REDUCTION(SimpleLocalSumReductionStrategy::new),
    NATIVE_SUM_REDUCTION(NativeLocalSumReductionStrategy::new),
    INTERLEAVED_SUM_REDUCTION(InterleavedLocalSumReductionStrategy::new);

    private final Supplier<? extends ReductionStrategy> factory;

    SumLocalReductionStrategy(Supplier<? extends ReductionStrategy> factory) {
        this.factory = factory;
    }

    public ReductionStrategy buildInstance() {
        return factory.get();
    }
}
