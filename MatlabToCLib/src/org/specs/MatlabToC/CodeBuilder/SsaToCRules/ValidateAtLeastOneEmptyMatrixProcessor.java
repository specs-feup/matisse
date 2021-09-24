package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateAtLeastOneEmptyMatrixInstruction;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ValidateAtLeastOneEmptyMatrixProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ValidateAtLeastOneEmptyMatrixInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ValidateAtLeastOneEmptyMatrixInstruction validate = (ValidateAtLeastOneEmptyMatrixInstruction) instruction;

        CNode condition = null;
        ProviderData currentProvider = builder.getCurrentProvider();
        for (String matrix : validate.getInputVariables()) {
            CNode node = builder.generateVariableNodeForSsaName(matrix);
            VariableType type = node.getVariableType();

            if (type instanceof ScalarType) {
                // We will skip these.
                // We could technically also skip matrices of known numel > 0, but
                // it's not worth it -- that sort of thing would be better done as an optimization pass.
                continue;
            }
            if (!(type instanceof MatrixType)) {
                throw new NotImplementedException(type);
            }

            MatrixType matrixType = (MatrixType) type;
            InstanceProvider numelProvider = matrixType.functions().numel();
            CNode functionCall = FunctionInstanceUtils.getFunctionCall(numelProvider, currentProvider,
                    node);
            CNode isNotEmpty = FunctionInstanceUtils.getFunctionCall(COperator.NotEqual, currentProvider,
                    functionCall, CNodeFactory.newCNumber(0));

            if (condition == null) {
                condition = isNotEmpty;
            } else {
                condition = FunctionInstanceUtils.getFunctionCall(COperator.LogicalAnd, currentProvider,
                        condition, isNotEmpty);
            }
        }

        if (condition == null) {
            return;
        }

        List<CNode> abortInstructions = new ArrayList<>();
        NumericFactory numerics = currentProvider.getNumerics();
        abortInstructions.add(FunctionInstanceUtils.getFunctionCall(CStdioFunction.PRINTF, currentProvider,
                CNodeFactory.newString("Invalid deletion operation: At least one index matrix must be empty.",
                        numerics.newChar().getBits())));
        abortInstructions.add(FunctionInstanceUtils.getFunctionCall(
                new StdlibFunctions(numerics).abort(), currentProvider));
        currentBlock.addIf(condition, abortInstructions);
    }

}
