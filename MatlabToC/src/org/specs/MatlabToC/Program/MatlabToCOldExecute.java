/*
 * Copyright 2012 Specs.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.CFile;
import org.specs.CIR.CProject;
import org.specs.CIR.CirKeys;
import org.specs.CIR.CirUtils;
import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.StubInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.VariableType;
import org.specs.JMatIOPlus.MatFile;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabAspects.MatlabAspectsUtils;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.DataUtils;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.CCode.CWriter;
import org.specs.MatlabToC.CodeBuilder.MatlabToCBuilder;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFunctions.MFunctionPrototype;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.MatlabToC.SystemInfo.ProjectMFiles;
import org.specs.MatlabToC.VariableStorage.CPersistenceUtils;
import org.specs.MatlabToC.jOptions.BuildersMap;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToC.jOptions.PostCodeGenAction;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.suikasoft.CMainFunction.Builder.TestMainInstance;
import org.suikasoft.CMainFunction.Builder.TestMainOptions;
import org.suikasoft.CMainFunction.Builder.Coder.CoderMainInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.jmatio.types.MLArray;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.utilities.ProgressCounter;

/**
 * Transforms Matlab code into C code.
 * 
 * @author Joao Bispo
 */
public class MatlabToCOldExecute {

    public static final String MAIN_FUNCTION_FILE = "main_test";
    public static final String ALL_FILES = "*";

    private final MatlabToCData data;
    private final TypesMap varTypeDefinition;

    private Collection<String> stubs;
    private List<String> testFunctionInputs;

    private MatlabToCExecute newExecute;

    // private boolean generateCoderMain = true;

    public MatlabToCOldExecute(MatlabToCData data) {
        this(data, new TypesMap());
    }

    public MatlabToCOldExecute(MatlabToCData data, TypesMap varTypeDefinition) {
        this.data = data;
        this.varTypeDefinition = varTypeDefinition;
        stubs = Collections.emptyList();
        testFunctionInputs = null;
    }

    public Collection<String> getStubs() {
        return stubs;
    }

    /**
     * @return the testFunctionInputs
     */
    public List<String> getTestFunctionInputs() {
        if (newExecute != null) {
            return newExecute.getTestFunctionInputs();
        }
        return testFunctionInputs;
    }

    /**
     * Currently checks for the following cases:
     * <p>
     * 1. If there are types with dynamic allocation and dynamic allocation is disabled
     * 
     * @return true if the data is alright, or false if a problem is found
     */
    private boolean checkData() {
        DataStore setup = data.getSettings();

        // 1. If dynamic allocation is not allowed, check if all the types do not use dynamic allocation
        if (!setup.get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            for (List<String> key : varTypeDefinition.getKeys()) {
                VariableType type = varTypeDefinition.getSymbol(key);

                if (!type.usesDynamicAllocation()) {
                    continue;
                }

                // Found a type with dynamic allocation
                SpecsLogs.msgInfo(" - Found dynamically allocated type '" + type + "' for scope '" + key
                        + "', but dynamic allocation is disabled.");

                return false;
            }

        }

        return true;
    }

