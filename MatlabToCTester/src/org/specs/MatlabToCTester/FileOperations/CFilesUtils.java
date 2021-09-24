/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToCTester.FileOperations;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabAspects.MatlabAspectsUtils;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.MatlabToC.Program.MatlabToCData;
import org.specs.MatlabToC.Program.MatlabToCOldExecute;
import org.specs.MatlabToC.Program.MatlabToCSetup;
import org.specs.MatlabToC.Program.MatlabToCVectorsOutput;
import org.specs.MatlabToC.Program.Global.MatlabToCGlobalData;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.MatlabToCTester.MatlabToCTesterData;
import org.specs.MatlabToCTester.MatlabToCTesterUtils;
import org.specs.MatlabToCTester.Auxiliary.InputFolders;
import org.specs.MatlabToCTester.Auxiliary.MatlabSession;
import org.specs.MatlabToCTester.Auxiliary.OutputFolders;
import org.specs.MatlabToCTester.Auxiliary.TestableFunction;
import org.specs.MatlabToCTester.CGeneration.CGenerator;
import org.specs.MatlabToCTester.CGeneration.CodegenUtils;
import org.suikasoft.CMainFunction.Builder.MainFunctionTarget;
import org.suikasoft.CMainFunction.Builder.TestMainSetup;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.lang.SpecsPlatforms;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.collections.HashSetString;
import pt.up.fe.specs.util.jobs.Job;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * Utility methods related to the creation and compilation of C files.
 * 
 * @author Joao Bispo
 * 
 */
public class CFilesUtils {

    private static final Collection<String> C_EXTENSIONS = Arrays.asList("c");

    private final MatlabToCTesterData data;

    private int totalTests;

    public CFilesUtils(MatlabToCTesterData data) {
        this.data = data;
        totalTests = -1;
    }

    public int getTotalTests() {
        return totalTests;
    }

