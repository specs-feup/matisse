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

package org.specs.matisselib.tests.unssa.lifetime;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passes.ssa.ConvertToCssaPass;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.LineInformationEliminationPass;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.widescope.MockWideScopeService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaBuilder;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.ReadGlobalInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.WriteGlobalInstruction;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.unssa.LifetimeAnalyzer;
import org.specs.matisselib.unssa.LifetimeInformation;

import pt.up.fe.specs.util.SpecsIo;

public class LifetimeAnalysisTests {
    @Test
    public void testSimple() {
        runTest(LifetimeAnalysisResource.SIMPLE);
    }

    @Test
    public void testBranch() {
        runTest(LifetimeAnalysisResource.BRANCH);
    }

    @Test
    public void testEntryInterferentOutputs() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        block.addAssignment("A$1", 0);
        block.addInstruction(new UntypedFunctionCallInstruction("mtimes", Arrays.asList("A$2"), Arrays.asList("A$1")));

        LifetimeInformation lifetimes = LifetimeAnalyzer.analyze(body);
        Assert.assertTrue(lifetimes.isLiveAtExit("A$1", 0, 1));
    }

    @Test
    public void testByRefInterferences() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        VariableType matrixType = DynamicMatrixType.newInstance(NumericFactory.defaultFactory().newInt());

        FunctionType fType = FunctionTypeBuilder.newWithOutputsAsInputs()
                .addReferenceInput("a", matrixType)
                .addReferenceInput("b", matrixType)
                .addInput("c", matrixType)
                .addOutputAsInput("out1", matrixType)
                .addOutputAsInput("out2", matrixType)
                .build();

        block.addAssignment("A$1", 0);
        block.addAssignment("A$2", 0);
        block.addInstruction(
                new TypedFunctionCallInstruction("foo", fType, Arrays.asList("A$3", "A$4"),
                        Arrays.asList("A$1", "A$2", "A$2")));

        LifetimeInformation lifetimes = LifetimeAnalyzer.analyze(body);
        Assert.assertFalse(lifetimes.isLiveAtExit("A$1", 0, 2));
        Assert.assertTrue(lifetimes.isLiveAtExit("A$2", 0, 2));
    }

    @Test
    public void testForInterferences() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);
        SsaBlock xfor = new SsaBlock();
        body.addBlock(xfor);
        SsaBlock xif = new SsaBlock();
        body.addBlock(xif);
        SsaBlock xelse = new SsaBlock();
        body.addBlock(xelse);
        SsaBlock afterIf = new SsaBlock();
        body.addBlock(afterIf);
        SsaBlock afterFor = new SsaBlock();
        body.addBlock(afterFor);

        block.addInstruction(new ArgumentInstruction("start$1", 0));
        block.addInstruction(new ArgumentInstruction("interval$1", 1));
        block.addInstruction(new ArgumentInstruction("end$1", 2));
        block.addInstruction(new ForInstruction("start$1", "interval$1", "end$1", 1, 5));
        xfor.addInstruction(new IterInstruction("$iter$1"));
        xfor.addInstruction(new BranchInstruction("start$1", 2, 3, 4));
        xelse.addAssignment("$hello", 1);

        LifetimeInformation lifetimes = LifetimeAnalyzer.analyze(body);
        Assert.assertTrue(lifetimes.isLiveAtExit("$iter$1", 3, 0));
    }

    @Test
    public void testLifetimeForReturn() {
        FunctionBody body = new FunctionBody();
        SsaBlock entryBlock = new SsaBlock();
        entryBlock.addInstruction(new ArgumentInstruction("a$ret", 0));
        entryBlock.addInstruction(new BranchInstruction("a$ret", 1, 2, 3));
        body.addBlock(entryBlock);

        body.addBlock(new SsaBlock());
        body.addBlock(new SsaBlock());
        SsaBlock lastBlock = new SsaBlock();
        lastBlock.addComment("Test");
        body.addBlock(lastBlock);

        LifetimeInformation lifetimes = LifetimeAnalyzer.analyze(body);
        Assert.assertTrue(lifetimes.isLiveAtExit("a$ret", 3, 0));
    }

    @Test
    public void testGlobals() {
        FunctionBody body = new FunctionBody();
        SsaBlock entryBlock = new SsaBlock();
        entryBlock.addInstruction(new ArgumentInstruction("a$1", 0));
        entryBlock.addInstruction(new ReadGlobalInstruction("b$1", "^b"));
        entryBlock.addInstruction(new ReadGlobalInstruction("a$2", "^a"));
        entryBlock.addInstruction(new EndInstruction("$foo$1", "a$1", 0, 1));
        entryBlock.addInstruction(new WriteGlobalInstruction("^a", "a$2"));
        entryBlock.addInstruction(new EndInstruction("$foo$2", "a$1", 0, 1));
        body.addBlock(entryBlock);

        LifetimeInformation lifetimes = LifetimeAnalyzer.analyze(body);
        Assert.assertTrue(lifetimes.isLiveAtEntry("^a", 0, 2));
        Assert.assertFalse(lifetimes.isLiveAtEntry("^a", 0, 4));
        Assert.assertTrue(lifetimes.isLiveAtExit("^a", 0, 4));
        Assert.assertTrue(lifetimes.isLiveAtExit("^a", 0, 5));
    }

    private static void runTest(LifetimeAnalysisResource resource) {
        FileNode file = new MatlabParser().parse(resource);
        FunctionNode function = file.getMainFunction();
        String functionName = function.getFunctionName();
        MatlabNode node = function;

        CommonPassData passData = new CommonPassData("simple-setup");
        passData.add(PassManager.NODE_REPORTING,
                new NodeReportService(System.err, resource.getResourceName(), () -> null));
        WideScopeService projectWideScope = new MockWideScopeService(file);
        WideScopeService wideScope = projectWideScope
                .withFunctionIdentification(projectWideScope
                        .getUserFunction(
                                new FunctionIdentification(file.getFilename() + ".m", functionName),
                                functionName)
                        .get());
        passData.add(PreTypeInferenceServices.COMMON_NAMING, new CommonNamingService(wideScope, node));
        passData.add(PassManager.DIRECTIVE_PARSER, new DirectiveParser());

        for (MatlabNodePass pass : DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe.getPasses()) {
            node = pass.apply(node, passData);
        }

        FunctionBody body = SsaBuilder.buildSsa((FunctionNode) node, passData);
        new LineInformationEliminationPass().apply(body, passData);
        new DeadCodeEliminationPass().apply(body, passData);
        new ConvertToCssaPass().apply(body, passData);

        LifetimeInformation info = LifetimeAnalyzer.analyze(body);

        String expected = TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource()));
        for (String line : expected.split("\n")) {
            line = line.trim();

            if (line.startsWith("%") || line.isEmpty()) {
                continue;
            }

            String[] components = line.split(" ");

            String variableName = components[0];
            String type = components[1];
            int blockId = Integer.parseInt(components[2]);
            int instructionId = Integer.parseInt(components[3]);
            String value = components[4];

            boolean actual;

            if (type.equals("entry")) {
                actual = info.isLiveAtEntry(variableName, blockId, instructionId);
            } else if (type.equals("exit")) {
                actual = info.isLiveAtExit(variableName, blockId, instructionId);
            } else {
                throw new UnsupportedOperationException();
            }

            if (value.equals("true")) {
                Assert.assertTrue(line, actual);
            } else if (value.equals("false")) {
                Assert.assertFalse(line, actual);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
