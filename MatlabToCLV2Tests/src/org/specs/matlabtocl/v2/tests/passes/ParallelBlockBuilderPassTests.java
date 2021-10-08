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

package org.specs.matlabtocl.v2.tests.passes;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.services.log.NullLogService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.instructions.EndDirectiveInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelDirectiveInstruction;
import org.specs.matlabtocl.v2.ssa.passes.ParallelBlockBuilderPass;
import org.specs.matlabtocl.v2.tests.CLTestUtils;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;

import pt.up.fe.specs.util.SpecsIo;

public class ParallelBlockBuilderPassTests {
    @Test
    public void testFunctionWide() {
        FunctionBody functionBody = new FunctionBody("test", 2);

        SsaBlock mainBlock = new SsaBlock();
        mainBlock.addInstruction(new LineInstruction(1));
        mainBlock.addInstruction(new ParallelDirectiveInstruction(CLTestUtils.buildDummySettings()));
        mainBlock.addInstruction(new LineInstruction(2));
        mainBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        mainBlock.addInstruction(new LineInstruction(3));
        mainBlock.addInstruction(new ArgumentInstruction("A$2", 1));
        mainBlock.addInstruction(new LineInstruction(4));
        mainBlock.addInstruction(AssignmentInstruction.fromVariable("A$3", "A$1"));
        mainBlock.addInstruction(new LineInstruction(5));
        mainBlock.addInstruction(AssignmentInstruction.fromVariable("y$ret", "A$2"));
        functionBody.addBlock(mainBlock);

        test(functionBody, ParallelBlockBuilderPassResources.FUNCTIONWIDE);
    }

    @Test
    public void testFunctionWideParameter() {
        FunctionBody functionBody = new FunctionBody("test", 2);

        SsaBlock mainBlock = new SsaBlock();
        mainBlock.addInstruction(new LineInstruction(2));
        mainBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        mainBlock.addInstruction(new LineInstruction(3));
        mainBlock.addInstruction(new ArgumentInstruction("A$2", 1));
        mainBlock.addInstruction(new LineInstruction(1));
        mainBlock.addAssignment("$fixed_work_groups$1", 4);
        ParallelRegionSettings parallelSettings = CLTestUtils.buildDummySettings();
        parallelSettings.schedule = ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL;
        parallelSettings.scheduleNames = Arrays.asList("$fixed_work_groups$1");
        mainBlock.addInstruction(new ParallelDirectiveInstruction(parallelSettings));
        mainBlock.addInstruction(new LineInstruction(4));
        mainBlock.addInstruction(AssignmentInstruction.fromVariable("A$3", "A$1"));
        mainBlock.addInstruction(new LineInstruction(5));
        mainBlock.addInstruction(AssignmentInstruction.fromVariable("y$ret", "A$2"));
        functionBody.addBlock(mainBlock);

        test(functionBody, ParallelBlockBuilderPassResources.FUNCTIONWIDE_PARAMETER);
    }

    @Test
    public void testRegion() {
        FunctionBody functionBody = new FunctionBody("test", 2);

        SsaBlock mainBlock = new SsaBlock();
        mainBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        mainBlock.addInstruction(new ParallelDirectiveInstruction(CLTestUtils.buildDummySettings()));
        mainBlock.addInstruction(AssignmentInstruction.fromVariable("A$2", "A$1"));
        mainBlock.addInstruction(new EndDirectiveInstruction());
        mainBlock.addInstruction(AssignmentInstruction.fromVariable("A$3", "A$2"));
        functionBody.addBlock(mainBlock);

        test(functionBody, ParallelBlockBuilderPassResources.REGION);
    }

    @Test
    public void testNestedRegion() {
        FunctionBody functionBody = new FunctionBody("test", 2);

        SsaBlock mainBlock = new SsaBlock();
        mainBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        mainBlock.addInstruction(new ParallelDirectiveInstruction(CLTestUtils.buildDummySettings()));
        mainBlock.addInstruction(new BranchInstruction("A$1", 1, 2, 3));
        functionBody.addBlock(mainBlock);

        SsaBlock ifBlock = new SsaBlock();
        ifBlock.addInstruction(new CommentInstruction("If"));
        functionBody.addBlock(ifBlock);

        SsaBlock elseBlock = new SsaBlock();
        elseBlock.addInstruction(new CommentInstruction("Else"));
        functionBody.addBlock(elseBlock);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new CommentInstruction("End if"));
        endBlock.addInstruction(new EndDirectiveInstruction());
        endBlock.addInstruction(AssignmentInstruction.fromVariable("A$3", "A$1"));
        functionBody.addBlock(endBlock);

        test(functionBody, ParallelBlockBuilderPassResources.NESTEDREGION);
    }

    @Test
    public void testFunctionWideWithBranch() {
        FunctionBody functionBody = new FunctionBody("test", 2);

        SsaBlock mainBlock = new SsaBlock();
        mainBlock.addInstruction(new LineInstruction(1));
        mainBlock.addInstruction(new ParallelDirectiveInstruction(CLTestUtils.buildDummySettings()));
        mainBlock.addInstruction(new LineInstruction(3));
        mainBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        mainBlock.addInstruction(new BranchInstruction("A$1", 1, 2, 3));
        functionBody.addBlock(mainBlock);

        SsaBlock ifBlock = new SsaBlock();
        ifBlock.addInstruction(new CommentInstruction("If"));
        functionBody.addBlock(ifBlock);

        SsaBlock elseBlock = new SsaBlock();
        elseBlock.addInstruction(new CommentInstruction("Else"));
        functionBody.addBlock(elseBlock);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new CommentInstruction("End if"));
        endBlock.addInstruction(AssignmentInstruction.fromVariable("A$3", "A$1"));
        functionBody.addBlock(endBlock);

        test(functionBody, ParallelBlockBuilderPassResources.FUNCTIONWIDEWITHBRANCH);
    }

    @Test
    public void testFunctionWideWithFor() {
        FunctionBody functionBody = new FunctionBody("test", 2);

        SsaBlock mainBlock = new SsaBlock();
        mainBlock.addInstruction(new LineInstruction(1));
        mainBlock.addInstruction(new ParallelDirectiveInstruction(CLTestUtils.buildDummySettings()));
        mainBlock.addInstruction(new LineInstruction(3));
        mainBlock.addInstruction(new ArgumentInstruction("A$1", 0));
        mainBlock.addInstruction(new ForInstruction("A$1", "A$1", "A$1", 1, 2));
        functionBody.addBlock(mainBlock);

        SsaBlock forBlock = new SsaBlock();
        forBlock.addInstruction(new CommentInstruction("For"));
        forBlock.addInstruction(new PhiInstruction("A$for", Arrays.asList("A$1", "A$for"), Arrays.asList(0, 1)));
        functionBody.addBlock(forBlock);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new CommentInstruction("End for"));
        endBlock.addInstruction(new PhiInstruction("A$ret", Arrays.asList("A$1", "A$for"), Arrays.asList(0, 1)));
        functionBody.addBlock(endBlock);

        test(functionBody, ParallelBlockBuilderPassResources.FUNCTIONWIDEWITHFOR);
    }

    private static void test(FunctionBody functionBody, ParallelBlockBuilderPassResources testResource) {
        SimpleDataStore data = new SimpleDataStore("test");
        data.add(PreTypeInferenceServices.LOG, new NullLogService());
        new ParallelBlockBuilderPass().apply(functionBody, data);

        String expected = SpecsIo.getResource(testResource);
        String obtained = functionBody.toString();

        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
