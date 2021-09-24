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

package org.specs.matisselib.tests.cpass.forsimplifier;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.cir.ForSimplifierPass;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ForSimplifierTests {
    @Test
    public void testSimple() {
        CInstructionList instructions = new CInstructionList();

        InstanceBuilder helper = new GenericInstanceBuilder(ProviderData.newInstance("dummy"));
        NumericFactory numerics = helper.getNumerics();

        ScalarType intType = numerics.newInt();
        DynamicMatrixType intMatrixType = DynamicMatrixType.newInstance(intType);

        VariableNode inductionVar = CNodeFactory.newVariable("i", intType);
        CNode endValue = CNodeFactory.newVariable("N", intType);
        CNode sourceMatrix = CNodeFactory.newVariable("B", intMatrixType);
        CNode targetMatrix = CNodeFactory.newVariable("A", intMatrixType);
        CNode minusOne = helper.getFunctionCall(COperator.Subtraction, inductionVar, CNodeFactory.newCNumber(1));

        CNode get = helper.getFunctionCall(intMatrixType.matrix().functions().get(), sourceMatrix, minusOne);
        CNode forBody = helper.getFunctionCall(intMatrixType.matrix().functions().set(), targetMatrix, minusOne, get);

        CNode forNode = new ForNodes(helper.getData()).newForLoopBlock(inductionVar, CNodeFactory.newCNumber(1),
                endValue,
                COperator.LessThanOrEqual,
                Arrays.asList(forBody));

        instructions.addInstruction(forNode);

        String expected = SpecsIo.getResource(ForSimplifierResource.SIMPLE);

        performTest(instructions, expected);
    }

    @Test
    public void testNonInteger() {
        CInstructionList instructions = new CInstructionList();

        InstanceBuilder helper = new GenericInstanceBuilder(ProviderData.newInstance("dummy"));
        NumericFactory numerics = helper.getNumerics();

        ScalarType intType = numerics.newInt();
        ScalarType doubleType = numerics.newDouble();
        DynamicMatrixType intMatrixType = DynamicMatrixType.newInstance(intType);

        VariableNode inductionVar = CNodeFactory.newVariable("i", intType);
        CNode endValue = CNodeFactory.newVariable("N", doubleType);
        CNode sourceMatrix = CNodeFactory.newVariable("B", intMatrixType);
        CNode targetMatrix = CNodeFactory.newVariable("A", intMatrixType);
        CNode minusOne = helper.getFunctionCall(COperator.Subtraction, inductionVar, CNodeFactory.newCNumber(1));

        CNode get = helper.getFunctionCall(intMatrixType.matrix().functions().get(), sourceMatrix, minusOne);
        CNode forBody = helper.getFunctionCall(intMatrixType.matrix().functions().set(), targetMatrix, minusOne, get);

        CNode forNode = new ForNodes(helper.getData()).newForLoopBlock(inductionVar, CNodeFactory.newCNumber(1),
                endValue,
                COperator.LessThanOrEqual,
                Arrays.asList(forBody));

        instructions.addInstruction(forNode);

        String expected = SpecsIo.getResource(ForSimplifierResource.NONINTEGER);

        performTest(instructions, expected);
    }

    private static void performTest(CInstructionList instructions, String expected) {
        new ForSimplifierPass().apply(instructions,
                ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings()));
        String obtained = instructions.get().get(0).getCode();
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
