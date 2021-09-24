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

package org.specs.matisselib.tests.pass.blockreordering;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.matisselib.passes.ssa.BlockReorderingPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class BlockReorderingTests {
    @Test
    public void testBranch() {
	FunctionBody body = new FunctionBody();

	SsaBlock block = new SsaBlock();
	block.addInstruction(new BranchInstruction("$1", 3, 1, 2));
	body.addBlock(block);

	SsaBlock trueBlock = new SsaBlock();
	trueBlock.addAssignment("$true", 1);

	SsaBlock falseBlock = new SsaBlock();
	falseBlock.addAssignment("$false", 1);

	SsaBlock endBlock = new SsaBlock();
	endBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("$3", "$4"), Arrays.asList(3, 1)));

	body.addBlock(falseBlock);
	body.addBlock(endBlock);
	body.addBlock(trueBlock);

	applyPass(body);

	BlockReorderingResource resource = BlockReorderingResource.SIMPLE;
	String obtained = body.toString();

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
		TestUtils.normalize(obtained));
    }

    private static void applyPass(FunctionBody body) {

	CommonPassData passData = new CommonPassData("foo");

	new BlockReorderingPass().apply(body, passData);
    }
}