    /**
     * Creates C source files from matlab source files. Calls MatlabToC.MatlabToCExecute.
     * 
     * @param runOnlyOneTest
     * @param setup
     * @param cGenerator
     * 
     * @return
     */
    public List<TestableFunction> createCTests(InputFolders inputFolders) {

        OutputFolders outputFolders = data.outputFolders;
        SetupData mainOptionsData = data.mainOptionsData;
        SetupData implementationData = data.implementationData;
        String runOnlyOneTest = data.testerOptions.getRunOnlyOneTest();
        DataStore setup = data.getSettings();
        CGenerator cGenerator = data.testerOptions.getcGenerator();

        String customRecipeFile = setup.get(MatlabToCKeys.CUSTOM_RECIPE);
        String customPreTypeSsaRecipeFile = setup.get(MatlabToCKeys.CUSTOM_PRE_TYPE_SSA_RECIPE);

        // Get aspect files
        List<File> aspectFiles = inputFolders.getAspectsFiles();

        // Setup aspectSetup = inputFolders.getAspectSetup();

        // Filter files that end in .aspect
        aspectFiles.removeIf(file -> !SpecsIo.getExtension(file).equals("aspect"));

        MatlabAspectsUtils aspects = new MatlabAspectsUtils(setup);
        TypesMap aspectsTypes = aspects.newTypesMap(aspectFiles);

        // Check for default real
        if (aspects.getDefaultReal().isPresent()) {
            setup.set(CirKeys.DEFAULT_REAL, aspects.getDefaultReal().get());
        }

        // Get M-files to test
        List<File> functionsToTest = MatlabToCTesterUtils.getTests(inputFolders.getTestsFolder(), runOnlyOneTest);

        // Build input vectors map
        Map<String, File> inputVectorsMap = MatlabToCTesterUtils.getInputVectorsMap(inputFolders
                .getInputVectorsFolder());

        List<TestableFunction> createdTests = SpecsFactory.newArrayList();

        // Get total number of tests
        // totalTests = calculateNumberOfTest(functionsToTest, inputVectorsMap);
        totalTests = 0;

        // Start MATLAB

        MatlabSession matlabSession = null;
        /*
        if (cGenerator == CGenerator.MathworksCodegen) {
            matlabSession = MatlabSession.newInstance();
        }
        */
        Optional<String> runOneTest = Optional.ofNullable(runOnlyOneTest);
        for (File functionToTest : functionsToTest) {
            String functionFilename = functionToTest.getName();
            String functionName = SpecsIo.removeExtension(functionFilename);

            // Get input vectors folder
            File inputVectorsFolder = inputVectorsMap.get(functionName);
            if (inputVectorsFolder == null && runOnlyOneTest == null) {
                SpecsLogs.msgInfo("!Skipping test file '" + functionName
                        + "': could not find corresponding input vectors folder.");
                continue;
            }

            // Get input vectors for given test file
            List<File> inputVectors = MatlabToCTesterUtils.getInputVectors(inputVectorsFolder, runOneTest);

            // Add to the total number of tests
            totalTests += inputVectors.size();

            // User message
            SpecsLogs.msgInfo("Creating tests for function '" + functionName + "' (" + inputVectors.size()
                    + " test vectors)");

            List<File> inputVectorsUpdated = SpecsFactory.newArrayList();
            List<List<String>> testInputs = SpecsFactory.newArrayList();
            // Process input vector
            for (File inputVector : inputVectors) {

                // String inputFilename = inputVector.getName();
                // String inputName = IoUtils.removeExtension(inputFilename);

                // Create the setup data
                MatlabToCData cData = newMatlabToCData(data.languageMode,
                        functionToTest, inputVector, inputFolders,
                        outputFolders,
                        customPreTypeSsaRecipeFile,
                        customRecipeFile,
                        mainOptionsData, implementationData,
                        data.testerOptions.isCombinedAuxiliaryFolders(),
                        data.generateDataFiles, setup);

                // Set pass system
                cData.getSettings().set(MatlabToCKeys.USE_PASS_SYSTEM, setup.get(MatlabToCKeys.USE_PASS_SYSTEM));

                // Set types
                // cData.generalSetup.getOptionTable().setOption(MatisseOption.TYPE_DEFINITION, aspectsTypes);

                cData.getSettings().set(MatisseKeys.TYPE_DEFINITION, aspectsTypes);

                List<String> functionInputs = generateC(cGenerator, aspectsTypes, inputVector, cData, matlabSession);

                if (functionInputs == null) {
                    continue;
                }

                // Add input vector to the list of test inputs
                inputVectorsUpdated.add(inputVector);
                testInputs.add(functionInputs);

            }

            // If no tests could be compiled to this function, skip it
            if (inputVectorsUpdated.isEmpty()) {
                if (inputVectors.isEmpty()) {
                    SpecsLogs.msgInfo("!Skipping test file '" + functionName + "': No input vectors.");
                } else {
                    SpecsLogs.msgInfo("!Skipping test file '" + functionName + "': Skipped all input vectors.");
                }
                continue;
            }

            // Create FunctionToTest
            File auxFolder = inputFolders.getAuxiliaryFolder();
            createdTests.add(new TestableFunction(functionToTest, inputVectorsUpdated, testInputs, auxFolder,
                    data.testerOptions.isCombinedAuxiliaryFolders()));
        }

        return createdTests;
    }

    /**
     * @param generalSetup
     * @param setup
     */
    /*
    private static void addOptions(Setup generalSetup, Setup setup) {
    for (Option option : setup.getOptionTable().getOptions()) {
        generalSetup.getOptionTable().setOption(option);
    }
    
    }
    */

    /*
    private int calculateNumberOfTest(List<File> functionsToTest, Map<String, File> inputVectorsMap) {
    int acc = 0;
    
    for (File functionToTest : functionsToTest) {
        String functionName = IoUtils.removeExtension(functionToTest.getName());
        File inputVectorsFolder = inputVectorsMap.get(functionName);
        if (inputVectorsFolder == null) {
    	continue;
        }
        // Get input vectors for given test file
        List<File> inputVectors = MatlabToCTesterUtils.getInputVectors(inputVectorsFolder);
        acc += inputVectors.size();
    }
    
    System.out.println("FOUND " + acc);
    
    // return acc;
    return 0;
    }
    */

