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

package org.specs.matisselib.tests.cpass.constantpropagation;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StdInd.StdIntType;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.cir.ConstantPropagationPass;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ConstantPropagationTests {
    @Test
    public void testSimple() {
        CInstructionList instructions = new CInstructionList();

        NumericTypeV2 intType = NumericTypeV2.newInstance(CTypeV2.INT, 32);
        VariableNode helloVar = CNodeFactory.newVariable("hello", intType);
        VariableNode worldVar = CNodeFactory.newVariable("world", intType);
        instructions.addAssignment(helloVar, CNodeFactory.newCNumber(1));
        instructions.addAssignment(worldVar, CNodeFactory.newCNumber(2));
        instructions.addAssignment(worldVar, CNodeFactory.newCNumber(3));

        instructions.addReturn(helloVar);
        instructions.addReturn(worldVar);

        String expected = SpecsIo.getResource(ConstantPropagationResource.SIMPLE);

        performTest(instructions, expected);
    }

    @Test
    public void testLiteral() {
        CInstructionList instructions = new CInstructionList();

        NumericTypeV2 intType = NumericTypeV2.newInstance(CTypeV2.INT, 32);
        VariableNode helloVar = CNodeFactory.newVariable("hello", intType);
        instructions.addAssignment(helloVar, CNodeFactory.newCNumber(1));

        instructions.addLiteralInstruction("LITERAL(hello);");
        instructions.addLiteralVariable(helloVar.getVariable());

        String expected = SpecsIo.getResource(ConstantPropagationResource.LITERAL);

        performTest(instructions, expected);
    }

    @Test
    public void testBlock() {
        CInstructionList instructions = new CInstructionList();

        NumericTypeV2 intType = NumericTypeV2.newInstance(CTypeV2.INT, 32);
        VariableNode helloVar = CNodeFactory.newVariable("hello", intType);
        instructions.addAssignment(helloVar, CNodeFactory.newCNumber(1));

        instructions.addIf(CNodeFactory.newCNumber(1),
                CNodeFactory.newAssignment(helloVar, CNodeFactory.newCNumber(2)));

        instructions.addReturn(helloVar);

        String expected = SpecsIo.getResource(ConstantPropagationResource.BLOCK);

        performTest(instructions, expected);
    }

    @Test
    public void testBlock2() {
        CInstructionList instructions = new CInstructionList();

        NumericTypeV2 intType = NumericTypeV2.newInstance(CTypeV2.INT, 32);
        VariableNode helloVar = CNodeFactory.newVariable("hello", intType);
        instructions.addAssignment(helloVar, CNodeFactory.newCNumber(1));

        ProviderData providerData = ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings());
        List<CNode> prodValues = Arrays.<CNode> asList(helloVar, CNodeFactory.newCNumber(2));
        CNode newValue = COperator.Multiplication
                .getCheckedInstance(providerData.createFromNodes(prodValues))
                .newFunctionCall(prodValues);
        instructions.addIf(CNodeFactory.newCNumber(1),
                CNodeFactory.newAssignment(helloVar, newValue));

        instructions.addReturn(helloVar);

        String expected = SpecsIo.getResource(ConstantPropagationResource.BLOCK2);

        performTest(instructions, expected);
    }

    @Test
    public void testOutput() {
        CInstructionList instructions = new CInstructionList();

        NumericTypeV2 intType = NumericTypeV2.newInstance(CTypeV2.INT, 32);
        VariableNode helloVar = CNodeFactory.newVariable("hello", intType);
        VariableNode worldVar = CNodeFactory.newVariable("world", intType);

        instructions.addAssignment(helloVar, CNodeFactory.newCNumber(1));
        instructions.addAssignment(worldVar, CNodeFactory.newCNumber(2));

        FunctionType fooType = FunctionTypeBuilder.newWithOutputsAsInputs()
                .addOutputAsInput("x", intType)
                .build();
        FunctionInstance fooInstance = new InstructionsInstance(fooType, "foo", "foo.c", new CInstructionList());
        instructions.addFunctionCall(fooInstance, helloVar);

        FunctionType barType = FunctionTypeBuilder.newSimple()
                .addInput("x", intType)
                .returningVoid()
                .build();
        FunctionInstance barInstance = new InstructionsInstance(barType, "bar", "bar.c", new CInstructionList());
        instructions.addFunctionCall(barInstance, worldVar);

        String expected = SpecsIo.getResource(ConstantPropagationResource.OUTPUT);

        performTest(instructions, expected);
    }

    @Test
    public void testParam() {
        StdIntType uint32 = StdIntType.newInstance(32, true);
        FunctionType functionType = FunctionTypeBuilder.newSimple()
                .addInput("cond", uint32)
                .addInput("a", uint32)
                .returning(uint32)
                .build();
        CInstructionList instructions = new CInstructionList(functionType);

        CNode condNode = CNodeFactory.newVariable("cond", uint32);
        CNode aNode = CNodeFactory.newVariable("a", uint32);

        instructions.addIf(condNode, CNodeFactory.newAssignment(aNode, CNodeFactory.newCNumber(1)));
        instructions.addReturn(aNode);

        String expected = SpecsIo.getResource(ConstantPropagationResource.PARAM);
        performTest(instructions, expected);
    }

    @Test
    public void testConditional() {
        VariableType intType = NumericFactory.defaultFactory().newInt();
        CNode yNode = CNodeFactory.newVariable("Y", intType);
        CNode aNode = CNodeFactory.newVariable("a", intType);

        CInstructionList instructions = new CInstructionList();
        instructions.addAssignment(yNode, aNode);

        List<CNode> gtNodes = Arrays.asList(yNode, CNodeFactory.newCNumber(255));

        ProviderData gtData = ProviderData.newInstance("test-conditional");
        gtData = gtData.createFromNodes(gtNodes);
        instructions.addIf(
                COperator.GreaterThan.getCheckedInstance(gtData).newFunctionCall(gtNodes),
                CNodeFactory.newAssignment(yNode, CNodeFactory.newCNumber(255)));

        instructions.addReturn(yNode);

        String expected = SpecsIo.getResource(ConstantPropagationResource.CONDITIONAL);
        performTest(instructions, expected);
    }

    private static void performTest(CInstructionList instructions, String expected) {
        if (instructions.getFunctionTypes() == null) {
            instructions.setFunctionTypes(FunctionTypeBuilder.newSimple().returningVoid().build());
        }

        new ConstantPropagationPass().apply(instructions,
                ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings()));
        String obtained = TestUtils.generateCCode(instructions);
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
