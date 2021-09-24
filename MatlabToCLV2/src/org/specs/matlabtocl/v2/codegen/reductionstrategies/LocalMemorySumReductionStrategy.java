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
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.ForNodes;
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
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;

/**
 * Items are copied to a per-workgroup buffer. Then, the "group leader" performs a partial reduction computation and
 * stores the result in a global buffer.
 * <p>
 * The global buffer is then copied to the host and the reduction is completed there.
 * 
 * @author Luís Reis
 *
 */
public class LocalMemorySumReductionStrategy extends ReductionStrategy {
    @Override
    public CInstructionList buildReductionOperation(ReductionComputation reductionComputation,
            SsaToOpenCLBuilderService builder) {

        CInstructionList ifBody = new CInstructionList();

        CNode isMasterThread = CLCodeGenUtils.generateIsMasterThreadNode(builder);

        CNode globalBuffer = reductionComputation.getGlobalBuffer();
        CNode localBuffer = reductionComputation.getLocalBuffer();

        CNode accumulator = reductionComputation.getAccumulatorVariable();

        ProviderData providerData = builder.getCurrentProvider();

        ifBody.addAssignment(accumulator, reductionComputation.getDefaultValue());

        CNode inductionVar = CNodeFactory
                .newVariable(builder.generateTemporary("i", CLNativeType.SIZE_T));

        List<CNode> partialValueArgs = Arrays.asList(localBuffer, inductionVar);
        ProviderData partialValueData = providerData.createFromNodes(partialValueArgs);
        CNode partialValue = ((MatrixType) localBuffer.getVariableType())
                .matrix()
                .functions()
                .get()
                .getCheckedInstance(partialValueData)
                .newFunctionCall(partialValueArgs);

        List<CNode> combinerArgs = Arrays.asList(accumulator, partialValue);
        ProviderData combinerData = providerData.createFromNodes(combinerArgs);
        CNode newValue = CLBinaryOperator.ADDITION
                .getCheckedInstance(combinerData)
                .newFunctionCall(combinerArgs);
        CNode forBody = CNodeFactory.newAssignment(accumulator, newValue);

        List<CNode> forInstructions = new ArrayList<>();

        AssignmentNode initial = CNodeFactory.newAssignment(inductionVar,
                CNodeFactory.newLiteral("0", inductionVar.getVariableType()));

        assert builder.getLocalSizeNodes().size() == 1;

        // FIXME
        List<CNode> stopArgs = Arrays.asList(inductionVar, builder.getLocalSizeNodes().get(0));
        ProviderData stopData = providerData.createFromNodes(stopArgs);
        CNode stop = CLBinaryOperator.LESS_THAN.getCheckedInstance(stopData).newFunctionCall(stopArgs);

        List<CNode> incrArgs = Arrays.asList(inductionVar, CNodeFactory.newCNumber(1));
        ProviderData incrData = providerData.createFromNodes(incrArgs);
        CNode incr = CLBinaryOperator.ADDITION.getCheckedInstance(incrData).newFunctionCall(incrArgs);
        CNode incrExpr = CNodeFactory.newAssignment(inductionVar, incr);

        forInstructions.add(new ForNodes(providerData).newForInstruction(initial, stop, incrExpr));
        forInstructions.add(forBody);

        CNode forBlock = CNodeFactory.newBlock(forInstructions);
        ifBody.addInstruction(forBlock);

        // FIXME
        List<CNode> setArgs = Arrays.asList(globalBuffer, builder.getGroupIdNodes().get(0), accumulator);
        ProviderData setData = providerData.createFromNodes(setArgs);
        CNode globalSet = ((MatrixType) globalBuffer.getVariableType())
                .matrix()
                .functions()
                .set()
                .getCheckedInstance(setData)
                .newFunctionCall(setArgs);

        ifBody.addInstruction(globalSet);

        CInstructionList reduction = new CInstructionList();
        reduction.addIf(isMasterThread, ifBody.get());

        return reduction;
    }

    @Override
    public Optional<ReductionOutput> prepareReduction(Reduction reduction,
            String initialSsaName,
            SsaToOpenCLBuilderService builder,
            VariableType variableType) {

        ScalarType elementType = (ScalarType) variableType;

        String initialFinalName = builder.getFinalName(initialSsaName);
        String finalSsaName = reduction.getLoopVariables().get(0).loopEnd;

        List<Variable> kernelExtraArguments = new ArrayList<>();
        List<KernelArgument> kernelCallerArguments = new ArrayList<>();

        // FIXME
        String localName = initialFinalName + "_valuesl";
        String globalName = initialFinalName + "_valuesg";
        String accumulatorName = initialFinalName + "_sum";

        RawBufferMatrixType localBufferType = new RawBufferMatrixType(
                AddressSpace.LOCAL, elementType);
        Variable localBufferVar = new Variable(localName, localBufferType);
        kernelExtraArguments.add(localBufferVar);
        kernelCallerArguments.add(KernelArgument.importLocalReductionBuffer(localName,
                initialSsaName,
                elementType));

        RawBufferMatrixType globalBufferType = new RawBufferMatrixType(
                AddressSpace.GLOBAL, elementType);
        Variable globalBufferVar = new Variable(globalName, globalBufferType);
        kernelExtraArguments.add(globalBufferVar);
        kernelCallerArguments.add(KernelArgument.importGlobalPerWorkGroupBuffer(globalName,
                initialSsaName,
                reduction.getFinalName(),
                elementType));

        ReductionComputation reductionComputation = new ReductionComputation(
                CNodeFactory.newVariable(localBufferVar),
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
                CLVersion.V1_0,
                null,
                null));
    }
}
