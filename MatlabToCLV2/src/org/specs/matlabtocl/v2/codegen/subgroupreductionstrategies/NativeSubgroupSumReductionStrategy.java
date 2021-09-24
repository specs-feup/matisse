package org.specs.matlabtocl.v2.codegen.subgroupreductionstrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionComputation;
import org.specs.matlabtocl.v2.codegen.SsaToOpenCLBuilderService;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionOutput;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionStrategy;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.functions.builtins.CLGroupReduceFunction;

import pt.up.fe.specs.util.SpecsCollections;

public class NativeSubgroupSumReductionStrategy extends ReductionStrategy {
    @Override
    public CInstructionList buildReductionOperation(ReductionComputation reductionComputation,
            SsaToOpenCLBuilderService builder) {

        CInstructionList body = new CInstructionList();

        ProviderData providerData = builder.getCurrentProvider();

        CNode partialValue = builder.getVariableManager()
                .generateVariableNodeForSsaName(reductionComputation.getInnerLoopEndName());

        body.addAssignment(partialValue,
                FunctionInstanceUtils.getFunctionCall(CLGroupReduceFunction.SUB_GROUP_REDUCE_ADD, providerData,
                        partialValue));

        return body;
    }

    @Override
    public Optional<ReductionOutput> prepareReduction(Reduction reduction, String initialSsaName,
            SsaToOpenCLBuilderService builder,
            VariableType variableType) {

        ScalarType elementType = (ScalarType) variableType;

        String finalSsaName = SpecsCollections.last(reduction.getLoopVariables()).loopEnd;

        List<Variable> kernelExtraArguments = new ArrayList<>();
        List<KernelArgument> kernelCallerArguments = new ArrayList<>();

        ReductionComputation reductionComputation = new ReductionComputation(
                null,
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
                CLVersion.V2_0,
                preparationCode,
                null));
    }
}
