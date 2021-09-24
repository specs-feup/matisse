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

import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateTrueInstruction;

public class ValidateTrueProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ValidateTrueInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ValidateTrueInstruction validateBoolean = (ValidateTrueInstruction) instruction;

        String inputVariable = validateBoolean.getInputVariable();
        VariableType type = builder.getInstance().getVariableType(inputVariable).get();

        if (!(type instanceof ScalarType)) {
            throw new RuntimeException();
        }

        CNode expected = builder.generateVariableExpressionForSsaName(currentBlock, inputVariable);
        CNode condition = COperator.LogicalNegation
                .getCheckedInstance(builder.getCurrentProvider().createFromNodes(expected))
                .newFunctionCall(expected);

        NumericFactory numerics = builder.getCurrentProvider().getNumerics();

        List<CNode> abortInstructions = new ArrayList<>();
        abortInstructions.add(CNodeFactory.newLiteral("printf(\"Check fail: " + condition.getCode() + "\");"));
        CNode abortNode = new StdlibFunctions(numerics)
                .abort()
                .newCInstance(builder.getCurrentProvider().createFromNodes())
                .newFunctionCall();
        abortInstructions.add(abortNode);
        currentBlock.addIf(condition, abortInstructions);

        InlinedInstance inst = new InlinedInstance(FunctionTypeBuilder.newInline().returningVoid().build(), "printf",
                tokens -> "");
        inst.setCustomCallIncludes("stdio.h");
        builder.addDependency(inst);
    }
}
