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

package org.specs.matisselib.tests.pass.convert.torange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.passes.posttype.ConvertToRangePass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class ConvertToRangeTests extends TestSkeleton {
    @Test
    public void testGet() {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$a", 0));
        block.addInstruction(new ArgumentInstruction("$b", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new EndInstruction("$1", "$a", 1, 3));
        block.addInstruction(
                new UntypedFunctionCallInstruction("colon", Arrays.asList("$2"), Arrays.asList("$one", "$1")));
        block.addInstruction(new EndInstruction("$3", "$a", 2, 3));
        block.addInstruction(
                new UntypedFunctionCallInstruction("colon", Arrays.asList("$4"), Arrays.asList("$one", "$3")));
        block.addInstruction(new MatrixGetInstruction("$5", "$a", Arrays.asList("$b", "$2", "$4")));
        body.addBlock(block);

        VariableType int1Type = getNumerics().newInt(1);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$one", int1Type);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(ConvertToRangeResource.GET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testPartialRangeGet() {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$a", 0));
        block.addInstruction(new ArgumentInstruction("$b", 1));
        block.addInstruction(new ArgumentInstruction("$c", 2));
        block.addAssignment("$one", 1);
        block.addInstruction(
                new UntypedFunctionCallInstruction("colon", Arrays.asList("$2"), Arrays.asList("$one", "$b")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("colon", Arrays.asList("$4"), Arrays.asList("$b", "$c")));
        block.addInstruction(new MatrixGetInstruction("$5", "$a", Arrays.asList("$b", "$2", "$4")));
        body.addBlock(block);

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$one", int1Type);
        types.put("$b", intType);
        types.put("$c", intType);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(ConvertToRangeResource.PARTIAL_RANGE_GET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testBasicGet() {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$a", 0));
        block.addInstruction(new ArgumentInstruction("$b", 1));
        block.addInstruction(new MatrixGetInstruction("$1", "$a", Arrays.asList("$b", "$b")));
        body.addBlock(block);

        VariableType int1Type = getNumerics().newInt(1);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$one", int1Type);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(ConvertToRangeResource.BASIC_GET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSet() {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$a", 0));
        block.addInstruction(new ArgumentInstruction("$b", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new EndInstruction("$1", "$a", 1, 3));
        block.addInstruction(
                new UntypedFunctionCallInstruction("colon", Arrays.asList("$2"), Arrays.asList("$one", "$1")));
        block.addInstruction(new EndInstruction("$3", "$a", 2, 3));
        block.addInstruction(
                new UntypedFunctionCallInstruction("colon", Arrays.asList("$4"), Arrays.asList("$one", "$3")));
        block.addAssignment("$value", 2);
        block.addInstruction(new MatrixSetInstruction("$5", "$a", Arrays.asList("$b", "$2", "$4"), "$value"));
        body.addBlock(block);

        VariableType int1Type = getNumerics().newInt(1);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$one", int1Type);
        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(ConvertToRangeResource.SET.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void applyPass(FunctionBody body, Map<String, VariableType> types) {
        TestUtils.testTypeTransparentPass(new ConvertToRangePass(), body, types, new HashMap<>());
    }
}
