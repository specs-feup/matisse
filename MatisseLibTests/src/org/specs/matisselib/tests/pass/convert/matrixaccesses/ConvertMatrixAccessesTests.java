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

package org.specs.matisselib.tests.pass.convert.matrixaccesses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.functionproperties.AssumeMatrixIndicesInRangeProperty;
import org.specs.matisselib.passes.posttype.ConvertMatrixAccessesPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ConvertMatrixAccessesTests extends TestSkeleton {
    @Test
    public void testGet() {
        FunctionBody body = new FunctionBody();
        body.addProperty(new AssumeMatrixIndicesInRangeProperty());

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$0", 0));
        block.addInstruction(new ArgumentInstruction("$1", 1));
        block.addInstruction(new MatrixGetInstruction("y$ret", "$0", Arrays.asList("$1")));
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$0", intMatrixType);
        types.put("$1", intType);
        types.put("y$ret", intType);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(ConvertMatrixAccessesResource.GET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testGetNoProperty() {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$0", 0));
        block.addInstruction(new ArgumentInstruction("$1", 1));
        block.addInstruction(new MatrixGetInstruction("y$ret", "$0", Arrays.asList("$1")));
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$0", intMatrixType);
        types.put("$1", intType);
        types.put("y$ret", intType);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(ConvertMatrixAccessesResource.GET_NO_PROPERTY.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSet() {
        FunctionBody body = new FunctionBody();
        body.addProperty(new AssumeMatrixIndicesInRangeProperty());

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$0", 0));
        block.addInstruction(new ArgumentInstruction("$1", 1));
        block.addInstruction(new ArgumentInstruction("$2", 2));
        block.addInstruction(new MatrixSetInstruction("y$ret", "$0", Arrays.asList("$1"), "$2"));
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$0", intMatrixType);
        types.put("$1", intType);
        types.put("$2", intType);
        types.put("y$ret", intMatrixType);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(ConvertMatrixAccessesResource.SET.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void applyPass(FunctionBody body, Map<String, VariableType> types) {
        TestUtils.testTypeTransparentPass(new ConvertMatrixAccessesPass(), body, types, new HashMap<>());
    }
}
