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

package org.specs.matlabtocl.v2.tests.passes;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.passes.RedundantCopyForSizeEliminationPass;

import pt.up.fe.specs.util.SpecsIo;

public class RedundantCopyForSizeEliminationTests {
    @Test
    public void testSimple() {
        VariableType underlyingType = VoidType.newInstance(); // STUB

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("arg$1", 0));
        block.addInstruction(new ArgumentInstruction("arg$2", 0));
        block.addInstruction(
                new CompleteReductionInstruction("out$1", ReductionType.MATRIX_SET, "arg$2", underlyingType, null,
                        "arg$1"));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("numel$1"), Arrays.asList("out$1")));
        body.addBlock(block);

        applyTest(body);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(RedundantCopyForSizeEliminationResources.SIMPLE)),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testRetNamePreservation() {
        VariableType underlyingType = VoidType.newInstance(); // STUB

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("arg$1", 0));
        block.addInstruction(new ArgumentInstruction("arg$2", 0));
        block.addInstruction(
                new CompleteReductionInstruction("result$ret", ReductionType.MATRIX_SET, "arg$2", underlyingType, null,
                        "arg$1"));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("numel$1"), Arrays.asList("result$ret")));
        body.addBlock(block);

        applyTest(body);

        Assert.assertEquals(TestUtils
                .normalize(SpecsIo.getResource(RedundantCopyForSizeEliminationResources.RET_NAME_PRESERVATION)),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testEnd() {
        VariableType underlyingType = VoidType.newInstance(); // STUB

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("arg$1", 0));
        block.addInstruction(new ArgumentInstruction("arg$2", 0));
        block.addInstruction(
                new CompleteReductionInstruction("out$1", ReductionType.MATRIX_SET, "arg$2", underlyingType, null,
                        "arg$1"));
        block.addInstruction(
                new EndInstruction("numel$1", "out$1", 0, 1));
        body.addBlock(block);

        applyTest(body);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(RedundantCopyForSizeEliminationResources.END)),
                TestUtils.normalize(body.toString()));
    }

    private static void applyTest(FunctionBody body) {
        TestUtils.testTypeNeutralPass(new RedundantCopyForSizeEliminationPass(), body);
    }
}