    /**
     * @param cGenerator
     * @param aspectsTypes
     * @param inputVector
     * @param cData
     * @param matlabSession
     * @return
     */
    private static List<String> generateC(CGenerator cGenerator, TypesMap aspectsTypes, File inputVector,
            // File matrixFolder, MatlabToCData cData, MatlabSession matlabSession) {
            MatlabToCData cData, MatlabSession matlabSession) {

        switch (cGenerator) {
        case MATISSE:
            // return generateCMatisse(aspectsTypes, inputVector, matrixFolder, cData);
            return generateCMatisse(aspectsTypes, inputVector, cData);
        case MathworksCodegen:
            // return generateCMathworks(aspectsTypes, inputVector, matrixFolder, cData, matlabSession);
            return generateCMathworks(aspectsTypes, inputVector, cData, matlabSession);

        default:
            SpecsLogs.warn("Case not implemented:" + cGenerator);
            return null;
        }
    }

    /**
     * @param userTypes
     * @param inputVector
     * @param cData
     * @param matlabSession
     * @return
     */
    // private static List<String> generateCMathworks(TypesMap userTypes, File inputVector, File matrixFolder,
    private static List<String> generateCMathworks(TypesMap userTypes, File inputVector, MatlabToCData cData,
            MatlabSession matlabSession) {

        MatlabToCOldExecute matlabToC = new MatlabToCOldExecute(cData);

        // Get types for main function
        TypesMap functionTypes = new TypesMap();
        functionTypes.addSymbols(userTypes.getSymbolMap(cData.functionToPutMain));

        File matrixFolder = cData.dataFolder;

        MatlabToCVectorsOutput inputVectorResult = matlabToC.generateCInputFileCode(inputVector, matrixFolder,
                functionTypes, true);

        TypesMap inputTypes = inputVectorResult.localVariables;

        // User-defined types take precedence over inference
        inputTypes.addSymbols(functionTypes);

        // Add user types
        // inputTypes.addSymbols(userTypes);

        File tempFolder = SpecsIo.mkdir(cData.outputFolder, "temp_matlab");

        List<String> inputNames = generateCoderCode(cData, inputTypes, tempFolder);

        SpecsIo.deleteFolderContents(tempFolder);
        tempFolder.delete();

        // Delete temporary folder
        if (cData.deleteOutputFolderContents) {
            SpecsIo.deleteFolderContents(tempFolder);
            tempFolder.delete();
            System.out.println("INSIDE IF");
        }

        return inputNames;
    }

    /**
     * @param cData
     * @param inputTypes
     * @param tempFolder
     * @param originalTypes
     * @return
     */
    private static List<String> generateCoderCode(MatlabToCData cData, TypesMap inputTypes, File tempFolder) {

        // LIMITATION: Auxiliary files cannot have folder substructure
        // SOLUTION: Pass auxiliary folder, to form relative path

        // Copy all M-files to temporary folder
        File mTempFolder = new File(tempFolder, "m_files");
        for (File mfile : cData.matlabFiles) {
            SpecsIo.copy(mfile, new File(mTempFolder, mfile.getName()));
        }

        // Get main M file
        File mainMFile = CodegenUtils.getMainFile(cData.matlabFiles, cData.functionToPutMain);
        if (mainMFile == null) {
            return null;
        }

        // Get input names
        FunctionDeclarationSt declaration = MatlabProcessorUtils.getFunctionDeclaration(mainMFile, cData.languageMode)
                .get();
        List<String> inputNames = declaration.getInputNames();
        String functionName = cData.functionToPutMain;

        // Implementation

        // MemoryAllocationOption alloc = new SetupOptions(cData.generalSetup).getMultipleChoice(
        // CirOption.MEMORY_ALLOCATION, MemoryAllocationOption.class);
        boolean allowDynamicAlloc = cData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION);

        // Write codegen script
        String matlabScript = CodegenUtils.getCodegenScript(functionName, inputNames, inputTypes, allowDynamicAlloc);
        // System.out.println("CODER SCRIPT:\n" + matlabScript);
        File codegenFile = new File(mTempFolder, "codegen_run.m");
        SpecsIo.write(codegenFile, matlabScript);

