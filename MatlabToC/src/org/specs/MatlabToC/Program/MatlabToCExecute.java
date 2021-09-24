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

package org.specs.MatlabToC.Program;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.CFile;
import org.specs.CIR.CProject;
import org.specs.CIR.CirKeys;
import org.specs.CIR.CirUtils;
import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.GlobalVariableInstance;
import org.specs.CIR.Passes.CRecipe;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.JMatIOPlus.MatFile;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.DataUtils;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.CCode.CWriter;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRuleList;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.PassAwareMatlabToCEngine;
import org.specs.MatlabToC.MFunctions.MFunctionPrototype;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.MatlabToC.VariableStorage.CPersistenceUtils;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.io.PostTypeInferenceRecipeReader;
import org.specs.matisselib.io.PostTypeInferenceRecipeWriter;
import org.specs.matisselib.io.PreTypeInferenceSsaRecipeReader;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.typeinference.InferenceRuleList;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.ScriptInferenceResult;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocator;
import org.suikasoft.CMainFunction.Builder.TestMainInstance;
import org.suikasoft.CMainFunction.Builder.TestMainOptions;
import org.suikasoft.CMainFunction.Builder.Coder.CoderMainInstance;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import com.jmatio.types.MLArray;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.utilities.StringList;

public class MatlabToCExecute {

    public static final String MAIN_FUNCTION_FILE = "main_test";

    private final MatlabToCData data;
    private final TypesMap varTypeDefinition;
    private List<String> testFunctionInputs;

    public MatlabToCExecute(MatlabToCData data) {
        this(data, new TypesMap());
    }

    public MatlabToCExecute(MatlabToCData data, TypesMap varTypeDefinition) {
        this.data = data;
        this.varTypeDefinition = varTypeDefinition;
        testFunctionInputs = null;
    }

    public List<String> getTestFunctionInputs() {
        return testFunctionInputs;
    }

