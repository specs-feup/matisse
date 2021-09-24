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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matlabtocl.v2.codegen.CLCodeGenUtils;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionComputation;
import org.specs.matlabtocl.v2.codegen.SsaToOpenCLBuilderService;
import org.specs.matlabtocl.v2.functions.builtins.CLBuiltinReductionFunction;
import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Performs a set of per-workgroup partial reduction computations, the results of which are copied to the host, where
 * the computation is completed.
 * 
 * @author Luís Reis
 *
 */
public class WorkgroupSumReductionStrategy extends ReductionStrategy {
    @Override
    public CInstructionList buildReductionOperation(ReductionComputation reductionComputation,
            SsaToOpenCLBuilderService builder) {

        CInstructionList body = new CInstructionList();

        CNode isMasterThread = CLCodeGenUtils.generateIsMasterThreadNode(builder);

        ProviderData providerData = builder.getCurrentProvider();

        CNode globalBuffer = reductionComputation.getGlobalBuffer();
        CNode partialValue = builder.getVariableManager()
                .generateVariableNodeForSsaName(reductionComputation.getInnerLoopEndName());

        List<CNode> reduceArgs = Arrays.asList(partialValue);
        ProviderData reduceData = providerData.createFromNodes(reduceArgs);
        CNode accumulatorNode = CLBuiltinReductionFunction.WORK_GROUP_REDUCE_ADD
                .getCheckedInstance(reduceData)
                .newFunctionCall(reduceArgs);

        Variable resultVar = builder.generateTemporary("workgroup_sum", accumulatorNode.getVariableType());
        CNode resultNode = CNodeFactory.newVariable(resultVar);

        body.addAssignment(resultNode, accumulatorNode);

        assert builder.getGroupIdNodes().size() == 1;
        // FIXME
        List<CNode> setArgs = Arrays.asList(globalBuffer, builder.getGroupIdNodes().get(0), resultNode);
        ProviderData setData = providerData.createFromNodes(setArgs);
        CNode globalSet = ((MatrixType) globalBuffer.getVariableType())
                .matrix()
                .functions()
                .set()
                .getCheckedInstance(setData)
                .newFunctionCall(setArgs);

        body.addIf(isMasterThread, globalSet);

        return body;
    }

    @Override
    public Optional<ReductionOutput> prepareReduction(Reduction reduction,
            String initialSsaName,
            SsaToOpenCLBuilderService builder,
            VariableType variableType) {

        ScalarType elementType = (ScalarType) variableType;

        String initialFinalName = builder.getFinalName(initialSsaName);
        String finalSsaName = SpecsCollections.last(reduction.getLoopVariables()).loopEnd;

        List<Variable> kernelExtraArguments = new ArrayList<>();
        List<KernelArgument> kernelCallerArguments = new ArrayList<>();

        String globalName = initialFinalName + "_valuesg";
        String accumulatorName = initialFinalName + "_sum";

        RawBufferMatrixType globalBufferType = new RawBufferMatrixType(
                AddressSpace.GLOBAL, elementType);
        Variable globalBufferVar = new Variable(globalName, globalBufferType);
        kernelExtraArguments.add(globalBufferVar);
        kernelCallerArguments.add(KernelArgument.importGlobalPerWorkGroupBuffer(globalName,
                initialSsaName,
                reduction.getFinalName(),
                elementType));

        ReductionComputation reductionComputation = new ReductionComputation(
                null,
                CNodeFactory.newVariable(globalBufferVar),
                initialSsaName,
                finalSsaName,
                CNodeFactory.newCNumber(0, elementType),
                CNodeFactory.newVariable(accumulatorName, elementType),
                this);

        return Optional.of(new ReductionOutput(true,
                kernelExtraArguments,
                kernelCallerArguments,
                reductionComputation,
                CLVersion.V2_0,
                null,
                null));
    }
}
