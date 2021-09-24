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

package org.specs.matisselib.tests.pass.basicaccesssimplifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.ast.OperatorReplacementPass;
import org.specs.matisselib.passes.posttype.BasicAccessSimplifierPass;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.LineInformationEliminationPass;
import org.specs.matisselib.passes.ssa.RedundantAssignmentEliminationPass;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.scalarbuilderinfo.SimpleScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaBuilder;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class BasicAccessSimplifierTests extends TestSkeleton {
    @Test
    public void testSimple() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("$numel"), Arrays.asList("A$1")));
        block.addAssignment("$one", 1);
        block.addInstruction(new ForInstruction("$one", "$one", "$numel", 1, 2));
        body.addBlock(block);

        SsaBlock loop = new SsaBlock();
        loop.addInstruction(new PhiInstruction("A$2", Arrays.asList("A$1", "A$3"), Arrays.asList(0, 1)));
        loop.addInstruction(new IterInstruction("$iter"));
        loop.addInstruction(new MatrixSetInstruction("A$3", "A$2", Arrays.asList("$iter"), "$one"));
        body.addBlock(loop);

        SsaBlock loopEnd = new SsaBlock();
        loopEnd.addInstruction(new PhiInstruction("A$ret", Arrays.asList("A$1", "A$3"), Arrays.asList(0, 1)));
        body.addBlock(loopEnd);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$2", intMatrixType);
        types.put("A$3", intMatrixType);
        types.put("A$ret", intMatrixType);
        types.put("$numel", intType);
        types.put("$iter", intType);
        types.put("$one", int1Type);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(BasicAccessSimplifierResource.SIMPLE_TXT.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testMat2D() {

        BasicAccessSimplifierResource resource = BasicAccessSimplifierResource.MAT2D_M;

        DataStore passData = new CommonPassData("test-mat2d");
        passData.add(PassManager.NODE_REPORTING,
                new NodeReportService(System.err, "test.m", StringProvider.newInstance(resource)));
        passData.add(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER,
                new SimpleScalarValueInformationBuilderService());
        passData.add(PassManager.DIRECTIVE_PARSER, new DirectiveParser());

        FileNode file = new MatlabParser().parse(resource);

        new OperatorReplacementPass().apply(file, passData);

        FunctionNode function = file.getMainFunction();
        FunctionBody body = SsaBuilder.buildSsa(function, passData);

        Map<String, VariableType> types = new HashMap<>();
        types.put("$zeros_arg1$1", getNumerics().newInt());
        types.put("$zeros_arg2$1", getNumerics().newInt());
        types.put("$start$1", getNumerics().newInt(1));
        types.put("$start$2", getNumerics().newInt(1));
        types.put("$interval$1", getNumerics().newInt(1));
        types.put("$interval$2", getNumerics().newInt(1));
        types.put("a$2+3+4+5+6", getNumerics().newInt());
        types.put("b$2+3+4+5+6", getNumerics().newInt());
        types.put("y$2", DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        types.put("y$3", DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        types.put("y$4", DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        types.put("y$5", DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));
        types.put("y$6", DynamicMatrixType.newInstance(getNumerics().newInt(), TypeShape.newDimsShape(2)));

        new LineInformationEliminationPass().apply(body, passData);
        new RedundantAssignmentEliminationPass(false).apply(body, passData);
        new DeadCodeEliminationPass().apply(body, passData);
        TestUtils.testTypeTransparentPass(new BasicAccessSimplifierPass(), body, types, getDefaultFunctions());

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(BasicAccessSimplifierResource.MAT2D_TXT.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testInIf() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType int10Type = getNumerics().newInt(10);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(
                new UntypedFunctionCallInstruction("numel", Arrays.asList("$numel"), Arrays.asList("A$1")));
        block.addAssignment("$one", 1);
        block.addInstruction(new ForInstruction("$one", "$one", "$numel", 1, 2));
        body.addBlock(block);

        SsaBlock loop = new SsaBlock();
        loop.addInstruction(new PhiInstruction("A$2", Arrays.asList("A$1", "A$5"), Arrays.asList(0, 4)));
        loop.addInstruction(new IterInstruction("$iter"));
        loop.addAssignment("$ten", 10);
        loop.addInstruction(new UntypedFunctionCallInstruction("gt", Arrays.asList("$gt"), Arrays.asList("$iter",
                "$ten")));
        loop.addInstruction(new BranchInstruction("$gt", 2, 3, 4));
        body.addBlock(loop);

        SsaBlock loopIf = new SsaBlock();
        loopIf.addInstruction(new MatrixSetInstruction("A$3", "A$2", Arrays.asList("$iter"), "$one"));
        body.addBlock(loopIf);

        SsaBlock loopElse = new SsaBlock();
        loopElse.addInstruction(new MatrixSetInstruction("A$4", "A$2", Arrays.asList("$iter"), "$ten"));
        body.addBlock(loopElse);

        SsaBlock branchEnd = new SsaBlock();
        branchEnd.addInstruction(new PhiInstruction("A$5", Arrays.asList("A$3", "A$4"), Arrays.asList(2, 3)));
        body.addBlock(branchEnd);

        SsaBlock loopEnd = new SsaBlock();
        loopEnd.addInstruction(new PhiInstruction("A$ret", Arrays.asList("A$1", "A$5"), Arrays.asList(0, 4)));
        body.addBlock(loopEnd);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("A$2", intMatrixType);
        types.put("A$3", intMatrixType);
        types.put("A$4", intMatrixType);
        types.put("A$5", intMatrixType);
        types.put("A$ret", intMatrixType);
        types.put("$numel", intType);
        types.put("$gt", intType);
        types.put("$iter", intType);
        types.put("$one", int1Type);
        types.put("$ten", int10Type);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(BasicAccessSimplifierResource.INIF_TXT.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("ndims", MatlabBuiltin.NDIMS.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());
        functions.put("gt", MatlabOp.GreaterThan.getMatlabFunction());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new BasicAccessSimplifierPass(), body, types, functions);
    }
}
