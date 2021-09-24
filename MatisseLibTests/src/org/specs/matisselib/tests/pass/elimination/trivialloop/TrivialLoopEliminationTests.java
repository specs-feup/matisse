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

package org.specs.matisselib.tests.pass.elimination.trivialloop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.passes.posttype.TrivialLoopEliminationPass;
import org.specs.matisselib.passes.ssa.BlockReorderingPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class TrivialLoopEliminationTests extends TestSkeleton {
    @Test
    public void testZeroInterval() {
        testForWithBranch(1, 0, 1, TrivialLoopEliminationResource.DELETEDFOR);
    }

    @Test
    public void testOutOfRange() {
        testForWithBranch(1, 1, 0, TrivialLoopEliminationResource.DELETEDFOR);
    }

    @Test
    public void testOutOfRangeReversed() {
        testForWithBranch(0, -1, 1, TrivialLoopEliminationResource.DELETEDFOR);
    }

    @Test
    public void testSingleWithBranch() {
        testForWithBranch(1, 2, 1, TrivialLoopEliminationResource.SINGLEFOR);
    }

    @Test
    public void testSingle() {
        testForWithoutBranch(1, 2, 1, TrivialLoopEliminationResource.SIMPLE_SINGLEFOR);
    }

    @Test
    public void testKeptFor() {
        testForWithBranch(1, 1, 2, TrivialLoopEliminationResource.KEPTFOR);
    }

    private static void testForWithBranch(int start, int interval, int end,
            TrivialLoopEliminationResource resource) {

        FunctionBody body = new FunctionBody();
        SsaBlock startBlock = new SsaBlock();
        int startId = body.addBlock(startBlock);
        SsaBlock loopStartBlock = new SsaBlock();
        int loopStartId = body.addBlock(loopStartBlock);
        SsaBlock loopIfBlock = new SsaBlock();
        int loopIfId = body.addBlock(loopIfBlock);
        SsaBlock loopElseBlock = new SsaBlock();
        int loopElseId = body.addBlock(loopElseBlock);
        SsaBlock loopEndBlock = new SsaBlock();
        int loopEndId = body.addBlock(loopEndBlock);
        SsaBlock afterLoopBlock = new SsaBlock();
        int afterLoopId = body.addBlock(afterLoopBlock);
        SsaBlock secondLoopBlock = new SsaBlock();
        int secondLoopId = body.addBlock(secondLoopBlock);
        SsaBlock endBlock = new SsaBlock();
        int endId = body.addBlock(endBlock);

        startBlock.addAssignment("$init", 1);
        startBlock.addInstruction(new ForInstruction("$start", "$interval", "$end", loopStartId, afterLoopId));

        loopStartBlock.addInstruction(
                new PhiInstruction("$body", Arrays.asList("$init", "$body"), Arrays.asList(startId, loopEndId)));
        loopStartBlock.addInstruction(new IterInstruction("$iter"));
        loopStartBlock.addAssignment("$bar", "$iter");
        loopStartBlock.addInstruction(new BranchInstruction("$body", loopIfId, loopElseId, loopEndId));
        loopEndBlock.addAssignment("$loopend", 1);
        afterLoopBlock.addInstruction(new PhiInstruction("$afterloop", Arrays.asList("$init", "$body"),
                Arrays.asList(startId, loopEndId)));
        afterLoopBlock.addInstruction(new ForInstruction("$init", "$init", "$afterloop", secondLoopId, endId));
        secondLoopBlock.addInstruction(
                new PhiInstruction("$2", Arrays.asList("$init", "$2"), Arrays.asList(afterLoopId, secondLoopId)));

        Map<String, VariableType> types = new HashMap<>();
        types.put("$start", getNumerics().newInt(start));
        types.put("$interval", getNumerics().newInt(interval));
        types.put("$end", getNumerics().newInt(end));
        types.put("$init", getNumerics().newInt(1));
        types.put("$loopend", getNumerics().newInt(1));
        types.put("$iter", getNumerics().newInt());
        types.put("$body", getNumerics().newInt());
        types.put("$afterloop", getNumerics().newInt());
        types.put("$2", getNumerics().newInt());

        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void testForWithoutBranch(int start, int interval, int end,
            TrivialLoopEliminationResource resource) {

        FunctionBody body = new FunctionBody();
        SsaBlock startBlock = new SsaBlock();
        int startId = body.addBlock(startBlock);
        SsaBlock loopStartBlock = new SsaBlock();
        int loopStartId = body.addBlock(loopStartBlock);
        SsaBlock afterLoopBlock = new SsaBlock();
        int afterLoopId = body.addBlock(afterLoopBlock);
        SsaBlock secondLoopBlock = new SsaBlock();
        int secondLoopId = body.addBlock(secondLoopBlock);
        SsaBlock endBlock = new SsaBlock();
        int endId = body.addBlock(endBlock);

        startBlock.addAssignment("$init", 1);
        startBlock.addInstruction(new ForInstruction("$start", "$interval", "$end", loopStartId, afterLoopId));

        loopStartBlock.addInstruction(
                new PhiInstruction("$body", Arrays.asList("$init", "$body"), Arrays.asList(startId, loopStartId)));
        loopStartBlock.addInstruction(new IterInstruction("$iter"));
        loopStartBlock.addAssignment("$bar", "$iter");
        loopStartBlock.addAssignment("$loopend", 1);
        afterLoopBlock.addInstruction(new PhiInstruction("$afterloop", Arrays.asList("$init", "$body"),
                Arrays.asList(startId, loopStartId)));
        afterLoopBlock.addInstruction(new ForInstruction("$init", "$init", "$afterloop", secondLoopId, endId));
        secondLoopBlock.addInstruction(
                new PhiInstruction("$2", Arrays.asList("$init", "$2"), Arrays.asList(afterLoopId, secondLoopId)));

        Map<String, VariableType> types = new HashMap<>();
        types.put("$start", getNumerics().newInt(start));
        types.put("$interval", getNumerics().newInt(interval));
        types.put("$end", getNumerics().newInt(end));
        types.put("$init", getNumerics().newInt(1));
        types.put("$loopend", getNumerics().newInt(1));
        types.put("$iter", getNumerics().newInt());
        types.put("$body", getNumerics().newInt());
        types.put("$afterloop", getNumerics().newInt());
        types.put("$2", getNumerics().newInt());

        applyPass(body, types);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
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

        TestUtils.testTypeTransparentPass(new TrivialLoopEliminationPass(), body, types, functions);
        new BlockReorderingPass().apply(body, new CommonPassData("trivial-loop-elimination-block-reordering-data"));
    }
}
