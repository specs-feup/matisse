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

package org.specs.matisselib.tests.pass.rae;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.matisselib.passes.ssa.RedundantAssignmentEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class RedundantAssignmentEliminationTest {

    @Test
    public void runTest() {

	FunctionBody body = new FunctionBody();
	SsaBlock block = new SsaBlock();

	block.addInstruction(new ArgumentInstruction("y$1", 0));
	block.addInstruction(AssignmentInstruction.fromVariable("y$2", "y$1"));
	block.addAssignment("x$ret", 1);
	block.addInstruction(AssignmentInstruction.fromVariable("y$ret", "y$2"));

	body.addBlock(block);

	new RedundantAssignmentEliminationPass(true).apply(body, new CommonPassData("hello"));

	Assert.assertEquals(
		TestUtils.normalize(SpecsIo.getResource(RedundantAssignmentEliminationResource.RESULT1.getResource())),
		TestUtils.normalize(body.toString()));
    }
}
