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

package org.specs.matisselib.tests.pass.branch.empty;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.EmptyBranchEliminationPass;
import org.specs.matisselib.passes.ssa.LineInformationEliminationPass;
import org.specs.matisselib.passes.ssa.RedundantAssignmentEliminationPass;
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
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class EmptyBranchEliminationTests {
    @Test
    public void testSimple() {
        runTest(EmptyBranchEliminationResource.SIMPLE);
    }

    @Test
    public void testInWhile() {
        runTest(EmptyBranchEliminationResource.INWHILE);
    }

    @Test
    public void testSwap() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("arg$1", 0));
        block.addInstruction(new ArgumentInstruction("arg$2", 1));
        block.addInstruction(new BranchInstruction("arg$1", 1, 2, 3));
        body.addBlock(block);
        body.addBlock(new SsaBlock());
        body.addBlock(new SsaBlock());
        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("$result", Arrays.asList("arg$1", "arg$2"), Arrays.asList(1, 2)));
        body.addBlock(endBlock);

        new EmptyBranchEliminationPass().apply(body, new CommonPassData("data"));

        // Can't delete block because it is still needed for phi.
        Assert.assertEquals(4, body.getBlocks().size());
    }

    private static void runTest(EmptyBranchEliminationResource resource) {
        FileNode file = new MatlabParser().parse(resource);
        FunctionNode function = file.getMainFunction();
        String functionName = function.getFunctionName();
        MatlabNode node = function;

        CommonPassData passData = new CommonPassData("simple-setup");
        passData.add(PassManager.NODE_REPORTING,
                new NodeReportService(System.err, "file.m", () -> null));
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
        new DeadCodeEliminationPass().apply(body, passData);
        new RedundantAssignmentEliminationPass(false).apply(body, passData);
        new EmptyBranchEliminationPass().apply(body, passData);
        new LineInformationEliminationPass().apply(body, passData);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
                TestUtils.normalize(body.toString()));
    }
}
