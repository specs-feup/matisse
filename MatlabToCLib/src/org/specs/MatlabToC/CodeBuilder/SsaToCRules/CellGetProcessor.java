package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.CellGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.types.CellArrayType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class CellGetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof CellGetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        CellGetInstruction get = (CellGetInstruction) instruction;

        String cell = get.getInputCell();
        List<String> indices = get.getIndices();
        String output = get.getOutput();

        CNode cellNode = builder.generateVariableExpressionForSsaName(currentBlock, cell);

        VariableType inputType = builder.getInstance().getVariableType(cell).get();
        if (!(inputType instanceof CellArrayType)) {
            throw new NotImplementedException("Cell array get for non-cell value.");
        }

        CellArrayType cellType = (CellArrayType) inputType;

        if (indices.stream()
                .allMatch(index -> builder.getInstance().getVariableType(index).get() instanceof ScalarType)) {

            InstanceProvider getProvider = cellType.cell().functions().get();

            CNode outputVariable = builder.generateVariableExpressionForSsaName(currentBlock, output, false);
            List<CNode> inputs = new ArrayList<>();
            inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, cell, false));

            CNode one = CNodeFactory.newCNumber(1);
            for (String index : indices) {
                CNode inputVariable = builder.generateVariableExpressionForSsaName(currentBlock, index, false);

                CNode inputNode = FunctionInstanceUtils.getFunctionCall(COperator.Subtraction,
                        builder.getCurrentProvider(), Arrays.asList(inputVariable, one));

                inputs.add(inputNode);
            }

            FunctionCallNode functionCall = FunctionInstanceUtils.getFunctionCall(getProvider,
                    builder.getCurrentProvider(), inputs);
            if (functionCall.getFunctionInstance().getFunctionType().hasOutputsAsInputs()) {
                functionCall.getFunctionInputs().setInput(inputs.size(), outputVariable);
                currentBlock.addInstruction(functionCall);
            } else {
                currentBlock.addAssignment(outputVariable, functionCall);
            }

        } else {
            throw new NotImplementedException("Non-scalar indices for cell array get");
        }
    }

}
