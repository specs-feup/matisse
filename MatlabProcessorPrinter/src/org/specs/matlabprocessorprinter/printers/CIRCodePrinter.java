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

package org.specs.matlabprocessorprinter.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CProject;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Passes.CRecipe;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.jOptions.IvdepType;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class CIRCodePrinter implements CodePrinter {

    @Override
    public List<ContentPage> getCode(String code, DataStore setup, FileNode file, PrintStream reportStream,
            CodeGenerationSettings settings) throws IOException {
        MatlabFunctionTable systemFunctions = InferredBytecodeCodePrinter.getSystemFunctions();
        VariableAllocator allocator = InferredBytecodeCodePrinter.getAllocator();

        FunctionNode function = file.getMainFunction();
        String functionName = function.getFunctionName();
        String fileName = functionName + ".m";

        ProjectPassCompilationManager manager = InferredBytecodeCodePrinter.setupManager(reportStream,
                code, systemFunctions,
                allocator, fileName, true, settings);

        TypedInstance instance = InferredBytecodeCodePrinter.buildInstance(function, functionName, fileName, manager,
                setup);

        manager.applyPostTypeInferencePasses();

        CProject cProject = new CProject(settings.applyPasses() ? DefaultRecipes.DefaultCRecipe : CRecipe.empty(),
                MatlabToCOptionUtils.newDefaultSettings());

        FunctionInstance mainInstance = SsaToCBuilder
                .buildImplementation(manager, instance, systemFunctions, allocator,
                        SsaToCBuilder.DEFAULT_SSA_TO_C_RULES);
        cProject.addFunction(mainInstance);

        StringBuilder resultBuilder = new StringBuilder();

        for (FunctionInstance cFunction : cProject.getUserFunctions().values()) {
            if (cFunction instanceof InstructionsInstance) {
                InstructionsInstance instructionsInstance = (InstructionsInstance) cFunction;

                resultBuilder.append("Function ");
                resultBuilder.append(cFunction.getCName());
                resultBuilder.append(":\n");
                resultBuilder.append(instructionsInstance.getInstructions().toStringTree());
                resultBuilder.append("\n");
            }
        }

        String result = resultBuilder.toString();

        return Arrays.asList(new ContentPage("CIR code", result, getContentType()));
    }

    public String getContentType() {
        return "text/java";
    }

    @Override
    public void processSetup(DataStore basicSetup, CodeGenerationSettings settings) {
        basicSetup.set(MatlabToCKeys.IVDEP_TYPE, IvdepType.NONE);
        basicSetup.set(MatlabToCKeys.USE_PASS_SYSTEM, true);
        basicSetup.set(MatlabToCKeys.ENABLE_Z3, settings.enableZ3());
        basicSetup.set(MatisseLibOption.DUMP_SSA_INSTRUCTIONS, settings.dumpSsa());
        basicSetup.set(MatisseLibOption.DUMP_OUTPUT_TYPES, settings.dumpOutputTypes());
    }
}
