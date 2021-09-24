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

package org.specs.MatlabToC.CodeBuilder.SsaToC;

import org.junit.Assert;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class TestUtils {
    public static void test(MockSsaToCBuilderService builder, SsaInstruction instruction, SsaToCRule rule,
            String expected) {
        Assert.assertTrue(rule.accepts(builder, instruction));

        CInstructionList currentBlock = new CInstructionList();
        rule.apply(builder, currentBlock, instruction);

        Assert.assertEquals(expected.trim(), currentBlock.toCNode().getCode().trim());
    }
}
