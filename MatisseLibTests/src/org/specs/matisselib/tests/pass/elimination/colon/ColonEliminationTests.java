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

package org.specs.matisselib.tests.pass.elimination.colon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.passes.posttype.ColonEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ColonEliminationTests extends TestSkeleton {
    @Test
    public void testNone() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("y$ret", 1);
        body.addBlock(block);

        applyPass(body, null, null);

        ColonEliminationResource resource = ColonEliminationResource.NONE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple() {
        ProviderData data = ProviderData.newInstance("foo");

        VariableType intType = data.getNumerics().newInt();
        VariableType int1Type = data.getNumerics().newInt(1);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        data = data.create(int1Type, intType);
        FunctionType functionType = functions.get("colon").getType(data);

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("$foo", 0));
        block.addAssignment("X$2", 1);
        block.addInstruction(new TypedFunctionCallInstruction("colon", functionType, "X$1", "X$2", "$foo"));
        block.addInstruction(AssignmentInstruction.fromVariable("y$ret", "X$1"));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("X$2", int1Type);
        types.put("$foo", intType);
        types.put("X$1", functionType.getOutputTypes().get(0));
        applyPass(body, types, functions);

        ColonEliminationResource resource = ColonEliminationResource.SIMPLE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testScalar() {

        VariableType int1Type = getNumerics().newInt(1);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        ProviderData data = ProviderData.newInstance("foo");
        data = data.create(int1Type, int1Type);
        FunctionType functionType = functions.get("colon").getType(data);

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addAssignment("$foo", 1);
        block.addInstruction(new TypedFunctionCallInstruction("colon", functionType, "X$1", "$foo", "$foo"));
        block.addInstruction(AssignmentInstruction.fromVariable("y$ret", "X$1"));

        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$foo", int1Type);
        types.put("X$1", functionType.getOutputTypes().get(0));
        applyPass(body, types, functions);

        ColonEliminationResource resource = ColonEliminationResource.SCALAR;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testNested() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        ProviderData data = ProviderData.newInstance("foo");
        data = data.create(int1Type, intType);
        FunctionType functionType = functions.get("colon").getType(data);

        FunctionBody body = new FunctionBody();

        SsaBlock outer = new SsaBlock();
        body.addBlock(outer);

        outer.addInstruction(new ArgumentInstruction("$foo", 0));
        outer.addAssignment("X$2", 1);
        outer.addInstruction(new ForInstruction("X$2", "X$2", "X$2", 1, 2));

        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new TypedFunctionCallInstruction("colon", functionType, "X$1", "X$2", "$foo"));

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);

        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("$foo", "X$1"), Arrays.asList(0, 1)));

        Map<String, VariableType> types = new HashMap<>();
        types.put("X$2", intType);
        types.put("$foo", intType);
        types.put("X$1", functionType.getOutputTypes().get(0));
        applyPass(body, types, functions);

        ColonEliminationResource resource = ColonEliminationResource.NESTED;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("colon", MatlabOp.Colon.getMatlabFunction());
        functions.put("max", MathFunction.MAX.getMatlabFunction());
        functions.put("plus", COperator.Addition.getProvider());
        functions.put("minus", COperator.Subtraction.getProvider());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new ColonEliminationPass(), body, types, functions);
    }
}
