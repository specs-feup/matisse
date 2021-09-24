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

package org.specs.matisselib.tests.cpass.whileconditionbuilder;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.cir.WhileConditionBuilderPass;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class WhileConditionBuilderTests extends TestSkeleton {
    @Test
    public void testSimple() {
        CInstructionList instructions = new CInstructionList();

        VariableType intType = getNumerics().newInt();
        ProviderData dummyProvider = ProviderData.newInstance("dummy");

        CNode ifCondition = FunctionInstanceUtils.getFunctionCall(COperator.LogicalNegation, dummyProvider,
                Arrays.asList(CNodeFactory.newLiteral("x > 0", intType)));

        CNode innerIf = IfNodes.newIfThenElse(ifCondition, CNodeFactory.newReservedWord(ReservedWord.Break),
                CNodeFactory.newLiteral("A"));

        CNode whileCondition = CNodeFactory.newCNumber(1);
        CNode whileHeader = CNodeFactory.newInstruction(InstructionType.While,
                CNodeFactory.newReservedWord(ReservedWord.While),
                whileCondition);
        CNode whileBlock = CNodeFactory.newBlock(whileHeader, innerIf, CNodeFactory.newLiteral("B"));

        instructions.addInstruction(whileBlock);

        String expected = SpecsIo.getResource(WhileConditionBuilderResource.SIMPLE);

        performTest(instructions, expected);
    }

    @Test
    public void testJoined() {
        CInstructionList instructions = new CInstructionList();

        VariableType intType = getNumerics().newInt();
        ProviderData dummyProvider = ProviderData.newInstance("dummy");

        CNode ifCondition1 = FunctionInstanceUtils.getFunctionCall(COperator.LogicalNegation, dummyProvider,
                Arrays.asList(CNodeFactory.newLiteral("x > 0", intType, PrecedenceLevel.GreaterThan)));
        CNode ifCondition2 = FunctionInstanceUtils.getFunctionCall(COperator.LogicalNegation, dummyProvider,
                Arrays.asList(CNodeFactory.newLiteral("y > 0", intType, PrecedenceLevel.Equality)));

        CNode innerIf1 = IfNodes.newIfThen(ifCondition1, CNodeFactory.newReservedWord(ReservedWord.Break));
        CNode innerIf2 = IfNodes.newIfThenElse(ifCondition2, CNodeFactory.newReservedWord(ReservedWord.Break),
                CNodeFactory.newLiteral("A"));

        CNode whileCondition = CNodeFactory.newCNumber(1);
        CNode whileHeader = CNodeFactory.newInstruction(InstructionType.While,
                CNodeFactory.newReservedWord(ReservedWord.While),
                whileCondition);
        CNode whileBlock = CNodeFactory.newBlock(whileHeader, innerIf1, innerIf2, CNodeFactory.newLiteral("B"));

        instructions.addInstruction(whileBlock);

        String expected = SpecsIo.getResource(WhileConditionBuilderResource.JOINED);

        performTest(instructions, expected);
    }

    private static void performTest(CInstructionList instructions, String expected) {
        new WhileConditionBuilderPass().apply(instructions,
                ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings()));
        String obtained = instructions.get().get(0).getCode();
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
