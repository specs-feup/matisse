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

package org.specs.matisselib.tests.pass.loopgetsimplifier.preallocator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.passes.posttype.loopgetsimplifier.MatrixPreallocatorPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.FunctionComposer;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;

public class MatrixPreallocatorTests extends TestSkeleton {
    @Test
    public void testSimple1D() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("n$1", 0));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        loopContent.addInstruction(new IterInstruction("i$2"));
        loopContent.addInstruction(new MatrixSetInstruction("y$3", "y$2", Arrays.asList("i$2"), "i$2"));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("n$1", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE1D.getResource())),
                TestUtils.normalize(obtained));

        Assert.assertEquals("DynamicMatrixType(INT, shape=[Matrix Shape: [1, -1], Dims: 1])",
                types.get("y$1").toString());
    }

    @Test
    public void testSimple1DDerived() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("n$1", 0));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        loopContent.addInstruction(new IterInstruction("i$2"));
        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInput(intType)
                .addInput(intType)
                .returning(intType)
                .build();
        loopContent.addInstruction(
                new TypedFunctionCallInstruction("plus", functionType, Arrays.asList("a$1"),
                        Arrays.asList("i$2", "i$2")));
        loopContent.addInstruction(new MatrixSetInstruction("y$3", "y$2", Arrays.asList("a$1"), "i$2"));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("n$1", intType);
        types.put("a$1", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE1D_DERIVED.getResource())),
                TestUtils.normalize(obtained));

        Assert.assertEquals("DynamicMatrixType(INT, shape=[Matrix Shape: [1, -1], Dims: 1])",
                types.get("y$1").toString());
    }

    @Test
    public void testSimple1DDropDerived() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("n$1", 0));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        loopContent.addInstruction(new IterInstruction("i$2"));
        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInput(intType)
                .addInput(intType)
                .returning(intType)
                .build();
        loopContent.addInstruction(
                new TypedFunctionCallInstruction("minus", functionType, Arrays.asList("a$1"),
                        Arrays.asList("n$1", "i$2")));
        loopContent.addInstruction(new MatrixSetInstruction("y$3", "y$2", Arrays.asList("a$1"), "i$2"));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("n$1", intType);
        types.put("a$1", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils
                        .normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE1D_DROP_DERIVED.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple1Dx2() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("n$1", 0));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        loopContent.addInstruction(new IterInstruction("i$2"));
        loopContent.addInstruction(new MatrixSetInstruction("y$3", "y$2", Arrays.asList("i$2", "i$2"), "i$2"));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("y$1", "y$3"), Arrays.asList(0, 1)));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("n$1", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE1DX2.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple2D() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("m$1", 0));
        block.addInstruction(new ArgumentInstruction("n$1", 1));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "m$1", 1, 4));
        body.addBlock(block);

        SsaBlock outerLoopContent = new SsaBlock();
        outerLoopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        outerLoopContent.addInstruction(new IterInstruction("i$2"));
        outerLoopContent.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 2, 3));
        body.addBlock(outerLoopContent);

        SsaBlock innerLoopContent = new SsaBlock();
        innerLoopContent.addInstruction(new PhiInstruction("y$3", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        innerLoopContent.addInstruction(new IterInstruction("j$2"));
        innerLoopContent.addInstruction(new MatrixSetInstruction("y$4", "y$3", Arrays.asList("i$2", "j$2"), "i$2"));
        body.addBlock(innerLoopContent);

        SsaBlock afterInnerLoop = new SsaBlock();
        afterInnerLoop.addInstruction(new PhiInstruction("y$5", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        body.addBlock(afterInnerLoop);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$6", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        endBlock.addInstruction(new UntypedFunctionCallInstruction("f", Arrays.asList("y$ret"), Arrays.asList("y$6")));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$4", intMatrixType);
        types.put("y$5", intMatrixType);
        types.put("y$6", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("m$1", intType);
        types.put("n$1", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE2D.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple2DPreallocated() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("m$1", 0));
        block.addInstruction(new ArgumentInstruction("n$1", 1));
        block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_dims", Arrays.asList("y$1"),
                Arrays.asList("m$1", "m$1")));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 1, 4));
        body.addBlock(block);

        SsaBlock outerLoopContent = new SsaBlock();
        outerLoopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        outerLoopContent.addInstruction(new IterInstruction("i$2"));
        outerLoopContent.addInstruction(new ForInstruction("$start$1", "$interval$1", "n$1", 2, 3));
        body.addBlock(outerLoopContent);

        SsaBlock innerLoopContent = new SsaBlock();
        innerLoopContent.addInstruction(new PhiInstruction("y$3", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        innerLoopContent.addInstruction(new IterInstruction("j$2"));
        innerLoopContent.addInstruction(new MatrixSetInstruction("y$4", "y$3", Arrays.asList("i$2", "j$2"), "i$2"));
        body.addBlock(innerLoopContent);

        SsaBlock afterInnerLoop = new SsaBlock();
        afterInnerLoop.addInstruction(new PhiInstruction("y$5", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        body.addBlock(afterInnerLoop);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$6", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        endBlock.addInstruction(new UntypedFunctionCallInstruction("f", Arrays.asList("y$ret"), Arrays.asList("y$6")));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$1", intMatrixType);
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$4", intMatrixType);
        types.put("y$5", intMatrixType);
        types.put("y$6", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("m$1", intType);
        types.put("n$1", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils
                .normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE2D_PREALLOCATED.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple2DInner() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("m$1", 0));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "m$1", 1, 4));
        body.addBlock(block);

        SsaBlock outerLoopContent = new SsaBlock();
        outerLoopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        outerLoopContent.addInstruction(new IterInstruction("i$2"));
        outerLoopContent.addInstruction(new ForInstruction("$start$1", "$interval$1", "i$2", 2, 3));
        body.addBlock(outerLoopContent);

        SsaBlock innerLoopContent = new SsaBlock();
        innerLoopContent.addInstruction(new PhiInstruction("y$3", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        innerLoopContent.addInstruction(new IterInstruction("j$2"));
        innerLoopContent.addInstruction(new MatrixSetInstruction("y$4", "y$3", Arrays.asList("i$2", "j$2"), "i$2"));
        body.addBlock(innerLoopContent);

        SsaBlock afterInnerLoop = new SsaBlock();
        afterInnerLoop.addInstruction(new PhiInstruction("y$5", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        body.addBlock(afterInnerLoop);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$4", intMatrixType);
        types.put("y$5", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("m$1", intType);
        types.put("i$2", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE2D_INNER.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple2DExtraUse() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("m$1", 0));
        block.addInstruction(AssignmentInstruction.fromUndefinedValue("y$1"));
        block.addAssignment("$start$1", 1);
        block.addAssignment("$interval$1", 1);
        block.addInstruction(new ForInstruction("$start$1", "$interval$1", "m$1", 1, 4));
        body.addBlock(block);

        SsaBlock outerLoopContent = new SsaBlock();
        outerLoopContent.addInstruction(new PhiInstruction("y$2", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        outerLoopContent.addInstruction(new IterInstruction("i$2"));
        outerLoopContent.addInstruction(new ForInstruction("$start$1", "$interval$1", "m$1", 2, 3));
        body.addBlock(outerLoopContent);

        SsaBlock innerLoopContent = new SsaBlock();
        innerLoopContent.addInstruction(new PhiInstruction("y$3", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        innerLoopContent.addInstruction(new IterInstruction("j$2"));
        innerLoopContent.addInstruction(new MatrixSetInstruction("y$4", "y$3", Arrays.asList("i$2", "j$2"), "i$2"));
        body.addBlock(innerLoopContent);

        SsaBlock afterInnerLoop = new SsaBlock();
        afterInnerLoop.addInstruction(new PhiInstruction("y$5", Arrays.asList("y$2", "y$4"), Arrays.asList(1, 2)));
        body.addBlock(afterInnerLoop);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("y$ret", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        endBlock.addInstruction(new PhiInstruction("w$ret", Arrays.asList("y$1", "y$5"), Arrays.asList(0, 3)));
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("y$2", intMatrixType);
        types.put("y$3", intMatrixType);
        types.put("y$4", intMatrixType);
        types.put("y$5", intMatrixType);
        types.put("y$ret", intMatrixType);
        types.put("w$ret", intMatrixType);
        types.put("$start$1", int1Type);
        types.put("$interval$1", int1Type);
        types.put("m$1", intType);
        types.put("i$2", intType);
        body = applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.SIMPLE2D_EXTRA_USE.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testMultipleApplications() {
        TypedInstance instance = FunctionComposer.create(getDefaultFunctions(), editor -> {
            VariableType intType = editor.getNumerics().newInt();
            DynamicMatrixType intMatrixType = DynamicMatrixType.newInstance(intType);

            String nElems = editor.makeTemporary("nElems", intType);
            editor.addInstruction(new ArgumentInstruction(nElems, 0));
            String one = editor.addMakeIntegerInstruction("one", 1);
            String initialX = editor.addMakeUndefinedInstruction("x");
            String initialY = editor.addMakeUndefinedInstruction("y");
            String initialZ = editor.addMakeUndefinedInstruction("z");

            String outerLoopX = editor.makeTemporary("x", intMatrixType);
            String outerLoopEndX = editor.makeTemporary("x", intMatrixType);
            String innerLoopX = editor.makeTemporary("x", intMatrixType);
            String innerLoopEndX = editor.makeTemporary("x", intMatrixType);
            String endX = editor.makeTemporary("x", intMatrixType);

            String outerLoopY = editor.makeTemporary("y", intMatrixType);
            String outerLoopEndY = editor.makeTemporary("y", intMatrixType);
            String innerLoopY = editor.makeTemporary("y", intMatrixType);
            String innerLoopEndY = editor.makeTemporary("y", intMatrixType);
            String endY = editor.makeTemporary("y", intMatrixType);

            String outerLoopZ = editor.makeTemporary("z", intMatrixType);
            String outerLoopEndZ = editor.makeTemporary("z", intMatrixType);
            String innerLoopZ = editor.makeTemporary("z", intMatrixType);
            String innerLoopEndZ = editor.makeTemporary("z", intMatrixType);
            String endZ = editor.makeTemporary("z", intMatrixType);

            ForLoopBuilderResult outerLoop = editor.makeForLoop(one, one, nElems);
            BlockEditorHelper outerLoopEditor = outerLoop.getLoopBuilder();
            BlockEditorHelper outerEndEditor = outerLoop.getEndBuilder();

            outerLoopEditor.addInstruction(new PhiInstruction(outerLoopX, Arrays.asList(initialX, outerLoopEndX),
                    Arrays.asList(editor.getBlockId(), outerLoopEditor.getBlockId())));
            outerLoopEditor.addInstruction(new PhiInstruction(outerLoopY, Arrays.asList(initialY, outerLoopEndY),
                    Arrays.asList(editor.getBlockId(), outerLoopEditor.getBlockId())));
            outerLoopEditor.addInstruction(new PhiInstruction(outerLoopZ, Arrays.asList(initialZ, outerLoopEndZ),
                    Arrays.asList(editor.getBlockId(), outerLoopEditor.getBlockId())));
            String iter1 = outerLoopEditor.addIntItersInstruction("i");

            ForLoopBuilderResult innerLoop = outerLoopEditor.makeForLoop(one, one, nElems);
            BlockEditorHelper innerLoopEditor = innerLoop.getLoopBuilder();
            BlockEditorHelper innerEndEditor = innerLoop.getEndBuilder();

            innerLoopEditor.addInstruction(new PhiInstruction(innerLoopX, Arrays.asList(outerLoopX, innerLoopEndX),
                    Arrays.asList(outerLoopEditor.getBlockId(), innerLoopEditor.getBlockId())));
            innerLoopEditor.addInstruction(new PhiInstruction(innerLoopY, Arrays.asList(outerLoopY, innerLoopEndY),
                    Arrays.asList(outerLoopEditor.getBlockId(), innerLoopEditor.getBlockId())));
            innerLoopEditor.addInstruction(new PhiInstruction(innerLoopZ, Arrays.asList(outerLoopZ, innerLoopEndZ),
                    Arrays.asList(outerLoopEditor.getBlockId(), innerLoopEditor.getBlockId())));
            String iter2 = innerLoopEditor.addIntItersInstruction("j");

            innerLoopEditor
                    .addInstruction(
                            new MatrixSetInstruction(innerLoopEndX, innerLoopX, Arrays.asList(iter1, iter2), one));
            innerLoopEditor
                    .addInstruction(
                            new MatrixSetInstruction(innerLoopEndY, innerLoopY, Arrays.asList(iter1, iter2), one));
            innerLoopEditor
                    .addInstruction(
                            new MatrixSetInstruction(innerLoopEndZ, innerLoopZ, Arrays.asList(iter1, iter2), one));

            innerEndEditor.addInstruction(new PhiInstruction(outerLoopEndX, Arrays.asList(outerLoopX, innerLoopEndX),
                    Arrays.asList(outerLoopEditor.getBlockId(), innerLoopEditor.getBlockId())));
            innerEndEditor.addInstruction(new PhiInstruction(outerLoopEndY, Arrays.asList(outerLoopY, innerLoopEndY),
                    Arrays.asList(outerLoopEditor.getBlockId(), innerLoopEditor.getBlockId())));
            innerEndEditor.addInstruction(new PhiInstruction(outerLoopEndZ, Arrays.asList(outerLoopZ, innerLoopEndZ),
                    Arrays.asList(outerLoopEditor.getBlockId(), innerLoopEditor.getBlockId())));

            outerEndEditor.addInstruction(new PhiInstruction(endX, Arrays.asList(initialX, outerLoopEndX),
                    Arrays.asList(editor.getBlockId(), innerEndEditor.getBlockId())));
            outerEndEditor.addInstruction(new PhiInstruction(endY, Arrays.asList(initialY, outerLoopEndY),
                    Arrays.asList(editor.getBlockId(), innerEndEditor.getBlockId())));
            outerEndEditor.addInstruction(new PhiInstruction(endZ, Arrays.asList(initialZ, outerLoopEndZ),
                    Arrays.asList(editor.getBlockId(), innerEndEditor.getBlockId())));
        });

        applyPass(instance, getDefaultFunctions());

        String obtained = instance.getFunctionBody().toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(MatrixPreallocatorResource.MULTIPLE_ALLOCATIONS.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("MATISSE_reserve_capacity", MatissePrimitive.RESERVE_CAPACITY.getMatlabFunction());
        return functions;
    }

    private static FunctionBody applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        ProviderData providerData = ProviderData.newInstance("test-data");
        TypedInstance instance = new TypedInstance(new FunctionIdentification("test.m"),
                Collections.emptyList(),
                body,
                () -> "",
                providerData);
        for (String variableName : types.keySet()) {
            instance.addVariable(variableName, types.get(variableName));
        }

        body = applyPass(instance, functions);

        types.clear();
        types.putAll(instance.getVariableTypes());

        return body;
    }

    private static FunctionBody applyPass(TypedInstance instance,
            Map<String, InstanceProvider> functions) {

        DataStore passData = TestUtils.buildPassData(functions, instance);

        MatrixPreallocatorPass pass = new MatrixPreallocatorPass();
        pass.apply(instance, passData);

        return instance.getFunctionBody();
    }
}
