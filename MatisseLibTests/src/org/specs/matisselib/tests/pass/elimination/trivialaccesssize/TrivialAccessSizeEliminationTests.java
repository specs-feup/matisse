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

package org.specs.matisselib.tests.pass.elimination.trivialaccesssize;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.passes.posttype.TrivialAccessSizeEliminationPass;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AccessSizeInstruction;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class TrivialAccessSizeEliminationTests {
    @Test
    public void testReturn() {

	FunctionBody body = new FunctionBody();
	SsaBlock block = new SsaBlock();
	body.addBlock(block);

	block.addInstruction(new ArgumentInstruction("A$1", 0));
	block.addInstruction(new ArgumentInstruction("I$1", 1));
	block.addInstruction(new AccessSizeInstruction("y$ret", "A$1", "I$1"));
	block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("w$1"), Arrays.asList("y$ret")));

	VariableType intMatrixType = DynamicMatrixType.newInstance(NumericFactory.defaultFactory().newInt());

	Map<String, VariableType> types = new HashMap<>();
	types.put("A$1", intMatrixType);
	types.put("I$1", intMatrixType);
	types.put("y$ret", intMatrixType);

	applyPass(body, types, getDefaultFunctions());
	TrivialAccessSizeEliminationResource resource = TrivialAccessSizeEliminationResource.RETURN;

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
		TestUtils.normalize(body.toString()));
    }

    @Test
    public void testTrivial() {

	FunctionBody body = new FunctionBody();
	SsaBlock block = new SsaBlock();
	body.addBlock(block);

	block.addInstruction(new ArgumentInstruction("A$1", 0));
	block.addInstruction(new ArgumentInstruction("I$1", 1));
	block.addInstruction(new AccessSizeInstruction("y$1", "A$1", "I$1"));
	block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("w$1"), Arrays.asList("y$1")));

	VariableType intMatrixType = DynamicMatrixType.newInstance(NumericFactory.defaultFactory().newInt());

	Map<String, VariableType> types = new HashMap<>();
	types.put("A$1", intMatrixType);
	types.put("I$1", intMatrixType);
	types.put("y$1", intMatrixType);

	applyPass(body, types, getDefaultFunctions());
	TrivialAccessSizeEliminationResource resource = TrivialAccessSizeEliminationResource.TRIVIAL;

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
		TestUtils.normalize(body.toString()));
    }

    @Test
    public void testValidUse() {

	FunctionBody body = new FunctionBody();
	SsaBlock block = new SsaBlock();
	body.addBlock(block);

	block.addInstruction(new ArgumentInstruction("A$1", 0));
	block.addInstruction(new ArgumentInstruction("I$1", 1));
	block.addInstruction(new AccessSizeInstruction("y$1", "A$1", "I$1"));
	block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("w$1"), Arrays.asList("y$1")));
	block.addAssignment("$value", 2);
	block.addInstruction(new SimpleGetInstruction("a$1", "w$1", "$value"));

	VariableType intMatrixType = DynamicMatrixType.newInstance(NumericFactory.defaultFactory().newInt());

	Map<String, VariableType> types = new HashMap<>();
	types.put("A$1", intMatrixType);
	types.put("I$1", intMatrixType);
	types.put("y$1", intMatrixType);

	applyPass(body, types, getDefaultFunctions());
	TrivialAccessSizeEliminationResource resource = TrivialAccessSizeEliminationResource.VALIDUSE;

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
		TestUtils.normalize(body.toString()));
    }

    @Test
    public void testInvalidUse() {

	FunctionBody body = new FunctionBody();
	SsaBlock block = new SsaBlock();
	body.addBlock(block);

	block.addInstruction(new ArgumentInstruction("A$1", 0));
	block.addInstruction(new ArgumentInstruction("I$1", 1));
	block.addInstruction(new AccessSizeInstruction("y$1", "A$1", "I$1"));
	block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("w$1"), Arrays.asList("y$1")));
	block.addAssignment("$value", 2);
	block.addInstruction(new SimpleGetInstruction("a$1", "w$1", Arrays.asList("$value", "$value")));

	VariableType intMatrixType = DynamicMatrixType.newInstance(NumericFactory.defaultFactory().newInt());

	Map<String, VariableType> types = new HashMap<>();
	types.put("A$1", intMatrixType);
	types.put("I$1", intMatrixType);
	types.put("y$1", intMatrixType);

	applyPass(body, types, getDefaultFunctions());
	TrivialAccessSizeEliminationResource resource = TrivialAccessSizeEliminationResource.INVALIDUSE;

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
		TestUtils.normalize(body.toString()));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
	Map<String, InstanceProvider> functions = new HashMap<>();
	functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
	return functions;
    }

    private static void applyPass(FunctionBody body,
	    Map<String, VariableType> types,
	    Map<String, InstanceProvider> functions) {

	TestUtils.testTypeTransparentPass(new TrivialAccessSizeEliminationPass(), body, types, functions);
	new SsaValidatorPass("test-trivial-access-size").apply(body, new CommonPassData("data"));
    }
}
