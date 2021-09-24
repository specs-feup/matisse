/**
 * Copyright 2015 SPeCS.
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

package org.specs.matlabtocl.v2.codegen.ssatocrules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.LibraryFunctions.LibraryFunctions;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.types.api.EventType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public final class CompleteReductionProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof CompleteReductionInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        CompleteReductionInstruction completeReduction = (CompleteReductionInstruction) instruction;

        switch (completeReduction.getReductionType()) {
        case SUM:
            applySumReduction(builder, currentBlock, completeReduction);
            break;
        case MATRIX_SET:
            applyMatrixSetReduction(builder, currentBlock, completeReduction);
            break;
        default:
            throw new NotImplementedException("Reduction type: " + completeReduction.getReductionType());
        }
    }

    private static void applySumReduction(
            SsaToCBuilderService builder,
            CInstructionList currentBlock,
            CompleteReductionInstruction completeReduction) {
        String output = completeReduction.getOutput();
        CNode outputNode = builder.generateVariableNodeForSsaName(output);

        String buffer = completeReduction.getBuffer();
        VariableNode bufferNode = builder.generateVariableNodeForSsaName(buffer);
        builder.addLiteralVariableIfNotArgument(bufferNode.getVariable());

        CNode numGroups = builder.generateVariableExpressionForSsaName(currentBlock, completeReduction.getNumGroups(),
                false, true);

        InstanceProvider mallocProvider = LibraryFunctions.newMalloc(outputNode.getVariableType());
        ProviderData mallocData = builder.getCurrentProvider().createFromNodes(numGroups);
        CNode mallocNode = mallocProvider.getCheckedInstance(mallocData).newFunctionCall(numGroups);

        CNode bufferVariable = builder.generateTemporaryNode(buffer + "_cpu",
                new PointerType(completeReduction.getUnderlyingType()));

        currentBlock.addAssignment(bufferVariable, mallocNode);

        boolean profile = builder.getPassData()
                .get(CLServices.PROFILING_OPTIONS)
                .isDataTransferProfilingEnabled();
        Variable evtVariable = null;

        StringBuilder code = new StringBuilder();
        code.append("CHECK(clEnqueueReadBuffer, MATISSE_cl.command_queue, ");
        code.append(bufferNode.getCode());
        code.append(", CL_TRUE, 0, sizeof(");
        code.append(completeReduction.getUnderlyingType().code().getSimpleType());
        code.append(") * ");
        code.append(numGroups.getCodeForRightSideOf(PrecedenceLevel.Multiplication));
        code.append(", ");
        code.append(bufferVariable.getCode());
        code.append(", 0, NULL, ");
        if (profile) {
            evtVariable = builder.generateTemporary("evt", new EventType());
            builder.addLiteralVariable(evtVariable);

            code.append("&");
            code.append(evtVariable.getName());
        } else {
            code.append("NULL");
        }
        code.append(");");

        currentBlock.addLiteralInstruction(code.toString());

        if (profile) {
            assert evtVariable != null;

            currentBlock
                    .addLiteralInstruction(
                            "MATISSE_cl_register_device_to_host_data_transfer_event(" + evtVariable.getName() + ");");
        }

        currentBlock.addAssignment(outputNode,
                builder.generateVariableExpressionForSsaName(currentBlock, completeReduction.getInitialValue()));

        CNode induction = builder.generateTemporaryNode("i",
                builder.getCurrentProvider().getNumerics().newInt());

        List<CNode> additionNodes = Arrays.asList(outputNode, CNodeFactory.newLiteral(
                bufferVariable.getCodeForLeftSideOf(PrecedenceLevel.ArrayAccess) + "[" + induction.getCode() + "]",
                completeReduction.getUnderlyingType(),
                PrecedenceLevel.ArrayAccess));
        ProviderData additionData = builder.getCurrentProvider().createFromNodes(additionNodes);
        CNode addition = COperator.Addition.getCheckedInstance(additionData).newFunctionCall(additionNodes);

        CNode loopBody = CNodeFactory.newAssignment(outputNode, addition);

        List<CNode> loopBlockContents = new ArrayList<>();
        AssignmentNode assignment = CNodeFactory.newAssignment(induction, CNodeFactory.newCNumber(0));
        CNode stopExpr = CNodeFactory.newLiteral(induction.getCode() + " < " + numGroups.getCode());
        CNode incrExpr = CNodeFactory.newLiteral(induction.getCode() + "++");
        loopBlockContents
                .add(new ForNodes(builder.getCurrentProvider()).newForInstruction(assignment, stopExpr, incrExpr));
        loopBlockContents.add(loopBody);
        CNode loopBlock = CNodeFactory.newBlock(loopBlockContents);
        currentBlock.addInstruction(loopBlock);
    }

    private static void applyMatrixSetReduction(
            SsaToCBuilderService builder,
            CInstructionList currentBlock,
            CompleteReductionInstruction completeReduction) {

        String output = completeReduction.getOutput();
        VariableNode outputNode = builder.generateVariableNodeForSsaName(output);
        MatrixType outputNodeType = (MatrixType) outputNode.getVariableType();

        String initial = completeReduction.getInitialValue();
        VariableNode initialNode = builder.generateVariableNodeForSsaName(initial);

        String buffer = completeReduction.getBuffer();
        VariableNode bufferNode = builder.generateVariableNodeForSsaName(buffer);

        if (!outputNode.getVariable().equals(initialNode.getVariable())) {

            InstanceProvider allocator = MatissePrimitive.NEW_ARRAY_FROM_MATRIX.getMatlabFunction();

            List<CNode> tokens = Arrays.asList(initialNode);
            ProviderData providerData = builder.getCurrentProvider().createFromNodes(tokens);
            providerData.setOutputType(outputNode.getVariableType());

            FunctionCallNode functionCall = allocator.getCheckedInstance(providerData).newFunctionCall(tokens);
            functionCall.getFunctionInputs().setInput(1, outputNode);

            currentBlock.addInstruction(functionCall);
        }

        StringBuilder copyCode = new StringBuilder("CHECK(clEnqueueReadBuffer, MATISSE_cl.command_queue, ");
        copyCode.append(bufferNode.getCode());
        copyCode.append(", CL_TRUE, 0, sizeof(");
        copyCode.append(outputNodeType.matrix().getElementType().code().getSimpleType());
        copyCode.append(") * ");

        InstanceProvider numel = outputNodeType.functions().numel();
        List<CNode> numelArgs = Arrays.asList(outputNode);
        CNode numelNode = numel.getCheckedInstance(builder.getCurrentProvider().createFromNodes(numelArgs))
                .newFunctionCall(numelArgs);

        copyCode.append(numelNode.getCodeForRightSideOf(PrecedenceLevel.Multiplication));
        copyCode.append(", ");

        InstanceProvider data = outputNodeType.functions().data();
        List<CNode> dataArgs = Arrays.asList(outputNode);
        CNode dataNode = data.getCheckedInstance(builder.getCurrentProvider().createFromNodes(dataArgs))
                .newFunctionCall(dataArgs);

        copyCode.append(dataNode.getCode());
        copyCode.append(", 0, NULL, ");

        boolean profile = builder.getPassData()
                .get(CLServices.PROFILING_OPTIONS)
                .isDataTransferProfilingEnabled();
        Variable evtVariable = null;
        if (profile) {
            evtVariable = builder.generateTemporary("evt", new EventType());
            builder.addLiteralVariable(evtVariable);

            copyCode.append("&");
            copyCode.append(evtVariable.getName());
        } else {
            copyCode.append("NULL");
        }
        copyCode.append(");");
        currentBlock.addLiteralInstruction(copyCode.toString());

        if (profile) {
            assert evtVariable != null;

            currentBlock
                    .addLiteralInstruction(
                            "MATISSE_cl_register_device_to_host_data_transfer_event(" + evtVariable.getName() + ");");
        }
    }
}
