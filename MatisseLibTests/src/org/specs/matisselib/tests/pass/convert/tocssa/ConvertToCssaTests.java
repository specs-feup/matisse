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

package org.specs.matisselib.tests.pass.convert.tocssa;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
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
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.widescope.MockWideScopeService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaBuilder;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;

import pt.up.fe.specs.util.SpecsIo;

public class ConvertToCssaTests extends TestSkeleton {
    @Test
    public void testSimple() {
        runTest(ConvertToCssaResource.SIMPLE);
    }

    @Test
    public void testBranch() {
        runTest(ConvertToCssaResource.BRANCH);
    }

    @Test
    public void testWhile() {
        runTest(ConvertToCssaResource.XWHILE);
    }

    @Test
    public void testFor() {
        runTest(ConvertToCssaResource.XFOR);
    }

    private static void runTest(ConvertToCssaResource resource) {
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
        new DeadCodeEliminationPass().apply(body, passData);
        new ConvertToCssaPass().apply(body, passData);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testType() {
        CommonPassData passData = new CommonPassData("simple-setup");

        FunctionIdentification functionIdentification = new FunctionIdentification("foo.m");
        FunctionBody functionBody = new FunctionBody();
        ProviderData providerData = ProviderData.newInstance("foo");

        SsaBlock topBlock = new SsaBlock();
        topBlock.addAssignment("A$1", 10);
        topBlock.addInstruction(new BranchInstruction("A$1", 1, 2, 3));
        functionBody.addBlock(topBlock);

        SsaBlock trueBlock = new SsaBlock();
        trueBlock.addAssignment("A$2", 20);
        functionBody.addBlock(trueBlock);

        SsaBlock falseBlock = new SsaBlock();
        functionBody.addBlock(falseBlock);

        SsaBlock endBlock = new SsaBlock();
        endBlock.addInstruction(new PhiInstruction("A$3", Arrays.asList("A$1", "A$2"), Arrays.asList(1, 2)));
        functionBody.addBlock(endBlock);

        TypedInstance instance = new TypedInstance(functionIdentification, Collections.emptyList(), functionBody,
                () -> null,
                providerData);
        instance.addVariable("A$1", getNumerics().newInt(10));
        instance.addVariable("A$2", getNumerics().newInt(20));
        instance.addVariable("A$3", getNumerics().newDouble());

        new ConvertToCssaPass().apply(instance, passData);

        Assert.assertEquals("DOUBLE", instance.getVariableType("$A$1").get().toString());
        Assert.assertEquals("DOUBLE", instance.getVariableType("$A$2").get().toString());
        Assert.assertEquals("DOUBLE", instance.getVariableType("$A$3").get().toString());
        Assert.assertFalse(instance.getVariableType("$A$4").isPresent());
    }
}
