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
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
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

public class InterleavedLocalSumReductionStrategy extends ReductionStrategy {
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

        CInstructionList block = new CInstructionList();
        Variable tmp = builder.generateTemporary("i", CLNativeType.UINT);
        AssignmentNode assignment = CNodeFactory.newAssignment(tmp, CNodeFactory.newCNumber(0));
        CNode localId = builder.getLocalIdNodes().get(0);
        CNode localSize = builder.getLocalSizeNodes().get(0);
        VariableType type = localSize.getVariableType();
        Number localSizeConstant = ScalarUtils.getConstant(type);
        int localSizeLog2 = getLog2(localSizeConstant.intValue());
        CNode localSizeLog2Node = CNodeFactory.newCNumber(localSizeLog2);

        CNode stopExpr = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LESS_THAN,
                providerData,
                CNodeFactory.newVariable(tmp),
                localSizeLog2Node);
        CNode incrExpr = CNodeFactory.newAssignment(tmp,
                FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                        providerData,
                        CNodeFactory.newVariable(tmp),
                        CNodeFactory.newCNumber(1)));
        block.addInstruction(
                new ForNodes(builder.getCurrentProvider()).newForInstruction(assignment, stopExpr, incrExpr));

        block.addLiteralInstruction("barrier(CLK_LOCAL_MEM_FENCE);");

        Variable skip = builder.generateTemporary("skip", CLNativeType.UINT);
        block.addAssignment(skip,
                FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.LEFT_SHIFT,
                        providerData,
                        CNodeFactory.newCNumber(1),
                        CNodeFactory.newVariable(tmp)));

        CNode skip2 = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.MULTIPLICATION,
                providerData,
                CNodeFactory.newVariable(skip),
                CNodeFactory.newCNumber(2));
        CNode mask = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.SUBTRACTION,
                providerData,
                skip2,
                CNodeFactory.newCNumber(1));
        CNode maskedValue = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.BITWISE_AND,
                providerData, localId, mask);
        CNode valid = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.EQUAL,
                providerData, maskedValue, CNodeFactory.newCNumber(0));

        InstanceProvider localGetProvider = ((MatrixType) localBuffer.getVariableType())
                .functions()
                .get();
        CNode skipIndex = FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION, providerData,
                localId,
                CNodeFactory.newVariable(skip));
        CNode localGet = FunctionInstanceUtils.getFunctionCall(localGetProvider,
                providerData,
                localBuffer,
                skipIndex);

        CInstructionList ifBlock = new CInstructionList();

        ifBlock.addAssignment(partialValue,
                FunctionInstanceUtils.getFunctionCall(CLBinaryOperator.ADDITION,
                        providerData,
                        partialValue,
                        localGet));
        ifBlock.addInstruction(FunctionInstanceUtils.getFunctionCall(localSetProvider,
                providerData,
                localBuffer,
                localId,
                partialValue));

        block.addIf(valid, ifBlock.get());

        body.addInstruction(CNodeFactory.newBlock(block.get()));

        return body;
    }

    @Override
    public Optional<ReductionOutput> prepareReduction(Reduction reduction, String initialSsaName,
            SsaToOpenCLBuilderService builder,
            VariableType variableType) {

        CNode localSize = builder.getLocalSizeNodes().get(0);
        VariableType type = localSize.getVariableType();
        Number localSizeConstant = ScalarUtils.getConstant(type);
        if (localSizeConstant == null || !isPowerOfTwo(localSizeConstant.intValue())) {
            return Optional.empty();
        }

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
        CNode setZero = CNodeFactory.newAssignment(
                builder.getVariableManager().generateVariableExpressionForSsaName(preparationCode, initialSsaName),
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

    private static boolean isPowerOfTwo(int value) {
        for (;;) {
            if (value == 0) {
                return false;
            }
            if (value == 1) {
                return true;
            }
            if (value % 2 != 0) {
                return false;
            }

            value /= 2;
        }
    }

    private static int getLog2(int value) {
        int exponent = 0;
        for (;;) {
            assert value != 0;
            if (value == 1) {
                return exponent;
            }

            assert value % 2 == 0;

            ++exponent;
            value /= 2;
        }
    }
}
