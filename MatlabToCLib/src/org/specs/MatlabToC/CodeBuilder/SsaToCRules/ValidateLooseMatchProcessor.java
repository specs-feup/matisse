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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateLooseMatchInstruction;

public class ValidateLooseMatchProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ValidateLooseMatchInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ValidateLooseMatchInstruction validate = (ValidateLooseMatchInstruction) instruction;

        ProviderData providerData = builder.getCurrentProvider();
        NumericFactory numerics = providerData.getNumerics();
        ScalarType intType = numerics.newInt();

        CNode leftMatrix = builder.generateVariableNodeForSsaName(validate.getMatrix1());
        CNode rightMatrix = builder.generateVariableNodeForSsaName(validate.getMatrix2());

        MatrixType leftMatrixType = (MatrixType) leftMatrix.getVariableType();
        MatrixType rightMatrixType = (MatrixType) rightMatrix.getVariableType();

        CNode leftNumel = leftMatrixType
                .functions()
                .numel()
                .getCheckedInstance(providerData.createFromNodes(leftMatrix))
                .newFunctionCall(leftMatrix);
        CNode rightNumel = rightMatrixType
                .functions()
                .numel()
                .getCheckedInstance(providerData.createFromNodes(rightMatrix))
                .newFunctionCall(rightMatrix);

        CInstructionList notScalarInstructions = new CInstructionList();

        Variable leftIndex = builder.generateTemporary("left_index", intType);
        Variable rightIndex = builder.generateTemporary("right_index", intType);

        notScalarInstructions.addAssignment(leftIndex, CNodeFactory.newCNumber(0));
        notScalarInstructions.addAssignment(rightIndex, CNodeFactory.newCNumber(0));

        List<CNode> notLeftArguments = Arrays.asList(CNodeFactory.newVariable(leftIndex), leftNumel);
        CNode notLeftEnd = COperator.LessThan
                .getCheckedInstance(providerData.createFromNodes(notLeftArguments))
                .newFunctionCall(notLeftArguments);

        CInstructionList firstWhileInstructions = new CInstructionList();

        Variable leftValue = buildGetNextNonOne(builder, providerData,
                leftIndex, leftMatrix, leftMatrixType, "left_value",
                firstWhileInstructions);

        CNode errorNode = new StdlibFunctions(numerics)
                .abort()
                .getCheckedInstance(providerData.create())
                .newFunctionCall();

        List<CNode> bOutOfRangeArguments = Arrays.asList(CNodeFactory.newVariable(rightIndex), rightNumel);
        CNode bOutOfRange = COperator.GreaterThanOrEqual
                .getCheckedInstance(providerData.createFromNodes(bOutOfRangeArguments))
                .newFunctionCall(bOutOfRangeArguments);
        CNode bOutOfRangePrint = CNodeFactory.newLiteral("printf(\"" + bOutOfRange.getCode() + "\");");
        firstWhileInstructions.addIf(bOutOfRange, bOutOfRangePrint, errorNode);

        CInstructionList nestedWhileInstructions = new CInstructionList();
        Variable rightValue = buildGetNextNonOne(builder, providerData, rightIndex, rightMatrix, rightMatrixType,
                "right_value", nestedWhileInstructions);
        // Add assignment before nested while
        firstWhileInstructions.addAssignment(rightValue, CNodeFactory.newCNumber(1));

        CNode nestedWhileCondition = FunctionInstanceUtils.getFunctionCall(COperator.LessThan, providerData,
                CNodeFactory.newVariable(rightIndex), rightNumel);
        firstWhileInstructions.addWhile(nestedWhileCondition, nestedWhileInstructions.get());

        List<CNode> differentValuesArgs = Arrays.asList(CNodeFactory.newVariable(leftValue),
                CNodeFactory.newVariable(rightValue));
        CNode differentValues = MatlabOp.NotEqual
                .getMatlabFunction()
                .getCheckedInstance(providerData.createFromNodes(differentValuesArgs))
                .newFunctionCall(differentValuesArgs);

        CNode differentValuesPrint = CNodeFactory.newLiteral("printf(\"" + differentValues.getCode() + "\");");
        firstWhileInstructions.addIf(differentValues, differentValuesPrint, errorNode);

        notScalarInstructions.addWhile(notLeftEnd, firstWhileInstructions.get());

        List<CNode> notRightArguments = Arrays.asList(CNodeFactory.newVariable(rightIndex), rightNumel);
        CNode notRightEnd = COperator.LessThan
                .getCheckedInstance(providerData.createFromNodes(notRightArguments))
                .newFunctionCall(notRightArguments);

        CInstructionList finalWhileInstructions = new CInstructionList();

        List<CNode> getNodes = Arrays.asList(rightMatrix, CNodeFactory.newVariable(rightIndex));
        CNode get = rightMatrixType.functions()
                .get()
                .getCheckedInstance(providerData.createFromNodes(getNodes))
                .newFunctionCall(getNodes);
        List<CNode> valueNotOneArgs = Arrays.asList(get, CNodeFactory.newCNumber(1));
        CNode valueNotOne = MatlabOp.NotEqual
                .getMatlabFunction()
                .getCheckedInstance(providerData.createFromNodes(valueNotOneArgs))
                .newFunctionCall(valueNotOneArgs);

        CNode valueNotOnePrint = CNodeFactory.newLiteral("printf(\"" + valueNotOne.getCode() + "\");");
        finalWhileInstructions.addIf(valueNotOne, valueNotOnePrint, errorNode);
        addIncrement(providerData, rightIndex, finalWhileInstructions);

        notScalarInstructions.addWhile(notRightEnd, finalWhileInstructions.get());

        notScalarInstructions.addIf(FunctionInstanceUtils.getFunctionCall(COperator.NotEqual, providerData, rightNumel,
                CNodeFactory.newCNumber(1)), notScalarInstructions.get());

        InlinedInstance inst = new InlinedInstance(FunctionTypeBuilder.newInline().returningVoid().build(), "printf",
                tokens -> "");
        inst.setCustomCallIncludes("stdio.h");
        builder.addDependency(inst);
    }

    private static Variable buildGetNextNonOne(SsaToCBuilderService builder,
            ProviderData providerData,
            Variable index, CNode matrix, MatrixType matrixType, String variableName,
            CInstructionList block) {

        Variable value = builder.generateTemporary(variableName, matrixType.matrix().getElementType());
        List<CNode> getNodes = Arrays.asList(matrix, CNodeFactory.newVariable(index));
        CNode get = matrixType.functions()
                .get()
                .getCheckedInstance(providerData.createFromNodes(getNodes))
                .newFunctionCall(getNodes);
        block.addAssignment(value, get);

        addIncrement(providerData, index, block);

        List<CNode> leftValueIsOneArgs = Arrays.asList(CNodeFactory.newVariable(value), CNodeFactory.newCNumber(1));
        CNode leftValueIsOne = MatlabOp.Equal
                .getMatlabFunction()
                .getCheckedInstance(providerData.createFromNodes(leftValueIsOneArgs))
                .newFunctionCall(leftValueIsOneArgs);
        block.addIf(leftValueIsOne, CNodeFactory.newReservedWord(ReservedWord.Continue));

        return value;
    }

    private static void addIncrement(ProviderData providerData, Variable index, CInstructionList block) {
        List<CNode> incrementArgs = Arrays.asList(CNodeFactory.newVariable(index), CNodeFactory.newCNumber(1));
        block.addAssignment(index,
                COperator.Addition
                        .getCheckedInstance(providerData.createFromNodes(incrementArgs))
                        .newFunctionCall(incrementArgs));
    }

}
