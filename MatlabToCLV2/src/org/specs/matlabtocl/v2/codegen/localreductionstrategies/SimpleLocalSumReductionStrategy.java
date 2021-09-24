package org.specs.matlabtocl.v2.codegen.localreductionstrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
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
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionComputation;
import org.specs.matlabtocl.v2.codegen.SsaToOpenCLBuilderService;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionOutput;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionStrategy;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;

import pt.up.fe.specs.util.SpecsCollections;

public class SimpleLocalSumReductionStrategy extends ReductionStrategy {
    @Override
    public CInstructionList buildReductionOperation(ReductionComputation reductionComputation,
            SsaToOpenCLBuilderService builder) {

        CInstructionList body = new CInstructionList();

        CNode localBuffer = reductionComputation.getLocalBuffer();

        ProviderData providerData = builder.getCurrentProvider();

        CNode partialValue = builder.getVariableManager()
                .generateVariableNodeForSsaName(reductionComputation.getInnerLoopEndName());

        assert builder.getLocalIdNodes().size() == 1;
        InstanceProvider localSetProvider = ((MatrixType) localBuffer.getVariableType())
                .functions()
                .set();
        CNode localSet = FunctionInstanceUtils.getFunctionCall(localSetProvider,
                providerData,
                localBuffer,
                builder.getLocalIdNodes().get(0), partialValue);

        body.addInstruction(localSet);
        body.addLiteralInstruction("barrier(CLK_LOCAL_MEM_FENCE);");

        CInstructionList block = new CInstructionList();
        Variable tmp = builder.generateTemporary("i", CLNativeType.UINT);
        AssignmentNode assignment = CNodeFactory.newAssignment(tmp, CNodeFactory.newCNumber(1));
        CNode stopExpr = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN,
                providerData,
                CNodeFactory.newVariable(tmp),
                builder.getLocalSizeNodes().get(0));
        CNode incrExpr = CNodeFactory.newAssignment(tmp,
                FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                        providerData,
                        CNodeFactory.newVariable(tmp),
                        CNodeFactory.newCNumber(1)));
        block.addInstruction(
                new ForNodes(builder.getCurrentProvider()).newForInstruction(assignment, stopExpr, incrExpr));

        InstanceProvider localGetProvider = ((MatrixType) localBuffer.getVariableType())
                .functions()
                .get();
        CNode localGet = FunctionInstanceUtils.getFunctionCall(localGetProvider,
                providerData,
                localBuffer,
                CNodeFactory.newVariable(tmp));

        block.addAssignment(partialValue,
                FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                        providerData,
                        partialValue,
                        localGet));

        CNode isLeaderThread = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.EQUAL,
                builder.getCurrentProvider(),
                builder.getLocalIdNodes().get(0),
                CNodeFactory.newCNumber(0));
        body.addIf(isLeaderThread, CNodeFactory.newBlock(block.get()));

        return body;
    }

    @Override
    public Optional<ReductionOutput> prepareReduction(Reduction reduction, String initialSsaName,
            SsaToOpenCLBuilderService builder,
            VariableType variableType) {

        ScalarType elementType = (ScalarType) variableType;

        String initialFinalName = builder.getFinalName(initialSsaName);
        String finalSsaName = SpecsCollections.last(reduction.getLoopVariables()).loopEnd;

        List<Variable> kernelExtraArguments = new ArrayList<>();
        List<KernelArgument> kernelCallerArguments = new ArrayList<>();

        RawBufferMatrixType localBufferType = new RawBufferMatrixType(
                AddressSpace.LOCAL, elementType);
        Variable localBufferVar = builder.generateTemporary(initialFinalName + "_partials", localBufferType);

        kernelExtraArguments.add(localBufferVar);
        kernelCallerArguments.add(KernelArgument.importLocalReductionBuffer(localBufferVar.getName(),
                reduction.getFinalName(),
                elementType));

        ReductionComputation reductionComputation = new ReductionComputation(
                CNodeFactory.newVariable(localBufferVar),
                null,
                initialSsaName,
                finalSsaName,
                CNodeFactory.newCNumber(0, elementType),
                null,
                this);

        CInstructionList preparationCode = new CInstructionList();
        CNode condition = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.NOT_EQUAL_TO,
                builder.getCurrentProvider(),
                builder.getLocalIdNodes().get(0),
                CNodeFactory.newCNumber(0));
        CNode initialNode = builder.getVariableManager().generateVariableExpressionForSsaName(preparationCode,
                initialSsaName);
        CNode setZero = CNodeFactory.newAssignment(
                initialNode,
                CNodeFactory.newCNumber(0));
        preparationCode.addIf(condition, setZero);

        return Optional.of(new ReductionOutput(false,
                kernelExtraArguments,
                kernelCallerArguments,
                reductionComputation,
                CLVersion.V1_0,
                preparationCode,
                null));
    }
}
