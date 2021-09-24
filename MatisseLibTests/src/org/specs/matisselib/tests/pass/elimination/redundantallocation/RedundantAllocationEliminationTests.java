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

package org.specs.matisselib.tests.pass.elimination.redundantallocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.passes.posttype.RedundantAllocationEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class RedundantAllocationEliminationTests {

    @Test
    public void testSimple() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("$value$1", 0));
        block.addInstruction(new ArgumentInstruction("$value$2", 1));
        block.addInstruction(new UntypedFunctionCallInstruction("ones", Arrays.asList("A$1"),
                Arrays.asList("$value$1", "$value$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("$result$1"), Arrays.asList("A$1")));

        body.addBlock(block);

        NumericFactory numerics = NumericFactory.defaultFactory();
        VariableType intType = numerics.newInt();
        DynamicMatrixType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$value$1", intType);
        types.put("$value$2", intType);
        types.put("A$1", intMatrixType);
        types.put("$result$1", intType);

        applyPass(body, types, getDefaultFunctions());

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(RedundantAllocationEliminationResource.SIMPLE)),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testTriple() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("$value$1", 0));
        block.addInstruction(new ArgumentInstruction("$value$2", 1));
        block.addInstruction(new ArgumentInstruction("$value$3", 2));
        block.addInstruction(new UntypedFunctionCallInstruction("ones", Arrays.asList("A$1"),
                Arrays.asList("$value$1", "$value$2", "$value$3")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("$result$1"), Arrays.asList("A$1")));

        body.addBlock(block);

        NumericFactory numerics = NumericFactory.defaultFactory();
        VariableType intType = numerics.newInt();
        DynamicMatrixType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$value$1", intType);
        types.put("$value$2", intType);
        types.put("$value$3", intType);
        types.put("A$1", intMatrixType);
        types.put("$result$1", intType);

        applyPass(body, types, getDefaultFunctions());

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(RedundantAllocationEliminationResource.TRIPLE)),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testInvalid() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new ArgumentInstruction("$value$1", 0));
        block.addInstruction(new ArgumentInstruction("$value$2", 1));
        block.addInstruction(new UntypedFunctionCallInstruction("ones", Arrays.asList("A$1"),
                Arrays.asList("$value$1", "$value$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("$result$1"), Arrays.asList("A$1")));
        block.addInstruction(new SimpleGetInstruction("$result$2", "A$1", "$value$1"));

        body.addBlock(block);

        NumericFactory numerics = NumericFactory.defaultFactory();
        VariableType intType = numerics.newInt();
        DynamicMatrixType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$value$1", intType);
        types.put("$value$2", intType);
        types.put("A$1", intMatrixType);
        types.put("$result$1", intType);
        types.put("$result$2", intType);

        applyPass(body, types, getDefaultFunctions());

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(RedundantAllocationEliminationResource.INVALID)),
                TestUtils.normalize(body.toString()));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("times", MatlabOp.Multiplication.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new RedundantAllocationEliminationPass(), body, types, functions);
    }
}
