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

package org.specs.matisselib.tests.pass.elimination.setall;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.passes.posttype.SetAllEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SetAllInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class SetAllEliminationTests extends TestSkeleton {
    @Test
    public void testScalar() {
        FunctionBody body = getBody();

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance());

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$ret", intMatrixType);
        types.put("B$1", intType);
        applyPass(body, types);

        SetAllEliminationResource resource = SetAllEliminationResource.SCALAR;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testFast() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(AssignmentInstruction.fromVariable("B$1", "A$1"));

        SetAllInstruction set = new SetAllInstruction("A$ret", "A$1", "B$1");
        block.addComment(set.toString());
        block.addInstruction(set);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance());

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$ret", intMatrixType);
        types.put("B$1", intMatrixType);
        applyPass(body, types);

        SetAllEliminationResource resource = SetAllEliminationResource.FAST;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testFast2() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addAssignment("$1", 1);
        block.addInstruction(new UntypedFunctionCallInstruction("numel", Arrays.asList("$2"), Arrays.asList("A$1")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("zeros", Arrays.asList("B$1"), Arrays.asList("$1", "$2")));

        SetAllInstruction set = new SetAllInstruction("A$ret", "A$1", "B$1");
        block.addComment(set.toString());
        block.addInstruction(set);

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance());

        Map<String, VariableType> types = new HashMap<>();

        types.put("A$1", intMatrixType);
        types.put("$1", int1Type);
        types.put("$2", intType);
        types.put("A$ret", intMatrixType);
        types.put("B$1", intMatrixType);
        applyPass(body, types);

        SetAllEliminationResource resource = SetAllEliminationResource.FAST2;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSlow() {
        FunctionBody body = getBody();

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, TypeShape.newInstance());

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$ret", intMatrixType);
        types.put("B$1", intMatrixType);
        applyPass(body, types);

        SetAllEliminationResource resource = SetAllEliminationResource.SLOW;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static FunctionBody getBody() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("B$1", 1));

        SetAllInstruction set = new SetAllInstruction("A$ret", "A$1", "B$1");
        block.addComment(set.toString());
        block.addInstruction(set);
        return body;
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("eq", MatlabOp.Equal.getMatlabFunction());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types) {

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        TestUtils.testTypeTransparentPass(new SetAllEliminationPass(), body, types, functions);
    }
}
