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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.PassAwareMatlabToCEngine;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matisselib.unssa.allocators.EfficientVariableAllocator;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.utilities.StringList;

public class InferredBytecodeCodePrinter implements CodePrinter {

    @Override
    public List<ContentPage> getCode(String code,
            DataStore setup,
            FileNode file,
            PrintStream reportStream,
            CodeGenerationSettings settings) {
        MatlabFunctionTable systemFunctions = getSystemFunctions();
        VariableAllocator allocator = getAllocator();

        FunctionNode function = file.getMainFunction();
        String functionName = function.getFunctionName();
        String fileName = functionName + ".m";

        ProjectPassCompilationManager manager = setupManager(reportStream, code, systemFunctions, allocator, fileName,
                settings.convertToCssa(), settings);

        buildInstance(function, functionName, fileName, manager, setup);

        if (settings.applyPasses()) {
            manager.applyPostTypeInferencePasses();
        }

        StringBuilder builder = new StringBuilder();

        for (TypedInstance instance : manager.getInferredInstances()) {
            builder.append(instance.toString());
            builder.append("\n\n");
        }

        return Arrays.asList(new ContentPage("IR", builder.toString(), getContentType()));
    }

    public static TypedInstance buildInstance(FunctionNode function,
            String functionName,
            String fileName,
            ProjectPassCompilationManager manager,
            DataStore baseSetup) {

        TypesMap types = baseSetup.get(MatisseKeys.TYPE_DEFINITION);

        // No inputs for now
        List<VariableType> inputTypes = new ArrayList<>();

        for (String inputName : function.getDeclarationNode().getInputs().getNames()) {
            VariableType variableType = types.getSymbol(functionName, inputName);
            if (variableType == null) {
                System.err.println(types);
                throw new RuntimeException("Missing type for input " + inputName);
            }

            types.addSymbol(inputName, variableType);
            inputTypes.add(variableType);
        }
        manager.setDefaultTypes(types);
        manager.applyTypeInference(baseSetup);

        ProviderData providerData = ProviderData.newInstance(baseSetup)
                .create(inputTypes);
        providerData.setNargouts(function.getDeclarationNode().getOutputs().getNumChildren());

        TypedInstance instance = manager
                .inferFunction(new FunctionIdentification(fileName, functionName), providerData);
        return instance;
    }

    public static ProjectPassCompilationManager setupManager(PrintStream reportStream,
            String code,
            MatlabFunctionTable systemFunctions,
            VariableAllocator allocator,
            String fileName,
            boolean convertToCssa,
            CodeGenerationSettings settings) {

        Map<String, StringProvider> files = new HashMap<>();
        files.put(fileName, StringProvider.newInstance(code));

        DataStore additionalServices = new SimpleDataStore("processor-printer-manager");
        additionalServices.add(MatisseLibOption.DUMP_SSA_INSTRUCTIONS, settings.dumpSsa());
        additionalServices.add(MatisseLibOption.DUMP_OUTPUT_TYPES, settings.dumpOutputTypes());
        additionalServices.add(MatisseLibOption.PASSES_TO_LOG, new StringList(settings.getPassesToLog()));

        ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(DefaultRecipes.getTestPreTypeInferenceRecipe())
                        .withPostTypeInferenceRecipe(
                                DefaultRecipes.getTestPostTypeInferenceRecipe(convertToCssa))
                        .withAvailableFiles(files)
                        .withSystemFunctions(systemFunctions)
                        .withLanguageMode(settings.getLanguageMode())
                        .withZ3Enabled(settings.enableZ3())
                        .withAdditionalServices(DataView.newInstance(additionalServices)));
        manager.setErrorReportStream(reportStream);

        PassAwareMatlabToCEngine engine = new PassAwareMatlabToCEngine(manager,
                systemFunctions,
                allocator,
                SsaToCBuilder.DEFAULT_SSA_TO_C_RULES);
        MFileProvider.setEngine(engine);

        manager.applyPreTypeInferencePasses(fileName);
        return manager;
    }

    public static VariableAllocator getAllocator() {
        VariableAllocator allocator = new EfficientVariableAllocator();
        return allocator;
    }

    public static MatlabFunctionTable getSystemFunctions() {
        MatlabFunctionTable systemFunctions = MatlabToCUtils.buildMatissePrototypeTable();
        return systemFunctions;
    }

    public String getContentType() {
        return "application/x-matisse-bytecode";
    }
}
