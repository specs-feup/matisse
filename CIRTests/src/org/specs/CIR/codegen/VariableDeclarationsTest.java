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

package org.specs.CIR.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

import pt.up.fe.specs.util.SpecsIo;

public class VariableDeclarationsTest {

    @Test
    public void testSimple() {
        NumericFactory numerics = NumericFactory.defaultFactory();
        VariableType intType = numerics.newInt();

        CInstructionList body = new CInstructionList(FunctionTypeBuilder.newSimple().returningVoid().build());

        body.addAssignment(CNodeFactory.newVariable("foo", intType), CNodeFactory.newCNumber(1));

        InstructionsInstance inst = new InstructionsInstance("test", "test.c", body);

        Assert.assertEquals(SpecsIo.getResource(VariableDeclarationsResource.SIMPLE).replace("\r\n", "\n"),
                inst.getImplementationCode());
    }

    @Test
    public void testIfInIf() {
        NumericFactory numerics = NumericFactory.defaultFactory();
        VariableType intType = numerics.newInt();

        CInstructionList body = new CInstructionList(FunctionTypeBuilder.newSimple().returningVoid().build());

        CNode assignment = CNodeFactory.newAssignment(CNodeFactory.newVariable("foo", intType),
                CNodeFactory.newCNumber(1));
        body.addIf(CNodeFactory.newCNumber(1), IfNodes.newIfThen(CNodeFactory.newCNumber(2), assignment));

        InstructionsInstance inst = new InstructionsInstance("test", "test.c", body);

        Assert.assertEquals(SpecsIo.getResource(VariableDeclarationsResource.IF_IN_IF).replace("\r\n", "\n"),
                inst.getImplementationCode());
    }

    @Test
    public void testShadow() {
        NumericFactory numerics = NumericFactory.defaultFactory();
        VariableType intType = numerics.newInt();

        CInstructionList body = new CInstructionList(FunctionTypeBuilder.newSimple()
                .addInput("s", intType)
                .addInput("m", intType)
                .addInput("e", intType)
                .returning(intType)
                .build());

        VariableNode yNode = CNodeFactory.newVariable("y", intType);
        VariableNode signNode = CNodeFactory.newVariable("sign_1", intType);
        VariableNode iNode = CNodeFactory.newVariable("i", intType);
        // VariableNode sNode = CNodeFactory.newVariable("s", intType);
        VariableNode mNode = CNodeFactory.newVariable("m", intType);
        VariableNode eNode = CNodeFactory.newVariable("e", intType);

        body.addAssignment(yNode, CNodeFactory.newCNumber(0));
        ProviderData providerData = ProviderData.newInstance("data");
        body.addIf(
                FunctionInstanceUtils.getFunctionCall(COperator.NotEqual, providerData,
                        Arrays.asList(mNode, CNodeFactory.newCNumber(0))),
                CNodeFactory.newBlock(
                        CNodeFactory.newInstruction(InstructionType.If, CNodeFactory.newReservedWord(ReservedWord.If),
                                FunctionInstanceUtils.getFunctionCall(COperator.LessThan, providerData,
                                        Arrays.asList(mNode, CNodeFactory.newCNumber(0)))),
                        CNodeFactory.newAssignment(signNode, CNodeFactory.newCNumber(-1))),
                new ForNodes(providerData).newForLoopBlock(iNode, eNode,
                        CNodeFactory.newAssignment(
                                yNode,
                                FunctionInstanceUtils.getFunctionCall(COperator.Addition, providerData,
                                        Arrays.asList(yNode, signNode)))));
        body.addReturn(yNode);

        InstructionsInstance inst = new InstructionsInstance("test", "test.c", body);

        Assert.assertEquals(SpecsIo.getResource(VariableDeclarationsResource.SHADOW).replace("\r\n", "\n"),
                inst.getImplementationCode());
    }

    @Test
    public void testBug36() {
        ProviderData data = ProviderData.newInstance("test-bug36");
        NumericFactory numerics = data.getNumerics();
        VariableType intType = numerics.newInt();

        CInstructionList body = new CInstructionList(FunctionTypeBuilder.newSimple()
                .addInput("dim", intType)
                .returning("y_j", intType)
                .build());

        VariableNode dim = CNodeFactory.newVariable("dim", intType);
        VariableNode y_1 = CNodeFactory.newVariable("y_1", intType);
        VariableNode y_j = CNodeFactory.newVariable("y_j", intType);
        VariableNode i_1 = CNodeFactory.newVariable("i_1", intType);

        ForNodes forNodes = new ForNodes(data);

        List<CNode> innerElseContent = new ArrayList<>();
        innerElseContent.add(CNodeFactory.newAssignment(y_1, y_j));

        List<CNode> innerForInstructions = new ArrayList<>();
        innerForInstructions.add(IfNodes.newIfThenElse(y_j, Collections.emptyList(), innerElseContent));
        innerForInstructions.add(CNodeFactory.newAssignment(y_1, y_j));

        List<CNode> outerForInstructions = new ArrayList<>();
        outerForInstructions.add(forNodes.newForLoopBlock(y_j, CNodeFactory.newCNumber(20), innerForInstructions));

        List<CNode> outerElseContent = new ArrayList<>();
        outerElseContent.add(CNodeFactory.newAssignment(y_1, CNodeFactory.newCNumber(0)));
        outerElseContent
                .add(forNodes.newForLoopBlock(i_1, CNodeFactory.newCNumber(10), outerForInstructions));

        body.addInstruction(IfNodes.newIfThenElse(dim, Collections.emptyList(), outerElseContent));

        InstructionsInstance instance = new InstructionsInstance("min3", "min3.c", body);

        String code = instance.getImplementationCode();

        Assert.assertNotEquals(1, code.indexOf("int i_1;"));
        Assert.assertTrue(code.indexOf("int i_1;") == code.lastIndexOf("int i_1;"));
    }
}
