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

package org.specs.matisselib.tests.pass.elimination.redundantcast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.Misc.CastFunctions;
import org.specs.matisselib.passes.posttype.RedundantCastEliminationPass;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestUtils;

public class RedundantCastEliminationTests {
    @Test
    public void testSimple() {
	performTest("double", Arrays.asList(NumericFactory.defaultFactory().newDouble()), true);
    }

    @Test
    public void testNotRedundant() {
	performTest("double", Arrays.asList(NumericFactory.defaultFactory().newFloat()), false);
    }

    @Test
    public void testInvalidFunction() {
	performTest("sin", Arrays.asList(NumericFactory.defaultFactory().newDouble()), false);
    }

    private static void performTest(String functionName, List<VariableType> types, boolean shouldErase) {
	InstanceProvider function = getDefaultFunctions().get(functionName);
	FunctionType fType = function.getType(ProviderData.newInstance("test").create(types));

	List<String> inputs = new ArrayList<>();
	for (int i = 0; i < types.size(); ++i) {
	    inputs.add("$i_" + i);
	}

	FunctionBody body = new FunctionBody();
	SsaBlock block = new SsaBlock();
	block.addInstruction(new TypedFunctionCallInstruction(functionName, fType, Arrays.asList("$out"), inputs));
	block.addAssignment("$out2", "$out");
	body.addBlock(block);

	String expected = shouldErase ? "block #0:\n  $out2 = $i_0\n" : body.toString();

	Map<String, VariableType> varTypes = new HashMap<>();
	TestUtils.testTypeTransparentPass(new RedundantCastEliminationPass(), body, varTypes, getDefaultFunctions());

	Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(body.toString()));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
	Map<String, InstanceProvider> functions = new HashMap<>();

	for (MatlabFunction func : CastFunctions.getCastPrototypes()) {
	    functions.put(func.getFunctionName(), func);
	}

	functions.put("sin", MathFunction.SIN.getMatlabFunction());

	return functions;
    }
}
