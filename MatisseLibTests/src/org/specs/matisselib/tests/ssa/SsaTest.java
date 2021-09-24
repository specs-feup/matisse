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

package org.specs.matisselib.tests.ssa;

import java.util.Optional;

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
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.widescope.MockWideScopeService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBuilder;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class SsaTest {

    @Test
    public void testSimple() {
        runTest(SsaResource.SIMPLE);
    }

    @Test
    public void testBranch() {
        runTest(SsaResource.BRANCH);
    }

    @Test
    public void testFor() {
        runTest(SsaResource.FOR);
    }

    @Test
    public void testLogicalAnd() {
        runTest(SsaResource.LOGICAL_AND);
    }

    @Test
    public void testLogicalOr() {
        runTest(SsaResource.LOGICAL_OR);
    }

    @Test
    public void testUseIn() {
        runTest(SsaResource.USE_IN);
    }

    @Test
    public void testWhile() {
        runTest(SsaResource.XWHILE);
    }

    @Test
    public void testMultiEnd() {
        runTest(SsaResource.MULTI_END);
    }

    @Test
    public void testCellEnd() {
        runTest(SsaResource.CELL_END);
    }

    @Test
    public void testWhileBreak() {
        runTest(SsaResource.WHILE_BREAK);
    }

    @Test
    public void testForBreak() {
        runTest(SsaResource.FOR_BREAK);
    }

    @Test
    public void testForBreak2() {
        runTest(SsaResource.FOR_BREAK2);
    }

    @Test
    public void testGlobals() {
        runTest(SsaResource.GLOBALS);
    }

    @Test
    public void testGlobalsGet() {
        runTest(SsaResource.GLOBALS_GET);
    }

    @Test
    public void testGlobalsSet() {
        runTest(SsaResource.GLOBALS_SET);
    }

    @Test
    public void testDeletion() {
        runTest(SsaResource.DELETION);
    }

    @Test
    public void testDeletion2() {
        runTest(SsaResource.DELETION2);
    }

    @Test
    public void testInputNames() {
        FileNode file = new MatlabParser().parse("function f(a, ~, c), end");
        FunctionBody body = buildSsaFromFile(file);

        Assert.assertEquals(3, body.getNumInputs());
        Assert.assertEquals(Optional.of("a"), body.getInputName(0));
        Assert.assertEquals(Optional.empty(), body.getInputName(1));
        Assert.assertEquals(Optional.of("c"), body.getInputName(2));
    }

    @Test
    public void testOutputNames() {
        FileNode file = new MatlabParser().parse("function [a, b, c] = f(), a = 1; b = 2; c = 3; end");
        FunctionBody body = buildSsaFromFile(file);

        Assert.assertEquals(3, body.getNumOutputs());
        Assert.assertEquals(Optional.of("a"), body.getOutputName(0));
        Assert.assertEquals(Optional.of("b"), body.getOutputName(1));
        Assert.assertEquals(Optional.of("c"), body.getOutputName(2));
    }

    private static void runTest(SsaResource resource) {
        FileNode file = new MatlabParser().parse(resource);
        FunctionBody body = buildSsaFromFile(file);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
                TestUtils.normalize(body.toString()));
    }

    private static FunctionBody buildSsaFromFile(FileNode file) {
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
        return body;
    }
}