    public int execute() {

        boolean newPath = data.getSettings().get(MatlabToCKeys.USE_PASS_SYSTEM);
        if (newPath) {
            newExecute = new MatlabToCExecute(data, varTypeDefinition);
            return newExecute.execute();
        }

        MFileProvider.setEngine(MFileProvider.buildEngine(data.getSettings(), data.languageMode));

        // public int execute(File matrixFolder) {
        // public int execute() {

        /*
        	if (data.resetAspectsBeforEachRun) {
        	    varTypeDefinition = new TypesMap();
        	}
        	*/

        // Add symbols
        TypesMap userTypes = getUserTypes(data.getSettings(), data.aspectFiles);
        varTypeDefinition.addSymbols(userTypes);

        // Check data
        if (!checkData()) {
            return 1;
        }

        // Create table with the M-files of the project
        ImplementationData implementationData = MatlabToCUtils.newImplementationData(data.languageMode,
                varTypeDefinition,
                data.getSettings());

        // Add additional builders
        addCustomBuilders(implementationData, data.getSettings());

        // Add project files
        addMatlabFiles(implementationData);

        // Implement prototypes
        Map<String, FunctionNode> topLevelFunctions = getTopLevelFunctions(implementationData.getProjectMFiles());

        List<FunctionInstance> mFilesImplementations = SpecsFactory.newArrayList();
        for (String mainFunctionName : topLevelFunctions.keySet()) {
            FunctionNode function = topLevelFunctions.get(mainFunctionName);
            String functionName = function.getFunctionName();

            String filename = mainFunctionName + ".m";

            // Build prototype
            MFunctionPrototype mFunctionProto = MFunctionPrototype.newMainFunction(false, filename, functionName,
                    function, implementationData);

            // FunctionInstance implementation = MatlabToCUtils.buildImplementation(mFunctionProto,
            FunctionInstance implementation = MatlabToCUtils.buildImplementation(mFunctionProto, varTypeDefinition,
                    data.getSettings());

            if (implementation == null) {
                SpecsLogs.msgInfo("Stopping: could not implement function '" + function.getFunctionName() + "'");
                return -1;
            }

            mFilesImplementations.add(implementation);

        }

        CProject cproject = new CProject(DefaultRecipes.DefaultCRecipe, data.getSettings());

        // Add Matlab implementations to the project
        for (FunctionInstance implementation : mFilesImplementations) {
            cproject.addFunction(implementation);
        }

        if (!data.functionToPutMain.isEmpty()) {
            File dataFolder = data.dataFolder;
            // FunctionInstance mainImpl = getMainFunction(implementationData, matrixFolder);
            FunctionInstance mainImpl = getMainFunction(implementationData, dataFolder, false);

            // Add main function to the test function file
            if (mainImpl != null) {
                cproject.addFunction(mainImpl);
            }

            // Add main for coder
            if (data.mainOptions.generateMainForCoder) {
                FunctionInstance coderMainImpl = getMainFunction(implementationData, dataFolder, true);

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
        // CirUtils.writeProject(cproject, data.outputFolder);

        // Show C tree
        if (data.showCTree) {
            SpecsLogs.msgInfo(cproject.toString());
        }

        // Check if project has stubs
        // TODO verbose option

        Collection<String> stubs = cproject.getStubs();
        if (!stubs.isEmpty()) {

            // Inform user
            if (data.showInfoMessages) {
                String stubFilename = StubInstance.getStubFilename();
                SpecsLogs
                        .msgInfo(
                                " -> Could not implement one or more functions. Created the following stubs in the file "
                                        + stubFilename + ".c:\n " + stubs);
            }

            // Set stubs
            this.stubs = stubs;
        }

        runPostCompilationStep();

        // Save data before returning
        DataUtils.saveData(data.outputFolder, data.getSettings());

        // Show var types
        // System.out.println("VAR TYPES:\n" + varTypeDefinition);

        // System.out.println("TYPES:\n"
        // + new SetupHelper(data.generalSetup).getValue(MatlabToCOption.FinalTypeDefinition,
        // TypesMap.class));

        return 0;
    }

    private void runPostCompilationStep() {
        PostCodeGenAction runnable = data.getSettings().get(MatlabToCKeys.POST_CODEGEN_STEP);
        if (runnable != null) {
            runnable.run(data.outputFolder);
        }
    }

    public static void addCustomBuilders(ImplementationData implementationData, DataStore setup) {
        BuildersMap buildersMap = setup.get(MatlabToCKeys.MATLAB_BUILDERS);

        MatlabFunctionTable functionTable = implementationData.getBuiltInPrototypes();

        // Add builders in the map
        for (String functionName : buildersMap.getFunctions()) {
            functionTable.addBuilderFirst(functionName, buildersMap.getBuilders(functionName));
        }

    }

    /**
     * @return
     */
    public static TypesMap getUserTypes(DataStore matlabToCSetup, List<File> aspectFiles) {
        TypesMap allTypes = new TypesMap();

        // Add types from simple definition (including global variables)
        // MatrixImplementation impl = new CirSetup(matlabToCSetup).getMatrixImplementation();
        MatlabAspectsUtils aspects = new MatlabAspectsUtils(matlabToCSetup);
        TypesMap aspectTypes = aspects.newTypesMap(aspectFiles);

        // Check for default real
        if (aspects.getDefaultReal().isPresent()) {
            matlabToCSetup.set(CirKeys.DEFAULT_REAL, aspects.getDefaultReal().get());
        }

        // varTypeDefinition.addSymbols(aspectTypes);
        allTypes.addSymbols(aspectTypes);

        // Add types from LARA aspects
        TypesMap laraTypes = matlabToCSetup.get(MatisseKeys.TYPE_DEFINITION);
        allTypes.addSymbols(laraTypes);

        return allTypes;
    }

    /**
     * @param implementationData
     */
    private void addMatlabFiles(ImplementationData implementationData) {
        ProgressCounter progress = new ProgressCounter(data.matlabFiles.size());

        for (File matlabFile : data.matlabFiles) {
            if (data.matlabFiles.size() > 1) {
                SpecsLogs.msgInfo("Processing file '" + matlabFile.getName() + "' " + progress.next());
            }

            implementationData.getProjectMFiles().addUserFile(matlabFile);

        }
    }

    /**
     * @param generateMainForCoder
     * @param projectMFiles
     * @return
     */
    private FunctionInstance getMainFunction(ImplementationData implementationData, File matrixFolder,
            boolean generateMainForCoder) {
        Optional<FunctionNode> functionToTest = implementationData.getProjectMFiles()
                .getUserFunction(data.functionToPutMain);

        if (!functionToTest.isPresent()) {
            SpecsLogs.msgInfo("Tried to put a main() function in Matlab function '" + data.functionToPutMain
                    + "' but could not find it. ");
            return null;
        }

        // Build prototype
        String functionName = functionToTest.get().getFunctionName();
        MFunctionPrototype functionToTestProto = MFunctionPrototype.newMainFunction(false, functionName + ".m",
                functionName, functionToTest.get(), implementationData);

        // Create main function
        FunctionInstance mainFunction = newMainFunction(functionToTestProto, data.mInputVectors, matrixFolder,
                varTypeDefinition, data.mainOptions, generateMainForCoder);

        return mainFunction;
    }

    /**
     * @param projectMFiles
     * @return
     */
    private Map<String, FunctionNode> getTopLevelFunctions(ProjectMFiles projectMFiles) {
        if (data.topLevelMFiles.isEmpty()) {
            return SpecsFactory.newHashMap();
        }

        Map<String, FunctionNode> topLevelFunctions = SpecsFactory.newHashMap();

        for (String functionName : data.topLevelMFiles) {
            // Check if any of the names is <all files>
            if (functionName.equals(MatlabToCOldExecute.ALL_FILES)) {
                return projectMFiles.getMainUserFunctions();
                // return matlabToC.getProjectPrototypes().getUserPrototypes();
            }

            Optional<FunctionNode> mainFunction = projectMFiles.getUserFunction(functionName);

            if (!mainFunction.isPresent()) {
                SpecsLogs.msgInfo("Top-level function: could not find function with name '" + functionName + "'");
                continue;
            }

            topLevelFunctions.put(functionName, mainFunction.get());
        }

        return topLevelFunctions;
    }

    /**
     * @param fileToTest
     * @param mInputVectors
     * @param varTypeDefinition
     */
    private FunctionInstance newMainFunction(MFunctionPrototype functionToTest, File mInputVectors, File matrixFolder,
            TypesMap varTypes, TestMainOptions mainOptions, boolean isCoderMain) {

        // Check if there are input vectors.
        if (mInputVectors != null) {

            // Get variable definition for the input vectors
            TypesMap inputVectorsTypes = varTypes.getTypesMap(Arrays.asList(functionToTest.getFunctionName()));

            // Get the input instructions
            MatlabToCVectorsOutput vectorsOutput = generateCInputFileCode(mInputVectors, matrixFolder,
                    inputVectorsTypes, isCoderMain);

            CInstructionList inputInstructions = vectorsOutput.instructions;
            // Create a copy of given variable types (it will have the correct
            // scope)
            TypesMap testFunctionVars = new TypesMap();

            List<String> scope = Arrays.asList(functionToTest.getFunctionName());
            testFunctionVars.addSymbols(scope, inputVectorsTypes);

            scope = Arrays.asList(functionToTest.getFunctionName());
            testFunctionVars.addSymbols(scope, vectorsOutput.localVariables);

            // Discover to which variables should the function be specialized
            List<String> inputNames = getTestInputNames(functionToTest, testFunctionVars);
            // Set input names
            testFunctionInputs = inputNames;

            // Get implementation with local types
            FunctionInstance implementationToTest = MatlabToCUtils.buildImplementation(functionToTest, inputNames,
                    testFunctionVars, data.getSettings());

            // If coder main, generate and return
            if (isCoderMain) {
                generateCoderMain(implementationToTest, MatlabToCOldExecute.MAIN_FUNCTION_FILE, inputInstructions,
                        mainOptions,
                        data.getSettings());
                return null;
            }

            // Get main function
            // MultiMainInstance mainFunction = MultiMainInstance.newInstance(implementationToTest,
            FunctionInstance mainFunction = TestMainInstance.newInstance(implementationToTest,
                    MatlabToCOldExecute.MAIN_FUNCTION_FILE,
                    inputInstructions,
                    Collections.emptySet(),
                    mainOptions, data.getSettings());

            return mainFunction;
        }

        // Build main function based on a specialization of the test function
        // with the global Workspace Variables
        // Get implementation with local types
        FunctionInstance implementationToTest = MatlabToCUtils.buildImplementation(functionToTest, varTypes,
                data.getSettings());

        if (isCoderMain) {
            generateCoderMain(implementationToTest, MatlabToCOldExecute.MAIN_FUNCTION_FILE, null, data.mainOptions,
                    data.getSettings());

            return null;
        }

        // Build main function based on the function to test
        FunctionInstance mainFunction = TestMainInstance.newInstance(implementationToTest,
                MatlabToCOldExecute.MAIN_FUNCTION_FILE, null,
                Collections.emptySet(),
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
     * Adds the name of the inputs of the function, testing for each one if there is a type available. When a type is
     * not found for an input, stops and returns the collected names so far.
     * 
     * @param functionToTest
     * @param testFunctionVars
     * @return
     */
    static List<String> getTestInputNames(MFunctionPrototype functionToTest, TypesMap testFunctionVars) {

        List<String> instanceInputs = SpecsFactory.newArrayList();

        for (String inputName : functionToTest.getInputNames()) {
            // Check if a type exists in the table
            VariableType type = testFunctionVars.getSymbol(functionToTest.getScope(), inputName);
            if (type == null) {
                return instanceInputs;
            }

            instanceInputs.add(inputName);
        }

        return instanceInputs;
    }

    /*
    public MatlabToCVectorsOutput generateCInputFileCode(File matlabInputFile,
        TypesMap aspectDefinitions) {
    
    return generateCInputFileCode(matlabInputFile, aspectDefinitions, false);
    }
    */

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
            TypesMap aspectDefinitions, boolean isCoderMain) {

        // System.out.println("[ generateCInputFileCode ] input file: " + matlabInputFile);

        String extension = SpecsIo.getExtension(matlabInputFile);

        if (extension.equals("m")) {
            ProviderData providerData = ProviderData.newInstance(data.getSettings());

            return getInputsFromMFile(matlabInputFile, aspectDefinitions, providerData);
        }

        if (extension.equals("mat")) {
            return getInputsFromMatFile(matlabInputFile, matrixFolder, aspectDefinitions, isCoderMain);
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
            TypesMap aspectDefinitions, boolean isCoderMain) {

        // Read the .MAT file
        MatFile file = new MatFile(matlabInputFile);
        file.read();

        // Get the C instructions
        CWriter cwriter = new CWriter(data.getSettings(), aspectDefinitions);

        List<MLArray> variables = file.getVariables();

        CInstructionList instructions = new CInstructionList();

        // Loading of data is not working on Linux, disabling for now
        // List<MLArray> smallVariables = variables;

        // List<MLArray> smallVariables = CPersistenceUtils.extractBigMatrices(variables, instructions, matrixFolder,
        // ProviderData.newInstance(data.generalSetup), aspectDefinitions);

        // Loading from files is still not fully supported when using static matrices
        List<MLArray> smallVariables;
        if (data.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            smallVariables = CPersistenceUtils.extractBigMatrices(variables, instructions, matrixFolder,
                    ProviderData.newInstance(data.getSettings()), aspectDefinitions, data.generateDataFiles);
        } else {
            smallVariables = variables;
        }

        /*
        if (new CirSetup(data.generalSetup).useStaticAllocation()) {
            smallVariables = variables;
        } else {
            smallVariables = CPersistenceUtils.extractBigMatrices(variables, instructions, matrixFolder,
        	    ProviderData.newInstance(data.generalSetup), aspectDefinitions);
        }
        */

        // = variables;

        // List<MLArray> smallVariables = CPersistenceUtils.extractBigMatrices(variables, instructions, matrixFolder,
        // ProviderData.newInstance(data.generalSetup), aspectDefinitions);

        CInstructionList smallVariableInstructions = cwriter.contentsAsCCode(smallVariables, isCoderMain);
        instructions.add(smallVariableInstructions);

        // System.out.println("CODE:");
        // instructions.get().forEach(inst -> System.out.println(inst.getCode()));
        // Get the local variables
        TypesMap localVariables = getLocalVars(instructions);

        return new MatlabToCVectorsOutput(instructions, localVariables);
    }

    /**
     * @param matlabInputFile
     * @param aspectDefinitions
     * @return
     */
    private MatlabToCVectorsOutput getInputsFromMFile(File matlabInputFile, TypesMap aspectDefinitions,
            ProviderData providerData) {

        ImplementationData implementationData = MatlabToCUtils.newImplementationData(data.languageMode,
                aspectDefinitions,
                data.getSettings());

        // Create a matlab token from the file
        FileNode initMatlabToken = new MatlabParser().parse(matlabInputFile);

        // Create the FunctionData, get the script token and create the list of
        // C instructions

        MatlabToCFunctionData matlabData = MatlabToCFunctionData.newInstance(providerData, implementationData,
                new ArrayList<String>());

        // Set functionName, in case of script
        String functionName = SpecsIo.removeExtension(matlabInputFile.getName());
        matlabData.setFunctionName(functionName);

        ScriptNode scriptToken = initMatlabToken.getScript();
        CInstructionList cInstructions = MatlabToCBuilder.build(scriptToken, matlabData);
        TypesMap localVariables = matlabData.getLocalVariableTypes();

        return new MatlabToCVectorsOutput(cInstructions, localVariables);
    }

    public static TypesMap getLocalVars(CInstructionList cInstructions) {

        // Get map with local variables
        Map<String, VariableType> localVarMap = cInstructions.getLocalVars();

        // Add them to types map
        TypesMap typesMap = new TypesMap();
        for (String variableName : localVarMap.keySet()) {
            VariableType variableType = localVarMap.get(variableName);

            typesMap.addSymbol(Arrays.asList(variableName), variableType);
        }

        return typesMap;
    }

}
