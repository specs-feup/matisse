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

package org.specs.matisselib.tests.pass.elimination.horzcat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.passes.posttype.HorzcatEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class HorzcatEliminationTests extends TestSkeleton {
    @Test
    public void testNone() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("y$ret", 1);
        body.addBlock(block);

        applyPass(body, null, null);

        HorzcatEliminationResource resource = HorzcatEliminationResource.NONE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSingle() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("X$1", 1);
        block.addAssignment("X$2", 2);

        block.addInstruction(new UntypedFunctionCallInstruction("horzcat", Arrays.asList("X$3"), Arrays.asList("X$1",
                "X$2")));
        block.addAssignment("y$1", 1);
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance(1, 2));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("X$1", intType);
        types.put("X$2", intType);
        types.put("X$3", intMatrixType);
        applyPass(body, types, functions);

        HorzcatEliminationResource resource = HorzcatEliminationResource.SINGLE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testMixed() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("X$1", 1);
        block.addAssignment("X$2", 2);

        block.addInstruction(new UntypedFunctionCallInstruction("horzcat", Arrays.asList("X$3"), Arrays.asList("X$1",
                "X$2")));
        block.addInstruction(new UntypedFunctionCallInstruction("horzcat", Arrays.asList("$t"), Arrays.asList("X$1",
                "X$3", "X$2")));
        block.addAssignment("y$1", 1);
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrix2Type = DynamicMatrixType.newInstance(intType, TypeShape.newInstance(1, 2));
        VariableType intMatrix4Type = DynamicMatrixType.newInstance(intType, TypeShape.newInstance(1, 4));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("X$1", intType);
        types.put("X$2", intType);
        types.put("X$3", intMatrix2Type);
        types.put("$t", intMatrix4Type);
        applyPass(body, types, functions);

        HorzcatEliminationResource resource = HorzcatEliminationResource.MIXED;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testUnknownSize() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new UntypedFunctionCallInstruction("horzcat", Arrays.asList("A$2"), Arrays.asList("A$1")));
        block.addAssignment("y$1", 1);
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newRow());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$2", intMatrixType);
        applyPass(body, types, functions);

        HorzcatEliminationResource resource = HorzcatEliminationResource.UNKNOWN_SIZE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testBranch() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();

        block.addInstruction(new UntypedFunctionCallInstruction("horzcat", Arrays.asList("A$2"), Arrays.asList("A$1")));
        block.addAssignment("y$1", 1);
        block.addInstruction(new BranchInstruction("y$1", 1, 2, 3));
        body.addBlock(block);
        body.addBlock(new SsaBlock());
        body.addBlock(new SsaBlock());
        body.addBlock(new SsaBlock());

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newRow());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$2", intMatrixType);
        applyPass(body, types, functions);

        HorzcatEliminationResource resource = HorzcatEliminationResource.BRANCH;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testNested() {
        FunctionBody body = new FunctionBody();

        SsaBlock outerBlock = new SsaBlock();
        body.addBlock(outerBlock);
        outerBlock.addAssignment("$foo", 1);
        outerBlock.addInstruction(new ForInstruction("$foo", "$foo", "$foo", 1, 2));

        SsaBlock loopBlock = new SsaBlock();
        body.addBlock(loopBlock);
        loopBlock.addInstruction(new UntypedFunctionCallInstruction("horzcat", Arrays.asList("A$3"), Arrays
                .asList("A$1", "A$2")));

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);
        endBlock.addInstruction(new PhiInstruction("y$1", Arrays.asList("$foo", "A$3"), Arrays.asList(0, 1)));

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newRow());

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$2", intMatrixType);
        types.put("A$3", intMatrixType);
        applyPass(body, types, functions);

        HorzcatEliminationResource resource = HorzcatEliminationResource.NESTED;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("plus", COperator.Addition.getProvider());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new HorzcatEliminationPass(), body, types, functions);
    }
}
