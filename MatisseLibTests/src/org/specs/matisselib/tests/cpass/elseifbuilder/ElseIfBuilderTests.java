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

package org.specs.matisselib.tests.cpass.elseifbuilder;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.cir.ElseIfBuilderPass;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ElseIfBuilderTests {
    @Test
    public void testSimple() {
        CInstructionList instructions = new CInstructionList();

        CNode outerIfBody = CNodeFactory.newLiteral("A");
        CNode innerIfBody = CNodeFactory.newLiteral("B");
        CNode innerElseBody = CNodeFactory.newLiteral("C");

        CNode outerElseBody = IfNodes.newIfThenElse(CNodeFactory.newLiteral("y"), innerIfBody, innerElseBody);
        CNode outerIf = IfNodes.newIfThenElse(CNodeFactory.newLiteral("x"), outerIfBody, outerElseBody);

        instructions.addInstruction(outerIf);

        String expected = SpecsIo.getResource(ElseIfBuilderResource.SIMPLE);

        performTest(instructions, expected);
    }

    @Test
    public void testTooMuchContent() {
        CInstructionList instructions = new CInstructionList();

        CNode outerIfBody = CNodeFactory.newLiteral("A");
        CNode innerIfBody = CNodeFactory.newLiteral("B");
        CNode afterInner = CNodeFactory.newLiteral("C");

        CNode outerElseBody = IfNodes.newIfThen(CNodeFactory.newLiteral("y"), innerIfBody);
        CNode outerIf = IfNodes.newIfThenElse(CNodeFactory.newLiteral("x"),
                Arrays.asList(outerIfBody),
                Arrays.asList(outerElseBody, afterInner));

        instructions.addInstruction(outerIf);

        String expected = SpecsIo.getResource(ElseIfBuilderResource.TOOMUCHCONTENT);

        performTest(instructions, expected);
    }

    private static void performTest(CInstructionList instructions, String expected) {
        new ElseIfBuilderPass().apply(instructions,
                ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings()));
        String obtained = instructions.get().get(0).getCode();
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
