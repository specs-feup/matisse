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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.Variable;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.ReductionComputation;

public class ReductionOutput {
    private final boolean requiresExplicitNumThreads;
    private final List<Variable> kernelExtraArguments;
    private final List<KernelArgument> kernelCallerArguments;
    private final ReductionComputation reductionComputation;
    private final CLVersion requiredVersion;
    private final CInstructionList localLoopPreparationCode;
    private final CInstructionList argumentConstructionCode;

    public ReductionOutput(boolean requiresExplicitNumThreads,
            List<Variable> kernelExtraArguments,
            List<KernelArgument> kernelCallerArguments,
            ReductionComputation reductionComputation,
            CLVersion requiredVersion,
            CInstructionList localLoopPreparationCode,
            CInstructionList argumentConstructionCode) {

        this.requiresExplicitNumThreads = requiresExplicitNumThreads;
        this.kernelExtraArguments = new ArrayList<>(kernelExtraArguments);
        this.kernelCallerArguments = new ArrayList<>(kernelCallerArguments);
        this.reductionComputation = reductionComputation;
        this.requiredVersion = requiredVersion;
        this.localLoopPreparationCode = localLoopPreparationCode;
        this.argumentConstructionCode = argumentConstructionCode;
    }

    public boolean requiresExplicitNumThreads() {
        return requiresExplicitNumThreads;
    }

    public List<Variable> getKernelExtraArguments() {
        return Collections.unmodifiableList(kernelExtraArguments);
    }

    public List<KernelArgument> getKernelCallerArguments() {
        return Collections.unmodifiableList(kernelCallerArguments);
    }

    public ReductionComputation getReductionComputation() {
        return reductionComputation;
    }

    public CLVersion getRequiredVersion() {
        return requiredVersion;
    }

    public Optional<CInstructionList> getArgumentConstructionCode() {
        return Optional.ofNullable(argumentConstructionCode);
    }

    public Optional<CInstructionList> getLocalLoopPreparationCode() {
        return Optional.ofNullable(localLoopPreparationCode);
    }
}
