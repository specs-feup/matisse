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

package org.specs.matisselib.tests.matlabinference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.matlabinference.MatlabTypesPass;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.SsaRecipeBuilder;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.providers.StringProvider;

public class MatlabInferenceWip extends TestSkeleton {

    @Test
    @Ignore
    public void test() {
        ResourceProvider resource = MatlabInferenceResource.SIMPLESUM;
        // FileNode file = new MatlabParser().parse(resource);

        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put(resource.getResourceName(), StringProvider.newInstance(SpecsIo.getResource(resource)));

        MatlabFunctionTable systemFunctions = MatlabToCUtils.buildMatissePrototypeTable();

        SsaRecipeBuilder ssaRecipeBuilder = new SsaRecipeBuilder();
        for (SsaPass pass : DefaultRecipes.getTestPreTypeInferenceRecipe().getPasses()) {
            ssaRecipeBuilder.addPass(pass);
        }

        ssaRecipeBuilder.addPass(new MatlabTypesPass());
        SsaRecipe ssaRecipe = ssaRecipeBuilder.getRecipe();

        DataStore setup = DataStore.newInstance("foo");
        TypesMap types = new TypesMap();
        types.addSymbol(Arrays.asList("b"), getNumerics().newDouble());

        try (ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(ssaRecipe)
                        .withPostTypeInferenceRecipe(
                                PostTypeInferenceRecipe.fromSinglePass(new DeadCodeEliminationPass()))
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(systemFunctions)
                        .withDefaultTypes(types))) {

            manager.applyPreTypeInferencePasses(resource.getResourceName());
            TypedInstance root = manager.applyTypeInference(setup);
            manager.applyPostTypeInferencePasses();

            String body = root.toString();
            System.out.println("BODY:" + body);
            /*
            	FunctionBody functionBody = SsaBuilder.buildSsa(file, passData);
            
            	// TODO: Get a better way to get the stream
            	StringProvider originalCode = ((FileNode) file.getRoot()).getOriginalCode();
            	*/
            /*
            passData.set(INSTRUCTION_REPORT_SERVICE, new CommonInstructionReportingService(System.err,
            	functionIdentification.getFile(),
            	originalCode,
            	functionBody));
            */
            /*
            for (SsaPass pass : ssaRecipe.getPasses()) {
            passData.get(MatisseInit.PASS_LOG).append(
            	    "Applying pre-type-inference SSA pass " + pass.getName() + " to " + functionName);
            pass.apply(functionBody, passData);
            }
            */

        }
    }

}
