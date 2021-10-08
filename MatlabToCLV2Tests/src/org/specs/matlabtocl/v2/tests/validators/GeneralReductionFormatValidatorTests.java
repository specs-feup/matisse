package org.specs.matlabtocl.v2.tests.validators;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.GeneralReductionFormatValidationResult;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.GeneralReductionFormatValidator;

public class GeneralReductionFormatValidatorTests {
    @Test
    public void testSimple() {
        FunctionBody body = new FunctionBody();
        SsaBlock mainBlock = new SsaBlock();
        body.addBlock(mainBlock);
        SsaBlock loopBlock = new SsaBlock();
        body.addBlock(loopBlock);
        SsaBlock afterBlock = new SsaBlock();
        body.addBlock(afterBlock);

        mainBlock.addInstruction(new ForInstruction("$1", "$1", "$1", 1, 2));

        loopBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("$1", "$3"), Arrays.asList(0, 1)));
        loopBlock.addAssignment("$3", "$2");

        afterBlock.addInstruction(new PhiInstruction("$4", Arrays.asList("$1", "$3"), Arrays.asList(0, 1)));

        LoopVariable var = new LoopVariable("$1", "$2", "$3", "$4");
        GeneralReductionFormatValidationResult result = GeneralReductionFormatValidator
                .test(body, Arrays.asList(0), Arrays.asList(var)).get();

        Assert.assertEquals(Arrays.asList("$1", "$2", "$3"), result.getReductionNames());
    }

    @Test
    public void testIf() {
        FunctionBody body = new FunctionBody();

        SsaBlock mainBlock = new SsaBlock();
        int mainBlockIndex = body.addBlock(mainBlock);
        SsaBlock loopBlock = new SsaBlock();
        int loopBlockIndex = body.addBlock(loopBlock);
        SsaBlock ifBlock = new SsaBlock();
        int ifBlockIndex = body.addBlock(ifBlock);
        SsaBlock elseBlock = new SsaBlock();
        int elseBlockIndex = body.addBlock(elseBlock);
        SsaBlock afterIfBlock = new SsaBlock();
        int afterIfBlockIndex = body.addBlock(afterIfBlock);
        SsaBlock endBlock = new SsaBlock();
        int endBlockIndex = body.addBlock(endBlock);

        mainBlock.addInstruction(new ForInstruction("$1", "$1", "$1", loopBlockIndex, endBlockIndex));

        loopBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("$1", "$5"), Arrays.asList(mainBlockIndex,
                afterIfBlockIndex)));
        loopBlock.addInstruction(new BranchInstruction("$cond", ifBlockIndex, elseBlockIndex, afterIfBlockIndex));

        ifBlock.addAssignment("$3", "$2");
        elseBlock.addAssignment("$4", "$2");
        afterIfBlock.addInstruction(new PhiInstruction("$5", Arrays.asList("$3", "$4"), Arrays.asList(ifBlockIndex,
                elseBlockIndex)));

        LoopVariable var = new LoopVariable("$1", "$2", "$5", "$6");
        GeneralReductionFormatValidationResult result = GeneralReductionFormatValidator
                .test(body, Arrays.asList(mainBlockIndex), Arrays.asList(var))
                .get();

        Assert.assertEquals(Arrays.asList("$1", "$2", "$3", "$4", "$5"), result.getReductionNames());
    }

    @Test
    public void testFor() {
        FunctionBody body = new FunctionBody();

        SsaBlock mainBlock = new SsaBlock();
        int mainBlockIndex = body.addBlock(mainBlock);
        SsaBlock loopBlock = new SsaBlock();
        int loopBlockIndex = body.addBlock(loopBlock);
        SsaBlock innerBlock = new SsaBlock();
        int innerBlockIndex = body.addBlock(innerBlock);
        SsaBlock afterInnerBlock = new SsaBlock();
        int afterInnerBlockIndex = body.addBlock(afterInnerBlock);
        SsaBlock endBlock = new SsaBlock();
        int endBlockIndex = body.addBlock(endBlock);

        mainBlock.addInstruction(new ForInstruction("$1", "$1", "$1", loopBlockIndex, endBlockIndex));

        loopBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("$1", "$6"), Arrays.asList(mainBlockIndex,
                afterInnerBlockIndex)));
        loopBlock.addInstruction(new ForInstruction("$a", "$b", "$c", innerBlockIndex, afterInnerBlockIndex));

        innerBlock.addInstruction(new PhiInstruction("$3", Arrays.asList("$2", "$4"), Arrays.asList(loopBlockIndex,
                innerBlockIndex)));
        innerBlock.addInstruction(new SimpleSetInstruction("$4", "$3", Arrays.asList("$i"), "$v"));
        afterInnerBlock.addInstruction(new PhiInstruction("$5", Arrays.asList("$2", "$4"), Arrays.asList(
                loopBlockIndex, innerBlockIndex)));
        afterInnerBlock.addAssignment("$6", "$5");

        LoopVariable var = new LoopVariable("$1", "$2", "$6", "$7");
        GeneralReductionFormatValidationResult result = GeneralReductionFormatValidator
                .test(body, Arrays.asList(mainBlockIndex), Arrays.asList(var))
                .get();

        Assert.assertEquals(Arrays.asList("$1", "$2", "$3", "$4", "$5", "$6"), result.getReductionNames());
    }

    @Test
    public void testNoParticipationFor() {
        FunctionBody body = new FunctionBody();

        SsaBlock mainBlock = new SsaBlock();
        int mainBlockIndex = body.addBlock(mainBlock);
        SsaBlock loopBlock = new SsaBlock();
        int loopBlockIndex = body.addBlock(loopBlock);
        SsaBlock innerBlock = new SsaBlock();
        int innerBlockIndex = body.addBlock(innerBlock);
        SsaBlock afterInnerBlock = new SsaBlock();
        int afterInnerBlockIndex = body.addBlock(afterInnerBlock);
        SsaBlock endBlock = new SsaBlock();
        int endBlockIndex = body.addBlock(endBlock);

        mainBlock.addInstruction(new ForInstruction("$1", "$1", "$1", loopBlockIndex, endBlockIndex));

        loopBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("$1", "$3"), Arrays.asList(mainBlockIndex,
                afterInnerBlockIndex)));
        loopBlock.addInstruction(new ForInstruction("$a", "$b", "$c", innerBlockIndex, afterInnerBlockIndex));

        afterInnerBlock.addAssignment("$3", "$2");

        LoopVariable var = new LoopVariable("$1", "$2", "$3", "$4");
        GeneralReductionFormatValidationResult result = GeneralReductionFormatValidator
                .test(body, Arrays.asList(mainBlockIndex), Arrays.asList(var))
                .get();

        Assert.assertEquals(Arrays.asList("$1", "$2", "$3"), result.getReductionNames());
    }

    @Test
    public void testDoubleReference() {
        FunctionBody body = new FunctionBody();
        SsaBlock mainBlock = new SsaBlock();
        body.addBlock(mainBlock);
        SsaBlock loopBlock = new SsaBlock();
        body.addBlock(loopBlock);
        SsaBlock afterBlock = new SsaBlock();
        body.addBlock(afterBlock);

        mainBlock.addInstruction(new ForInstruction("$1", "$1", "$1", 1, 2));

        loopBlock.addInstruction(new PhiInstruction("$2", Arrays.asList("$1", "$3"), Arrays.asList(0, 1)));
        loopBlock.addAssignment("$3", "$2");
        loopBlock.addAssignment("$extra", "$2");

        afterBlock.addInstruction(new PhiInstruction("$4", Arrays.asList("$1", "$3"), Arrays.asList(0, 1)));

        LoopVariable var = new LoopVariable("$1", "$2", "$3", "$4");
        boolean isPresent = GeneralReductionFormatValidator
                .test(body, Arrays.asList(0), Arrays.asList(var)).isPresent();

        Assert.assertFalse(isPresent);
    }
}
