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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateBooleanInstruction;

public class ValidateBooleanProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ValidateBooleanInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ValidateBooleanInstruction validateBoolean = (ValidateBooleanInstruction) instruction;

        String inputVariable = validateBoolean.getInputVariable();
        VariableType type = builder.getInstance().getVariableType(inputVariable).get();

        if (type instanceof ScalarType) {
            // Do nothing
        } else {
            assert type instanceof MatrixType;

            MatrixType matrixType = (MatrixType) type;

            FunctionInstance numelInstance = matrixType.matrix().functions()
                    .numel()
                    .getCheckedInstance(builder.getCurrentProvider().create(type));

            CNode numel = numelInstance.newFunctionCall(builder.generateVariableExpressionForSsaName(currentBlock,
                    inputVariable));

            NumericFactory numerics = builder.getCurrentProvider().getNumerics();
            VariableType oneType = numerics.newInt(1);

            CNode numelNotOne = COperator.NotEqual
                    .getCheckedInstance(builder.getCurrentProvider().create(numel.getVariableType(), oneType))
                    .newFunctionCall(numel, CNodeFactory.newCNumber(1));

            List<CNode> abortInstructions = new ArrayList<>();
            abortInstructions
                    .add(CNodeFactory.newComment("printf(\"validation failed: %d\", (int) " + numel.getCode() + ");"));
            CNode abortNode = new StdlibFunctions(numerics)
                    .abort()
                    .newCInstance(builder.getCurrentProvider().create())
                    .newFunctionCall();
            abortInstructions.add(abortNode);

            currentBlock.addIf(numelNotOne, abortInstructions);

            InlinedInstance inst = new InlinedInstance(FunctionTypeBuilder.newInline().returningVoid().build(),
                    "printf",
                    tokens -> "");
            inst.setCustomCallIncludes("stdio.h");
            builder.addDependency(inst);
        }
    }
}
