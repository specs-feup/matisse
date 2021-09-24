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

package org.specs.matisselib.tests.cpass.shortcircuit;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VerbatimNode;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.cir.ShortCircuitedConditionalBuilderPass;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ShortCircuitedConditionalBuilderTests extends TestSkeleton {
    @Test
    public void testSimpleOr() {
        CInstructionList instructions = new CInstructionList(FunctionTypeBuilder
                .newSimple()
                .returningVoid()
                .build());

        NumericTypeV2 intType = getNumerics().newInt();
        ProviderData providerData = ProviderData.newInstance("test");
        Variable variable = new Variable("hello", intType);
        List<CNode> arguments = Arrays.asList(CNodeFactory.newLiteral("<left>", intType),
                CNodeFactory.newLiteral("<right>", intType));
        CNode elseExpression = COperator.Equal.getProvider()
                .getCheckedInstance(providerData.createFromNodes(arguments))
                .newFunctionCall(arguments);

        instructions.addInstruction(
                IfNodes.newIfThenElse(CNodeFactory.newLiteral("<condition>", intType),
                        CNodeFactory.newAssignment(variable, CNodeFactory.newCNumber(1)),
                        CNodeFactory.newAssignment(variable, elseExpression)));

        String expected = SpecsIo.getResource(ShortCircuitedConditionalBuilderResource.SIMPLE_OR);

        performTest(instructions, expected);
    }

    @Test
    public void testSimpleAnd() {
        CInstructionList instructions = new CInstructionList(FunctionTypeBuilder
                .newSimple()
                .returningVoid()
                .build());

        NumericTypeV2 intType = getNumerics().newInt();
        ProviderData providerData = ProviderData.newInstance("test");
        Variable variable = new Variable("hello", intType);
        List<CNode> arguments = Arrays.asList(CNodeFactory.newLiteral("<left>", intType),
                CNodeFactory.newLiteral("<right>", intType));
        CNode elseExpression = COperator.Equal.getProvider()
                .getCheckedInstance(providerData.createFromNodes(arguments))
                .newFunctionCall(arguments);

        instructions.addInstruction(
                IfNodes.newIfThenElse(CNodeFactory.newLiteral("<condition>", intType),
                        CNodeFactory.newAssignment(variable, CNodeFactory.newCNumber(0)),
                        CNodeFactory.newAssignment(variable, elseExpression)));

        String expected = SpecsIo.getResource(ShortCircuitedConditionalBuilderResource.SIMPLE_AND);

        performTest(instructions, expected);
    }

    @Test
    public void testSimpleElseAnd() {
        CInstructionList instructions = new CInstructionList(FunctionTypeBuilder
                .newSimple()
                .returningVoid()
                .build());

        NumericTypeV2 intType = getNumerics().newInt();
        ProviderData providerData = ProviderData.newInstance("test");
        Variable variable = new Variable("hello", intType);
        List<CNode> arguments = Arrays.asList(CNodeFactory.newLiteral("<left>", intType),
                CNodeFactory.newLiteral("<right>", intType));
        CNode ifExpression = COperator.Equal.getProvider()
                .getCheckedInstance(providerData.createFromNodes(arguments))
                .newFunctionCall(arguments);

        instructions.addInstruction(
                IfNodes.newIfThenElse(CNodeFactory.newLiteral("<condition>", intType),
                        CNodeFactory.newAssignment(variable, ifExpression),
                        CNodeFactory.newAssignment(variable, CNodeFactory.newCNumber(0))));

        String expected = SpecsIo.getResource(ShortCircuitedConditionalBuilderResource.SIMPLE_ELSE_AND);

        performTest(instructions, expected);
    }

    @Test
    public void testSimpleAnd2() {
        CInstructionList instructions = new CInstructionList(FunctionTypeBuilder
                .newSimple()
                .returningVoid()
                .build());

        NumericTypeV2 intType = getNumerics().newInt();
        ProviderData providerData = ProviderData.newInstance("test");
        Variable variable = new Variable("hello", intType);
        List<CNode> arguments = Arrays.asList(CNodeFactory.newLiteral("<left>", intType),
                CNodeFactory.newLiteral("<right>", intType));
        CNode elseExpression = COperator.Equal.getProvider()
                .getCheckedInstance(providerData.createFromNodes(arguments))
                .newFunctionCall(arguments);

        VerbatimNode baseCondition = CNodeFactory.newLiteral("<condition>", intType);
        CNode condition = COperator.LogicalNegation
                .getCheckedInstance(providerData.createFromNodes(baseCondition))
                .newFunctionCall(baseCondition);
        instructions.addInstruction(
                IfNodes.newIfThenElse(condition,
                        CNodeFactory.newAssignment(variable, CNodeFactory.newCNumber(0)),
                        CNodeFactory.newAssignment(variable, elseExpression)));

        String expected = SpecsIo.getResource(ShortCircuitedConditionalBuilderResource.SIMPLE_AND2);

        performTest(instructions, expected);
    }

    @Test
    public void testInvalid() {
        CInstructionList instructions = new CInstructionList();

        NumericTypeV2 intType = getNumerics().newInt();
        Variable variable = new Variable("hello", intType);

        CNode elseExpression = CNodeFactory.newCNumber(2);

        instructions.addInstruction(
                IfNodes.newIfThenElse(CNodeFactory.newLiteral("<condition>", intType),
                        CNodeFactory.newAssignment(variable, CNodeFactory.newCNumber(1)),
                        CNodeFactory.newAssignment(variable, elseExpression)));

        String expected = SpecsIo.getResource(ShortCircuitedConditionalBuilderResource.INVALID);

        performTest(instructions, expected);
    }

    private static void performTest(CInstructionList instructions, String expected) {
        new ShortCircuitedConditionalBuilderPass().apply(instructions,
                ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings()));
        String obtained = instructions.get().isEmpty() ? "" : instructions.get().get(0).getCode();
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