    public int execute() {

        // Prepare data

        MatlabRecipe preTypeInferenceRecipe = DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe;

        SsaRecipe baseSsaRecipe;
        if (data.customPreTypeSsaRecipeFile.isFile()) {
            try {
                List<String> recipePaths = new ArrayList<>();
                recipePaths.addAll(PreTypeInferenceSsaRecipeReader.DEFAULT_PASS_PACKAGES);
                StringList customRecipePaths = data.getSettings()
                        .get(MatlabToCKeys.CUSTOM_PRE_TYPE_SSA_RECIPE_PATHS);
                customRecipePaths.forEach(recipePaths::add);

                baseSsaRecipe = PreTypeInferenceSsaRecipeReader.read(data.customPreTypeSsaRecipeFile,
                        recipePaths);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            baseSsaRecipe = SsaRecipe.empty();
        }
        SsaRecipe ssaRecipe = SsaRecipe.combine(baseSsaRecipe,
                DefaultRecipes.getDefaultPreTypeInferenceRecipe(data.getSettings()));

        PostTypeInferenceRecipe baseRecipe;
        if (data.customRecipeFile.isFile()) {
            try {
                List<String> recipePaths = new ArrayList<>();
                recipePaths.addAll(PostTypeInferenceRecipeReader.DEFAULT_PASS_PACKAGES);
                StringList customRecipePaths = data.getSettings()
                        .get(MatlabToCKeys.CUSTOM_POST_TYPE_RECIPE_PATHS);
                customRecipePaths.forEach(recipePaths::add);

                baseRecipe = PostTypeInferenceRecipeReader.read(data.customRecipeFile, recipePaths);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            baseRecipe = DefaultRecipes.getOptimizingBasePostTypeInferenceRecipe();
        }
        PostTypeInferenceRecipe postTypeInferenceRecipe = PostTypeInferenceRecipe.combine(baseRecipe,
                DefaultRecipes.getFinalPasses(true));

        CRecipe cRecipe = DefaultRecipes.DefaultCRecipe;

        SsaToCRuleList baseSsaToCRules = SsaToCBuilder.DEFAULT_SSA_TO_C_RULES;
        SsaToCRuleList additionalSsaToCRules = data.getSettings()
                .get(MatlabToCKeys.ADDITIONAL_SSA_TO_C_RULES);

        SsaToCRuleList ssaToCRules = new SsaToCRuleList(baseSsaToCRules, additionalSsaToCRules);

        ImplementationData implementationData = MatlabToCUtils.newImplementationData(data.languageMode,
                varTypeDefinition,
                data.getSettings());
        MatlabToCOldExecute.addCustomBuilders(implementationData, data.getSettings());
        MatlabFunctionTable systemFunctions = implementationData.getBuiltInPrototypes();

        Map<String, StringProvider> availableFiles = new HashMap<>();
        for (File file : data.matlabFiles) {
            implementationData.getProjectMFiles().addUserFile(file);
            availableFiles.put(file.getName(), StringProvider.newInstance(file));
        }

        try {
            File recipeFile = new File(data.outputFolder, "used-recipe.recipe");

            PostTypeInferenceRecipeWriter.write(baseRecipe, recipeFile);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        InferenceRuleList typeInferenceRules = data.getSettings().get(MatlabToCKeys.TYPE_INFERENCE_RULES);
        DataView additionalServices = data.getSettings().get(MatlabToCKeys.ADDITIONAL_SERVICES);
        DataStore newSettings = new SimpleDataStore("additional-services");
        newSettings.addAll(additionalServices);
        if (data.getSettings().get(MatisseLibOption.SUPPRESS_PRINTING)) {
            newSettings.add(MatisseLibOption.SUPPRESS_PRINTING, true);
        }
        additionalServices = DataView.newInstance(newSettings);
        boolean enableZ3 = data.getSettings().get(MatlabToCKeys.ENABLE_Z3);

        ProjectPassCompilationOptions options = new ProjectPassCompilationOptions()
                .withPreTypeInferenceRecipe(preTypeInferenceRecipe)
                .withSsaRecipe(ssaRecipe)
                .withPostTypeInferenceRecipe(postTypeInferenceRecipe)
                .withInferenceRuleList(typeInferenceRules)
                .withAvailableFiles(availableFiles)
                .withSystemFunctions(systemFunctions)
                .withAdditionalServices(additionalServices)
                .withLanguageMode(data.languageMode)
                .withZ3Enabled(enableZ3);

        try (ProjectPassCompilationManager manager = new ProjectPassCompilationManager(options)) {

            VariableAllocator variableAllocator = data.getSettings().get(MatlabToCKeys.CUSTOM_VARIABLE_ALLOCATOR);
            PassAwareMatlabToCEngine engine = new PassAwareMatlabToCEngine(manager, systemFunctions, variableAllocator,
                    ssaToCRules);
            MFileProvider.setEngine(engine);

            if (data.topLevelMFiles.equals(Arrays.asList("*"))) {
                throw new NotImplementedException("Wildcard top level m files");
            }

            if (data.functionToPutMain.isEmpty()) {
                if (data.topLevelMFiles.size() != 1) {
                    throw new NotImplementedException(data.topLevelMFiles.size() + " top level M files");
                }

                manager.applyPreTypeInferencePasses(data.topLevelMFiles.get(0) + ".m");
            } else {
                manager.applyPreTypeInferencePasses(data.functionToPutMain + ".m");
            }

            File inputVectors = data.mInputVectors;

            CProject cproject = new CProject(cRecipe, data.getSettings());

            DataStore setupTable = data.getSettings();

            TypedInstance topLevelInstance;
            if (data.functionToPutMain.isEmpty()) {
                TypesMap types = MatlabToCOldExecute.getUserTypes(data.getSettings(), data.aspectFiles);
                manager.setDefaultTypes(types);
                topLevelInstance = manager.applyTypeInference(setupTable);

                manager.applyPostTypeInferencePasses();

                if (buildAndAddImplementation(manager,
                        topLevelInstance,
                        systemFunctions,
                        variableAllocator,
                        ssaToCRules,
                        cproject) == null) {

                    System.err.println("Could not build implementation for function");
                    return 1;
                }
            } else {
                // Check if there are input vectors.
                if (inputVectors == null) {
                    throw new UnsupportedOperationException();
                }

                File dataFolder = data.dataFolder;

                overrideTypes(varTypeDefinition, data.functionToPutMain);
                // Get the input instructions
                MatlabToCVectorsOutput vectorsOutput = generateCInputFileCode(inputVectors, dataFolder,
                        varTypeDefinition, data.mainOptions.generateMainForCoder, data.generateDataFiles);
                Set<FunctionInstance> extraDependentInstances = new HashSet<>();
                TypesMap types = null;

                try {
                    ScopedMap<VariableType> globalsMap = varTypeDefinition.getSymbolMap("global");
                    if (vectorsOutput == null) {
                        Map<String, StringProvider> scriptFiles = new HashMap<>();
                        scriptFiles.put("MATISSE_init.m", StringProvider.newInstance(inputVectors));

                        TypesMap initTypes = new TypesMap();
                        for (List<String> name : varTypeDefinition.getKeys()) {
                            if (name.size() == 1) {
                                initTypes.addSymbol(varTypeDefinition.getSymbol(name), "MATISSE_init", name.get(0));
                            }
                        }
                        initTypes.addSymbols(Arrays.asList("global"), globalsMap);

                        ScriptInferenceResult result;
                        FunctionInstance inputsInstance;
                        ProjectPassCompilationOptions tempOptions = new ProjectPassCompilationOptions(options)
                                .withAvailableFiles(scriptFiles)
                                .withDefaultTypes(initTypes);
                        try (ProjectPassCompilationManager tempManager = new ProjectPassCompilationManager(
                                tempOptions)) {

                            PassAwareMatlabToCEngine tempEngine = new PassAwareMatlabToCEngine(tempManager,
                                    systemFunctions,
                                    variableAllocator,
                                    ssaToCRules);
                            MFileProvider.setEngine(tempEngine);

                            tempManager.applyPreTypeInferencePasses("MATISSE_init.m");

                            result = tempManager.applyScriptTypeInference(data.getSettings());
                            tempManager.applyPostTypeInferencePasses();

                            types = new TypesMap();
                            types.addSymbols(varTypeDefinition);
                            types.addSymbols(Arrays.asList("global"), result.globalTypes);
                            types.addSymbols(result.returnTypes);

                            TypedInstance vectorsInstance = result.instance;

                            inputsInstance = SsaToCBuilder.buildImplementation(tempManager,
                                    vectorsInstance,
                                    systemFunctions,
                                    variableAllocator,
                                    ssaToCRules);
                        }

                        CInstructionList instructions = new CInstructionList();

                        List<CNode> outputArguments = new ArrayList<>();
                        List<String> outputsAsInputsNames = inputsInstance.getFunctionType().getOutputAsInputNames();
                        for (String outputName : outputsAsInputsNames) {
                            VariableType type = result.returnTypes.getSymbol(outputName);

                            outputArguments.add(CNodeFactory.newVariable(outputName, type));
                        }

                        CNode functionCall = CNodeFactory.newFunctionCall(inputsInstance, outputArguments);
                        if (outputsAsInputsNames.isEmpty() && !result.returnTypes.getKeys().isEmpty()) {
                            // Classic return
                            List<List<String>> keys = result.returnTypes.getKeys();

                            assert keys.size() == 1;
                            List<String> key = keys.get(0);
                            assert key.size() == 1;
                            String returnVarName = key.get(0);
                            VariableType returnType = result.returnTypes.getSymbol(returnVarName);

                            instructions.addAssignment(CNodeFactory.newVariable(returnVarName, returnType),
                                    functionCall);
                        } else {
                            instructions.addInstruction(functionCall);
                        }

                        vectorsOutput = new MatlabToCVectorsOutput(instructions, types);
                    } else {
                        types = vectorsOutput.localVariables;
                        types.addSymbols(varTypeDefinition);
                    }

                    CInstructionList prefixInstructions = new CInstructionList();
                    for (List<String> var : globalsMap.getKeys()) {
                        assert var.size() == 1;
                        String varName = var.get(0);

                        VariableType type = globalsMap.getSymbol(var);
                        GlobalVariableInstance globalInstance = new GlobalVariableInstance(varName, type);
                        CNode variableNode = globalInstance.getGlobalNode();
                        extraDependentInstances.add(globalInstance);
                        prefixInstructions
                                .add(type.code().getSafeDefaultDeclaration(variableNode,
                                        ProviderData.newInstance("global-provider-data")));
                    }
                    if (prefixInstructions.get().size() != 0) {
                        CInstructionList newInstructions = new CInstructionList();
                        newInstructions.add(prefixInstructions);
                        newInstructions.add(vectorsOutput.instructions);
                        vectorsOutput = new MatlabToCVectorsOutput(newInstructions, vectorsOutput.localVariables);
                    }

                    MFileProvider.setEngine(engine);
                    manager.setDefaultTypes(types);
                    topLevelInstance = manager.applyTypeInference(setupTable);

                    manager.applyPostTypeInferencePasses();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(manager.getPassLog());
                    return 1;
                }

                FunctionInstance implementation = buildAndAddImplementation(manager,
                        topLevelInstance,
                        systemFunctions,
                        variableAllocator,
                        ssaToCRules,
                        cproject);

                // FunctionInstance mainImpl = getMainFunction(implementationData, matrixFolder);
                FunctionInstance mainImpl = getMainFunction(implementationData, dataFolder, implementation,
                        vectorsOutput,
                        extraDependentInstances,
                        false);

                // Add main function to the test function file
                if (mainImpl != null) {
                    cproject.addFunction(mainImpl);
                }

                // Add main for coder
                if (data.mainOptions.generateMainForCoder) {
                    FunctionInstance coderMainImpl = getMainFunction(implementationData, dataFolder, implementation,
                            vectorsOutput,
                            Collections.emptySet(),
                            true);

                    // Add main function to the test function file
                    if (coderMainImpl != null) {
                        cproject.addFunction(coderMainImpl);
                    }

                }

            }

            // Delete files in output folder
            if (data.deleteOutputFolderContents) {
                SpecsIo.deleteFolderContents(data.outputFolder);
            }

            // Write CProject files
            if (data.generateSingleFile.isEmpty()) {
                CirUtils.writeProject(cproject, data.outputFolder);
            } else {
                CirUtils.writeProjectUniqueFile(cproject, data.generateSingleFile, data.outputFolder);
            }

            // Show C tree
            if (data.showCTree) {
                SpecsLogs.msgInfo(cproject.toString());
            }

            runPostCompilationStep();

            // Save data before returning
            DataUtils.saveData(data.outputFolder, data.getSettings());

            return 0;
        }
    }

    private static void overrideTypes(TypesMap types, String functionName) {
        Map<String, VariableType> functionTypes = new HashMap<>(types.getSymbols(Arrays.asList(functionName)));
        for (String varName : functionTypes.keySet()) {
            types.addSymbol(varName, functionTypes.get(varName));
        }
    }

    private static FunctionInstance buildAndAddImplementation(ProjectPassCompilationManager manager,
            TypedInstance topLevelInstance,
            MatlabFunctionTable systemFunctions,
            VariableAllocator variableAllocator,
            SsaToCRuleList ssaToCRules,
            CProject cproject) {

        FunctionInstance implementation = buildImplementation(manager,
                topLevelInstance,
                systemFunctions,
                variableAllocator,
                ssaToCRules);

        if (implementation == null) {
            SpecsLogs.msgInfo("Stopping: could not implement function '"
                    + topLevelInstance.getFunctionIdentification().getName() + "'");
            return null;
        }

        cproject.addFunction(implementation);

        return implementation;
    }

    private void runPostCompilationStep() {
        PostCodeGenAction runnable = data.getSettings().get(MatlabToCKeys.POST_CODEGEN_STEP);
        if (runnable != null) {
            runnable.run(data.outputFolder);
        }
    }

    private FunctionInstance getMainFunction(ImplementationData implementationData,
            File matrixFolder,
            FunctionInstance functionInstance,
            MatlabToCVectorsOutput vectorsOutput,
            Set<FunctionInstance> extraDependentInstances,
            boolean generateMainForCoder) {

        Optional<FunctionNode> functionToTestTry = implementationData
                .getProjectMFiles()
                .getUserFunction(data.functionToPutMain);

        TypesMap inputVectorsTypes = vectorsOutput.localVariables;

        if (!functionToTestTry.isPresent()) {
            SpecsLogs.msgInfo("Tried to put a main() function in Matlab function '" + data.functionToPutMain
                    + "' but could not find it. ");
            return null;
        }

        FunctionNode functionToTest = functionToTestTry.get();

        // Build prototype
        String functionName = functionToTest.getFunctionName();

        CInstructionList inputInstructions = vectorsOutput.instructions;

        // Create a copy of given variable types (it will have the correct
        // scope)
        TypesMap testFunctionVars = new TypesMap();

        List<String> scope = Arrays.asList(functionToTest.getFunctionName());
        testFunctionVars.addSymbols(scope, inputVectorsTypes);

        scope = Arrays.asList(functionToTest.getFunctionName());
        testFunctionVars.addSymbols(scope, vectorsOutput.localVariables);

        MFunctionPrototype functionPrototype = MFunctionPrototype.newMainFunction(false, functionName, functionName,
                functionToTest, implementationData);

        // Discover to which variables should the function be specialized
        List<String> inputNames = MatlabToCOldExecute.getTestInputNames(functionPrototype, testFunctionVars);
        // Set input names
        testFunctionInputs = inputNames;

        // Get implementation with local types
        FunctionInstance implementationToTest = functionInstance;

        // If coder main, generate and return
        if (generateMainForCoder) {
            generateCoderMain(implementationToTest, MatlabToCExecute.MAIN_FUNCTION_FILE, inputInstructions,
                    data.mainOptions,
                    data.getSettings());
            return null;
        }

        // Get main function
        // MultiMainInstance mainFunction = MultiMainInstance.newInstance(implementationToTest,
        FunctionInstance mainFunction = TestMainInstance.newInstance(implementationToTest,
                MatlabToCExecute.MAIN_FUNCTION_FILE,
                inputInstructions,
                extraDependentInstances,
                data.mainOptions, data.getSettings());

        return mainFunction;
    }

    public void generateCoderMain(FunctionInstance testFunction, String cFilename, CInstructionList inputInstructions,
            TestMainOptions options, DataStore setup) {

        FunctionInstance mainFunction = CoderMainInstance.newInstance(testFunction, cFilename, inputInstructions,
                options, setup, data.functionToPutMain);

        CFile cfile = new CFile(mainFunction.getCFilename());
        cfile.addFunction(mainFunction);

        // File coderMain = new File(data.outputFolder, mainFunction.getCFilename() + ".coder");
        File coderMain = new File(data.outputFolder, cfile.getCFilename() + ".coder");
        SpecsIo.write(coderMain, CodeGeneratorUtils.cFileCode(cfile));
    }

    /**
     * Reads a matlab source input file and generates the equivalent C input source file.
     * 
     * <p>
     * IMPORTANT: the given TypesMap must be flat, i.e. should contain no scope (e.g., function name), only variable
     * names and the respective variable type.
     * 
     * @param matlabInputFile
     *            the matlab file with the input source code
     * @param aspectDefinitions
     *            types for variables, in case we want to override the type in the input file
     * @param isCoderMain
     *            true if the target code is to be used with Coder, false if MATISSE
     * @return a string with the C code
     */

    public MatlabToCVectorsOutput generateCInputFileCode(File matlabInputFile, File matrixFolder,
            TypesMap aspectDefinitions, boolean isCoderMain, boolean generateDataFiles) {

        // System.out.println("[ generateCInputFileCode ] input file: " + matlabInputFile);

        String extension = SpecsIo.getExtension(matlabInputFile);

        if (extension.equals("m")) {
            return null;
        }

        if (extension.equals("mat")) {
            return getInputsFromMatFile(matlabInputFile, matrixFolder, aspectDefinitions, isCoderMain,
                    generateDataFiles);
        }

        throw new RuntimeException("Extension not supported:" + extension);
    }

    /**
     * Gets the input C instructions from a .MAT file.
     * 
     * @param matlabInputFile
     *            - the input .MAT file
     * @param aspectDefinitions
     * @return an instance of {@link MatlabToCVectorsOutput}
     */
    private MatlabToCVectorsOutput getInputsFromMatFile(File matlabInputFile, File matrixFolder,
            TypesMap aspectDefinitions, boolean isCoderMain, boolean generateDataFiles) {

        // Read the .MAT file
        MatFile file = new MatFile(matlabInputFile);
        file.read();

        // Get the C instructions
        CWriter cwriter = new CWriter(data.getSettings(), aspectDefinitions);

        List<MLArray> variables = file.getVariables();

        CInstructionList instructions = new CInstructionList();

        // Loading from files is still not fully supported when using static matrices
        List<MLArray> smallVariables;
        if (data.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            smallVariables = CPersistenceUtils.extractBigMatrices(variables, instructions, matrixFolder,
                    ProviderData.newInstance(data.getSettings()), aspectDefinitions, generateDataFiles);
        } else {
            smallVariables = variables;
        }

        CInstructionList smallVariableInstructions = cwriter.contentsAsCCode(smallVariables, isCoderMain);
        instructions.add(smallVariableInstructions);

        TypesMap localVariables = MatlabToCOldExecute.getLocalVars(instructions);

        return new MatlabToCVectorsOutput(instructions, localVariables);
    }

    private static FunctionInstance buildImplementation(ProjectPassCompilationManager manager,
            TypedInstance topLevelInstance,
            MatlabFunctionTable systemFunctions,
            VariableAllocator variableAllocator,
            SsaToCRuleList ssaToCRules) {

        return SsaToCBuilder.buildImplementation(manager, topLevelInstance, systemFunctions,
                variableAllocator,
                ssaToCRules);
    }
}
