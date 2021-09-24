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

package org.specs.matisselib.tests.pass.arrayaccesssimplifier;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.passes.ssa.ArrayAccessSimplifierPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ArrayAccessSimplifierTests {
    @Test
    public void testSimple() {
	FunctionBody body = new FunctionBody();

	SsaBlock block = new SsaBlock();
	block.addInstruction(new SimpleSetInstruction("A$2", "A$1", Arrays.asList("$index$1"), "$value$1"));
	block.addInstruction(new SimpleGetInstruction("$out$1", "A$2", Arrays.asList("$index$1")));
	body.addBlock(block);

	applyPass(body);
	String obtained = body.toString();

	Assert.assertEquals(
		TestUtils.normalize(SpecsIo.getResource(ArrayAccessSimplifierResource.SIMPLE.getResource())),
		TestUtils.normalize(obtained));
    }

    @Test
    public void testInvalid() {
	FunctionBody body = new FunctionBody();

	SsaBlock block = new SsaBlock();
	block.addInstruction(new SimpleSetInstruction("A$2", "A$1", Arrays.asList("$index$1"), "$value$1"));
	block.addInstruction(new SimpleGetInstruction("$out$1", "A$2", Arrays.asList("$index$2")));
	body.addBlock(block);

	applyPass(body);
	String obtained = body.toString();

	Assert.assertEquals(
		TestUtils.normalize(SpecsIo.getResource(ArrayAccessSimplifierResource.INVALID.getResource())),
		TestUtils.normalize(obtained));
    }

    private static void applyPass(FunctionBody body) {
	TestUtils.testTypeNeutralPass(new ArrayAccessSimplifierPass(), body);
    }
}
