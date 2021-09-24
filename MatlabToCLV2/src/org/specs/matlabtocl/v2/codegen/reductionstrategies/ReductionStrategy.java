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

import java.util.Optional;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.VariableType;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionComputation;
import org.specs.matlabtocl.v2.codegen.SsaToOpenCLBuilderService;

public abstract class ReductionStrategy {
    public abstract CInstructionList buildReductionOperation(ReductionComputation reductionComputation,
            SsaToOpenCLBuilderService builder);

    public abstract Optional<ReductionOutput> prepareReduction(Reduction reduction,
            String initialSsaName,
            SsaToOpenCLBuilderService builder,
            VariableType elementType);
}
