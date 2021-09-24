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

package org.specs.matisselib.tests.pass.branch.constant;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.passes.posttype.ConstantBranchEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;

import pt.up.fe.specs.util.SpecsIo;

public class ConstantBranchEliminationTests {
    @Test
    public void testSimple() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("arg$1", 0));
        body.addBlock(block);

        Function<String, Optional<Boolean>> isConstant = name -> Optional.empty();
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.SIMPLE;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testRelevantBranch() {
        FunctionBody body = buildSimpleBranchFunction();

        Function<String, Optional<Boolean>> isConstant = name -> Optional.empty();
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.RELEVANT;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testRemoveElse() {
        FunctionBody body = buildSimpleBranchFunction();

        Function<String, Optional<Boolean>> isConstant = name -> Optional.of(true);
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.TRUE;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testRemoveIf() {
        FunctionBody body = buildSimpleBranchFunction();

        Function<String, Optional<Boolean>> isConstant = name -> Optional.of(false);
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.FALSE;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testConditionalChangeTrue() {
        FunctionBody body = buildConditionalChangeBody();

        Function<String, Optional<Boolean>> isConstant = name -> Optional.of(true);
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.CONDITIONAL_CHANGE_TRUE;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testConditionalChangeFalse() {
        FunctionBody body = buildConditionalChangeBody();

        Function<String, Optional<Boolean>> isConstant = name -> Optional.of(false);
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.CONDITIONAL_CHANGE_FALSE;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testEraseOuterBranch() {
        FunctionBody body = buildNestedBranchBody();

        Function<String, Optional<Boolean>> isConstant = name -> name.equals("arg$0") ? Optional.of(true) : Optional
                .empty();
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.NESTED_BRANCH;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testEraseInnerBranch() {
        FunctionBody body = buildNestedBranchBody();

        Function<String, Optional<Boolean>> isConstant = name -> name.equals("arg$1") ? Optional.of(true) : Optional
                .empty();
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.NESTED_BRANCH2;

        performTest(body, isConstant, resource);
    }

    @Test
    public void testLoopInBranch() {
        FunctionBody body = new FunctionBody();
        SsaBlock rootBlock = new SsaBlock();
        rootBlock.addAssignment("$A$1", 1);
        rootBlock.addInstruction(new BranchInstruction("$A$1", 1, 4, 5));
        body.addBlock(rootBlock);

        SsaBlock beforeLoop = new SsaBlock();
        beforeLoop.addAssignment("B$1", 1);
        beforeLoop.addInstruction(new ForInstruction("$A$1", "$A$1", "$A$1", 2, 3));
        body.addBlock(beforeLoop);

        SsaBlock loop = new SsaBlock();
        loop.addAssignment("B$2", 2);
        body.addBlock(loop);

        SsaBlock afterLoop = new SsaBlock();
        afterLoop.addInstruction(new PhiInstruction("B$3", Arrays.asList("B$1", "B$2"), Arrays.asList(1, 2)));
        body.addBlock(afterLoop);

        SsaBlock elseBlock = new SsaBlock();
        elseBlock.addAssignment("B$4", 3);
        body.addBlock(elseBlock);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("B$ret", Arrays.asList("B$3", "B$4"), Arrays.asList(3, 4)));
        body.addBlock(endBlock);

        Function<String, Optional<Boolean>> isConstant = name -> name.equals("$A$1") ? Optional.of(true) : Optional
                .empty();
        ConstantBranchEliminationResource resource = ConstantBranchEliminationResource.LOOP_IN_BRANCH;

        performTest(body, isConstant, resource);
    }

    private static FunctionBody buildNestedBranchBody() {
        FunctionBody body = new FunctionBody();

        SsaBlock startBlock = new SsaBlock(); // 0
        SsaBlock outerIf = new SsaBlock(); // 1
        SsaBlock innerIf = new SsaBlock(); // 2
        SsaBlock innerElse = new SsaBlock(); // 3
        SsaBlock innerEnd = new SsaBlock(); // 4
        SsaBlock outerElse = new SsaBlock(); // 5
        SsaBlock outerEnd = new SsaBlock(); // 6

        body.addBlock(startBlock);
        int outerIfIndex = body.addBlock(outerIf);
        int innerIfIndex = body.addBlock(innerIf);
        int innerElseIndex = body.addBlock(innerElse);
        int innerEndIndex = body.addBlock(innerEnd);
        int outerElseIndex = body.addBlock(outerElse);
        int outerEndIndex = body.addBlock(outerEnd);

        startBlock.addInstruction(new ArgumentInstruction("arg$0", 0));
        startBlock.addInstruction(new ArgumentInstruction("arg$1", 1));
        startBlock.addInstruction(new BranchInstruction("arg$0", outerIfIndex, outerElseIndex, outerEndIndex));

        outerIf.addAssignment("$outerif", 1);
        outerIf.addInstruction(new BranchInstruction("arg$1", innerIfIndex, innerElseIndex, innerEndIndex));

        innerIf.addAssignment("$innerif", 2);
        innerElse.addAssignment("$innerelse", 3);
        innerEnd.addInstruction(new PhiInstruction("$innerend", Arrays.asList("$innerif", "$innerelse"),
                Arrays.asList(innerIfIndex, innerElseIndex)));

        outerElse.addAssignment("$outerelse", 4);
        outerEnd.addInstruction(new PhiInstruction("$outerend", Arrays.asList("$innerend", "$outerelse"),
                Arrays.asList(innerEndIndex, outerElseIndex)));
        return body;
    }

    private static FunctionBody buildConditionalChangeBody() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new BranchInstruction("$1", 1, 2, 3));
        body.addBlock(block);
        SsaBlock block2 = new SsaBlock();
        block2.addAssignment("$2", 2);
        body.addBlock(block2);
        body.addBlock(new SsaBlock());
        SsaBlock block3 = new SsaBlock();
        block3.addInstruction(new PhiInstruction("$3", Arrays.asList("$2", "$1"), Arrays.asList(1, 2)));
        body.addBlock(block3);
        return body;
    }

    private static FunctionBody buildSimpleBranchFunction() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("arg$1", 0));
        block.addInstruction(new BranchInstruction("arg$1", 1, 2, 3));
        body.addBlock(block);
        SsaBlock trueBlock = new SsaBlock();
        trueBlock.addAssignment("$true", 1);
        body.addBlock(trueBlock);
        SsaBlock falseBlock = new SsaBlock();
        falseBlock.addAssignment("$false", 2);
        body.addBlock(falseBlock);
        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("$result", Arrays.asList("$true", "$false"), Arrays.asList(1, 2)));
        body.addBlock(endBlock);
        return body;
    }

    private static void performTest(FunctionBody body, Function<String, Optional<Boolean>> isConstant,
            ConstantBranchEliminationResource resource) {

        // new ConstantBranchEliminationPass();
        ConstantBranchEliminationPass.apply(body, new SimpleDataStore("test"), isConstant);

        String expected = SpecsIo.getResource(resource);
        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(body.toString()));
    }
}
