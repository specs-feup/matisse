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

package org.specs.matisselib.tests.pass.elimination.redundantoutput;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.passes.posttype.RedundantOutputEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class RedundantOutputTests extends TestSkeleton {

    @Test
    public void testSimple() {

        VariableType doubleType = getNumerics().newDouble();

        ProviderData providerData = ProviderData.newInstance("test-rot-simple");

        FunctionBody functionBody = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("B$1", 1));
        block.addInstruction(AssignmentInstruction.fromVariable("A$ret", "A$1"));
        functionBody.addBlock(block);

        TypedInstance instance = new TypedInstance(new FunctionIdentification("simple.m"),
                Arrays.asList("A", "B"),
                functionBody,
                StringProvider.newInstance("dummy"),
                providerData);
        instance.addVariable("A$ret", doubleType);

        FunctionType functionType = FunctionTypeBuilder.newSimple()
                .addInput("A", doubleType)
                .addInput("B", doubleType)
                .returningVoid()
                .build();
        instance.setFunctionType(functionType);

        applyPass(instance);

        RedundantOutputResource resource = RedundantOutputResource.SIMPLE;
        String obtained = instance.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void applyPass(TypedInstance instance) {
        DataStore passData = new CommonPassData("test-rot");
        new RedundantOutputEliminationPass().apply(instance, passData);
    }
}
