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
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionComputation;
import org.specs.matlabtocl.v2.codegen.SsaToOpenCLBuilderService;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixSetReductionStrategy extends ReductionStrategy {

    @Override
    public CInstructionList buildReductionOperation(ReductionComputation reductionComputation,
            SsaToOpenCLBuilderService builder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ReductionOutput> prepareReduction(Reduction reduction,
            String usedInitialName,
            SsaToOpenCLBuilderService builder,
            VariableType elementType) {

        String reductionInitialName = reduction.getInitialName();
        String initialFinalName = builder.getFinalName(usedInitialName);
        CInstructionList argumentConstructionCode = new CInstructionList();

        VariableType variableType = builder.getVariableManager().getVariableTypeFromSsaName(usedInitialName).get();

        List<Variable> kernelExtraArguments = new ArrayList<>();
        List<KernelArgument> kernelCallerArguments = new ArrayList<>();

        if (variableType instanceof RawBufferMatrixType) {
            String finalName = initialFinalName;

            kernelExtraArguments.add(new Variable(finalName, variableType));
            kernelCallerArguments
                    .add(KernelArgument.importReductionData(finalName, reductionInitialName, reduction
                            .getFinalName()));

        } else if (variableType instanceof SizedMatrixType) {
            SizedMatrixType sizedMatrix = (SizedMatrixType) variableType;
            String finalName = initialFinalName;

            String dataVariableName = finalName + "_data";
            kernelExtraArguments.add(new Variable(dataVariableName, sizedMatrix.getUnderlyingRawMatrixType()));
            kernelCallerArguments
                    .add(KernelArgument.importReductionData(finalName, reductionInitialName,
                            reduction.getFinalName()));

            SsaToOpenCLBuilderService.buildSizedMatrixExtraParameters(kernelExtraArguments,
                    kernelCallerArguments,
                    argumentConstructionCode,
                    reduction.getInitialName(),
                    finalName,
                    dataVariableName,
                    sizedMatrix);
        } else {
            throw new NotImplementedException("Reduction type: " + reduction.getReductionType());
        }

        return Optional.of(new ReductionOutput(false,
                kernelExtraArguments,
                kernelCallerArguments,
                null,
                CLVersion.V1_0,
                null,
                argumentConstructionCode));
    }

}