        // Generate C files
        MatlabSession matlabSession = MatlabSession.getGlobalSession();
        boolean success = matlabSession.runScript(mTempFolder, SpecsIo.removeExtension(codegenFile.getName()));
        if (!success) {
            SpecsLogs.msgInfo("Could not generate C code for '" + cData.functionToPutMain
                    + "' using Coder. Refer to the MATLAB window for a description of the problem.");
            return null;
        }

        // Files will be in folder codegen/lib/<function_name>
        // Copy all .c and .h files in folder, and copy to output folder
        File cFolder = SpecsIo.existingFolder(mTempFolder, "codegen/lib/" + functionName);
        if (cFolder == null) {
            return null;
        }

        List<File> filesToCopy = SpecsIo.getFilesRecursive(cFolder, Arrays.asList("c", "h"));
        for (File fileToCopy : filesToCopy) {
            File destFile = new File(cData.outputFolder, fileToCopy.getName());
            SpecsIo.copy(fileToCopy, destFile);
        }

        // Patch <function>_types.h, it is missing include "rtwtypes.h"
        success = patchTypes(cData.functionToPutMain, cData.outputFolder);
        if (!success) {
            return null;
        }
        return inputNames;
    }

    /**
     * @param functionToPutMain
     * @param outputFolderBase
     */
    private static boolean patchTypes(String functionName, File outputFolder) {
        // Get file
        File typesFile = new File(outputFolder, functionName + "_types.h");

        if (!typesFile.isFile()) {
            SpecsLogs.msgInfo("Could not find types file '" + typesFile.getAbsolutePath()
                    + "'. Check if compilation has no problems.");
            return false;
        }

        String typesContent = SpecsIo.read(typesFile);

        // Marker
        String marker = "#define __" + functionName.toUpperCase() + "_TYPES_H__";

        String patch = "\n\n#include \"rtwtypes.h\"";

        // Replace marker
        typesContent = typesContent.replace(marker, marker + patch);

        SpecsIo.write(typesFile, typesContent);

        return true;
    }

    /**
     * @param aspectsTypes
     * @param inputVector
     * @param cData
     * @return
     */
    // private static List<String> generateCMatisse(TypesMap aspectsTypes, File inputVector, File matrixFolder,
    private static List<String> generateCMatisse(TypesMap aspectsTypes, File inputVector, MatlabToCData cData) {

        MatlabToCOldExecute matlabToC = new MatlabToCOldExecute(cData, aspectsTypes);
        // boolean success = createCTest(inputVector, cData, aspectsTypes);
        // boolean success = createCTest(inputVector, matrixFolder, matlabToC);
        boolean success = createCTest(inputVector, matlabToC);
        /*
        if (!success) {
            continue;
        }
        */
        List<String> functionInputs = null;
        if (success) {
            functionInputs = matlabToC.getTestFunctionInputs();
        } else {
            System.err.println("Failure when generating C code");
        }
        return functionInputs;
    }

    /**
     * Creates a configuration for MatlabToC.
     * 
     * @return
     */
    private static MatlabToCData newMatlabToCData(LanguageMode languageMode,
            File functionToTest,
            File inputVector,
            InputFolders inputFolders,
            OutputFolders outputFolders,
            String customPreTypeSsaRecipeFile,
            String customRecipeFile,
            SetupData mainOptions,
            SetupData implementationData,
            boolean combinedAuxiliaryFolder,
            boolean generateDataFiles,
            DataStore setup) {

        // Get name of the function
        String functionName = SpecsIo.removeExtension(functionToTest.getName());
        String inputVectorName = SpecsIo.removeExtension(inputVector.getName());

        // Create SetupData
        SetupData cData = SetupData.create(MatlabToCSetup.class);

        cData.put(MatlabToCSetup.Language, languageMode.name());

        // MFilesFolder is declared with an empty string because we'll be using absolute paths for the source files
        cData.put(MatlabToCSetup.MFilesFolder, FieldValue.create("", FieldType.string));

        // Collect all the matlab source files needed to run this function test
        List<File> allFiles = SpecsFactory.newArrayList();

        // First collect auxiliary files
        // We'll force separate folders for auxiliaries, since this is post-weaving (and the weaver creates
        // its own folder structure)
        allFiles.addAll(MatlabToCTesterUtils.getAuxiliaryFiles(inputFolders.getAuxiliaryFolder(), functionName,
                false));

        // Add function to test
        allFiles.add(functionToTest);

        // Get strings with file paths
        List<String> filePaths = SpecsFactory.newArrayList();
        for (File sourceFile : allFiles) {
            filePaths.add(sourceFile.getAbsolutePath());
        }

        FieldValue mFilesValue = FieldValue.create(new StringList(filePaths), FieldType.stringList);
        cData.put(MatlabToCSetup.MFiles, mFilesValue);

        // We need to tell which file is the one with the function we want to test so that we can create a main()
        // function that calls it
        FieldValue mainFunctionValue = FieldValue.create(functionName, FieldType.string);
        cData.put(MatlabToCSetup.PutMainInFunction, mainFunctionValue);

        // The input vector
        FieldValue inputVectorValue = FieldValue.create(inputVector.getAbsolutePath(), FieldType.string);
        cData.put(MatlabToCSetup.MFileWithInputVectors, inputVectorValue);

        // The C sources output directory
        File outputFolder = new File(outputFolders.getcSourcesFolder(), functionName);
        outputFolder = SpecsIo.mkdir(outputFolder, inputVectorName);

        FieldValue outputFolderValue = FieldValue.create(outputFolder.getAbsolutePath(), FieldType.string);
        cData.put(MatlabToCSetup.OutputFolder, outputFolderValue);

        File dataFolder = new File(outputFolders.getcExecutablesFolder(), functionName);
        dataFolder = SpecsIo.mkdir(dataFolder, inputVectorName);
        dataFolder = SpecsIo.mkdir(dataFolder, "data");
        cData.put(MatlabToCSetup.DataFolder, dataFolder.getAbsolutePath());

        // FieldValue singleCFileValue = FieldValue.create(outputFolders.getSingleCFile(),
        // FieldType.string);
        // cData.put(MatlabToCSetup.GenerateSingleFile, singleCFileValue);
        cData.putString(MatlabToCSetup.GenerateSingleFile, outputFolders.getSingleCFile());

        // Options for main function
        cData.put(MatlabToCSetup.OptionsForMainFunction, FieldValue.create(mainOptions, FieldType.integratedSetup));

        // Implementation settings
        cData.put(MatlabToCSetup.ImplementationSettings,
                FieldValue.create(implementationData, FieldType.integratedSetup));

        // Optimizations
        // HashSetString opts = setup.getA().value(MatlabToCOption.MATISSE_OPTIMIZATIONS, HashSetString.class);
        HashSetString opts = setup.get(MatlabToCKeys.MATISSE_OPTIMIZATIONS);
        cData.put(MatlabToCSetup.Optimizations,
                FieldValue.create(new StringList(opts), FieldType.multipleChoiceStringList));

        cData.put(MatlabToCSetup.EnableZ3,
                FieldValue.create(setup.get(MatlabToCKeys.ENABLE_Z3), FieldType.bool));

        // Turn off user messages
        cData.put(MatlabToCSetup.ShowInfoMessages, FieldValue.create(Boolean.FALSE, FieldType.bool));

        // TODO: Check if .getFile() is really needed
        // Alternatively, we could save the contents to a file and set the option

        // File setupFile = setup.getSetupFile().getFile();
        // cData.put(MatlabToCSetup.AspectDataFile, setupFile == null ? "" : setupFile.getAbsolutePath());
        // Turn off simple printfs

        cData.put(MatlabToCSetup.CustomPreTypeSsaRecipeFile, customPreTypeSsaRecipeFile);
        cData.put(MatlabToCSetup.CustomRecipeFile, customRecipeFile);

        // If LARA aspect defined, ignore ImplementationSettings
        if (inputFolders.getAspectsFiles().size() == 1
                && inputFolders.getAspectsFiles().get(0).getName().endsWith(".lara")) {

            cData.put(MatlabToCSetup.IgnoreImplementationSettings, Boolean.TRUE);
        }

        cData.put(MatlabToCSetup.GenerateDataFiles, generateDataFiles);

        // Create the C source files
        MatlabToCData parsedData = MatlabToCSetup.newData(cData, new MatlabToCGlobalData(null, null), setup);

        return parsedData;
    }

    /**
     * Creates a test for a given input vector.
     * 
     * @param inputVector
     * @param data
     * @param workspaceVars
     * @return true if the test for the given 'inputVector' could be created. False otherwise
     */
    // private static boolean createCTest(File inputVector, File matrixFolder, MatlabToCExecute matlabToC) {
    private static boolean createCTest(File inputVector, MatlabToCOldExecute matlabToC) {

        String testName = SpecsIo.removeExtension(inputVector.getName());

        // Check if test name does not start with a number
        if (Character.isDigit(testName.charAt(0))) {
            SpecsLogs.msgInfo(" -> Skipping test vector '" + testName + "', filename cannot start with a number. ");
            return false;
        }

        try {
            // matlabToC.execute(matrixFolder);
            matlabToC.execute();

            // Check if generated code has stubs
            Collection<String> stubs = matlabToC.getStubs();
            if (!stubs.isEmpty()) {
                SpecsLogs.msgInfo(" -> Skipping test vector '" + testName
                        + "', C files contains stubs for the following functions:\n " + stubs);
                return false;
            }

        } catch (Exception e) {
            String message = " -> Skipping test vector '" + testName + "', could not create C files.\n";
            // message += e.getMessage();
            SpecsLogs.warn(message, e);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Creates the executables from the generated C code.
     * 
     * @param mainFunctionTarget
     * @param fieldValue
     */
    public void createExecutableBinaries(GeneratedCompileScripts compileScripts) {

        SpecsLogs.msgInfo("Compiling...");

        if (SpecsPlatforms.isWindows()) {
            File batchFile = compileScripts.windowsBatchFile;
            if (batchFile == null) {
                throw new RuntimeException("Can't compile files for Windows: Check target.");
            }
            runJobsInBatch(compileScripts.executableFolder, batchFile);
        } else {
            File bashFile = compileScripts.linuxBashFile;
            if (bashFile == null) {
                throw new RuntimeException("Can't compile ");
            }
            runJobsInBash(compileScripts.executableFolder, bashFile);
        }

    }

    public GeneratedCompileScripts createCompilationScripts(List<TestableFunction> functionsToTest) {
        // The list of compiler jobs

        MainFunctionTarget target = new SetupAccess(data.mainOptionsData).getEnum(TestMainSetup.Target,
                MainFunctionTarget.class);

        File executableFolder = getCompilationExecutableFolder();
        File batchFile = null;
        File bashFile = null;

        if (target.includesWindows()) {
            batchFile = buildBatchFile(functionsToTest, executableFolder);
        }
        if (target.includesLinux()) {
            bashFile = buildBashFile(functionsToTest, executableFolder);
        }

        GeneratedCompileScripts compileScripts = new GeneratedCompileScripts(batchFile, bashFile, executableFolder);
        return compileScripts;
    }

    private File getCompilationExecutableFolder() {
        return data.outputFolders.getBaseOutputFolder();
    }

    /**
     * Creates a list of compiler jobs given the directory of the source files, the output directory and the compiler
     * options.
     * 
     * @param baseDirectory
     *            - the working directory of the compiler
     * @param cSourceTestFilesDirectory
     *            - the directory where the C source folders can be found
     * @param compiler
     *            - the name of the compiler (assumes it is on the system PATH variable)
     * @param optimizationOption
     *            - the optimization flags to use (e.g., -O2)
     * @param compilerOptions
     *            - other compiler options
     * @param cExecutablesDirectory
     *            - the directory where the executable binaries will be saved
     * @return a list of compiler jobs
     */
    private List<Job> createCompilerJobs(List<TestableFunction> functionsToTest, boolean isLinux) {

        List<Job> compilerJobs = SpecsFactory.newArrayList();
        File baseOutputFolder = getCompilationExecutableFolder();
        DataStore matisse = data.getSettings();

        for (TestableFunction function : functionsToTest) {

            String functionName = function.getFunctionName();

            // Iterate over each of these input folders and compile all the
            // files inside each one to a single executable binary file
            for (String inputName : function.getInputNames()) {
                List<String> args = SpecsFactory.newArrayList();

                String compiler = data.compilerOptions.getCompiler();
                args.add(compiler);

                // Create the directory (on disk) where the file will be saved
                // and clean it, then create the file itself
                String outputFolderName = MatlabToCTesterUtils.getTestExecutablePath(data, functionName, inputName)
                        .getAbsolutePath();

                File outputFolder = SpecsIo.mkdir(outputFolderName);
                File outputFile = new File(outputFolder, functionName);
                if (isLinux && outputFile.getName().endsWith(".exe")) {
                    // No .exe ending
                    String previousPath = outputFile.getAbsolutePath();
                    outputFile = new File(previousPath.substring(0, previousPath.length() - ".exe".length()));
                }
                if (!isLinux && !outputFile.getName().endsWith(".exe")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".exe");
                }

                // Build foldername
                File testFolder = new File(data.outputFolders.getcSourcesFolder(), File.separator + functionName
                        + File.separator
                        + inputName);

                if (!testFolder.isDirectory()) {
                    SpecsLogs.msgInfo(" ->Skipping compilation for '" + functionName + File.separator
                            + inputName + "'");
                    continue;
                }

                // Get all the input files
                List<File> inputFiles = SpecsIo.getFilesRecursive(testFolder, CFilesUtils.C_EXTENSIONS);

                List<String> inputFilesPathes = SpecsFactory.newArrayList();
                for (File inputFile : inputFiles) {
                    // TODO: Is it necessary to add "", for paths with spaces?
                    String relativePath = SpecsIo.getRelativePath(inputFile, baseOutputFolder);
                    inputFilesPathes.add("\"" + relativePath + "\"");
                }

                // If using BLAS, add BLAS includes
                addBlasIncludes(matisse, args);

                // Create the compiler job and add it to the list
                String outputFoldername = data.outputFolders.getBaseOutputFolder().getAbsolutePath();

                String optimizationFlag = data.compilerOptions.getOptimizationFlag();
                List<String> compilerFlags = data.compilerOptions.getCompilerFlags();
                // Job compilerJob = new Job(compiler, outputFile.getAbsolutePath(), inputFilesPathes,
                // optimizationFlag, compilerFlags, "-o", outputFoldername);

                args.addAll(compilerFlags);
                if (isLinux) {
                    args.addAll(data.compilerOptions.getLinuxOnlyCompilerFlags());
                }
                args.add("-D_FILE_OFFSET_BITS");
                args.addAll(inputFilesPathes);
                args.add(optimizationFlag);
                args.add("-lm");
                args.addAll(data.compilerOptions.getLinkerFlags());
                args.add("-o");
                String relativeOutput = SpecsIo.getRelativePath(outputFile, baseOutputFolder);
                // args.add("\"" + outputFile.getAbsolutePath() + "\"");
                args.add("\"" + relativeOutput + "\"");

                // Add BLAS library, after files (otherwise linked might not find it)
                addBlasLib(matisse, args);

                Job compilerJob = Job.singleProgram(args, outputFoldername);

                compilerJobs.add(compilerJob);
            }
        }

        return compilerJobs;
    }

    private static void addBlasLib(DataStore matisse, List<String> args) {

        if (MatlabToCKeys.isActive(matisse, MatisseOptimization.UseBlas)) {
            String blasLibraryFolder = matisse.get(MatlabToCKeys.BLAS_LIB_FOLDER);
            if (blasLibraryFolder.isEmpty()) {
                SpecsLogs
                        .msgInfo(" !Optimization '"
                                + MatisseOptimization.UseBlas
                                + "' is active, but BLAS library folder is not correctly defined, compilation might fail.");

                return;
            }

            // Add folder
            args.add("-L\"" + blasLibraryFolder + "\"");

            // Get all .a files
            List<File> files = SpecsIo.getFiles(new File(blasLibraryFolder), "a");

            for (File file : files) {
                String name = file.getName();
                if (!name.startsWith("lib")) {
                    continue;
                }

                name = name.substring("lib".length());

                name = SpecsIo.removeExtension(name);

                args.add("-l" + name);

            }

        }
    }

    private static void addBlasIncludes(DataStore matisse, List<String> args) {

        if (MatlabToCKeys.isActive(matisse, MatisseOptimization.UseBlas)) {
            String blasIncludeFolder = matisse.get(MatlabToCKeys.BLAS_INCLUDE_FOLDER);

            // Check if folder is valid
            if (blasIncludeFolder.isEmpty()) {
                SpecsLogs
                        .msgInfo(" !Optimization '"
                                + MatisseOptimization.UseBlas
                                + "' is active, but BLAS include folder is not correctly defined, compilation might fail.");
                return;
            }

            // Add folder
            args.add("-I\"" + blasIncludeFolder + "\"");

            // Get all .h files
            // List<File> files = IoUtils.getFiles(new File(blasIncludeFolder), "h");
            // files.forEach((file) -> inputFilesPathes.add("\"" + IoUtils.getPath(file) + "\""));
        }
    }

    private static void runJobsInBatch(File outputFolder, File batchFile) {

        // Build command
        List<String> command = SpecsFactory.newArrayList();
        command.add("cmd");
        command.add("/C");
        command.add(batchFile.getAbsolutePath());

        // Run batch file
        // try {
        long tic = System.nanoTime();
        // ProcessUtils.runProcess(command, outputFolder.getAbsolutePath());
        SpecsSystem.run(command, outputFolder);
        long toc = System.nanoTime();

        long nanos = (toc - tic);
        String compilationTime = SpecsStrings.parseTime(nanos);

        SpecsLogs.msgInfo("... Compilation took " + compilationTime);
    }

    private File buildBatchFile(List<TestableFunction> functionsToTest, File outputFolder) {
        List<Job> compilerJobs = createCompilerJobs(functionsToTest, false);

        SpecsLogs.msgInfo("Generating compilation scripts with " + compilerJobs.size() + " compilation jobs");

        StringBuilder builder = new StringBuilder();
        int interval = 10;

        builder.append("@echo off\n");
        int counter = 0;
        for (Job job : compilerJobs) {
            counter += 1;
            builder.append(job.getCommandString());
            builder.append("\n");

            if (counter % interval == 0) {
                builder.append("echo " + counter + " jobs completed\n");
            }
        }

        if (counter % interval != 0) {
            builder.append("echo " + counter + " jobs completed\n");
        }

        File batchFile = new File(outputFolder, "compilation_jobs.bat");
        SpecsIo.write(batchFile, builder.toString());
        return batchFile;
    }

    private static void runJobsInBash(File outputFolder, File batchFile) {
        // Build command
        List<String> command = SpecsFactory.newArrayList();
        command.add("sh");
        command.add(batchFile.getAbsolutePath());
        command.add(">");
        command.add("/dev/null");

        // Run batch file
        // try {
        long tic = System.nanoTime();
        // ProcessUtils.runProcess(command, outputFolder.getAbsolutePath());
        SpecsSystem.run(command, outputFolder);
        long toc = System.nanoTime();

        long nanos = (toc - tic);
        String compilationTime = SpecsStrings.parseTime(nanos);

        SpecsLogs.msgInfo("... Compilation took: " + compilationTime);
    }

    private File buildBashFile(List<TestableFunction> functionsToTest, File outputFolder) {
        List<Job> compilerJobs = createCompilerJobs(functionsToTest, true);

        // Create batch files
        StringBuilder builder = new StringBuilder();
        int interval = 10;

        int counter = 0;
        for (Job job : compilerJobs) {
            counter += 1;
            builder.append(job.getCommandString());
            builder.append("\n");

            if (counter % interval == 0) {
                builder.append("echo " + counter + " jobs completed\n");
            }
        }

        if (counter % interval != 0) {
            builder.append("echo " + counter + " jobs completed\n");
        }

        File batchFile = new File(outputFolder, "compilation_jobs.sh");
        SpecsIo.write(batchFile, builder.toString());
        return batchFile;
    }
}
