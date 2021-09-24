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

package org.specs.matisselib.tests.cpass.redundantreturnremoval;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.passes.cir.RedundantReturnRemovalPass;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class RedundantReturnRemovalTests {
    @Test
    public void testSimple() {
        CInstructionList instructions = new CInstructionList(FunctionTypeBuilder
                .newSimple()
                .returningVoid()
                .build());

        instructions.addReturn();

        String expected = SpecsIo.getResource(RedundantReturnRemovalResource.SIMPLE);

        performTest(instructions, expected);
    }

    @Test
    public void testRelevant() {
        CInstructionList instructions = new CInstructionList();

        instructions.addReturn(CNodeFactory.newCNumber(1));

        String expected = SpecsIo.getResource(RedundantReturnRemovalResource.RELEVANT);

        performTest(instructions, expected);
    }

    private static void performTest(CInstructionList instructions, String expected) {
        new RedundantReturnRemovalPass().apply(instructions,
                ProviderData.newInstance(MatlabToCOptionUtils.newDefaultSettings()));
        String obtained = instructions.get().isEmpty() ? "" : instructions.get().get(0).getCode();
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
