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

import static org.specs.MatlabIR.MatlabCodeUtils.addPath;
import static org.specs.MatlabIR.MatlabCodeUtils.rmPath;

import java.io.File;
import java.util.List;

import org.specs.MatlabIR.MatlabCodeUtils;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.MatlabToC.Functions.MatissePrimitives.CompatibilityPackageResource;
import org.specs.MatlabToCTester.MatlabToCTesterUtils;
import org.specs.MatlabToCTester.Auxiliary.InputFolders;
import org.specs.MatlabToCTester.Auxiliary.MatlabCodeBuilder;
import org.specs.MatlabToCTester.Auxiliary.MatlabOptions;
import org.specs.MatlabToCTester.Auxiliary.OutputFolders;
import org.specs.MatlabToCTester.Auxiliary.TestableFunction;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabScriptsUtils {

    private static final String COMPATIBILITY_PACKAGE_FOLDERNAME = "compatibility_package";

    /**
     * Generates all the scripts needed to test the C output against the matlab output.
     * 
     * 
     * @param matlabSourceFilesDirectory
     * @param matlabInputFilesDirectory
     * @param cOutputsDirectory
     * @param matlabScriptsDirectory
     * @param absEpsilon
     */
    public static void createMatlabScripts(LanguageMode languageMode,
            List<TestableFunction> functionsToTest, MatlabOptions matlabOptions,
            InputFolders inputFolders, OutputFolders outputFolders,
            boolean combinedAuxiliaryFolders) {

        int totalTests = 0;

        // Iterate over the source files
        for (TestableFunction functionToTest : functionsToTest) {

            // Increment counter
            totalTests += functionToTest.getInputNames().size();

            FunctionDeclarationSt functionDeclaration = MatlabProcessorUtils.getFunctionDeclaration(
                    functionToTest.getFunctionToTest(), languageMode).get();
            // FunctionDeclarationInfo declaration =
            // MatlabProcessorUtils.parseMFunctionDeclaration(functionToTest
            // .getFunctionToTest());

            // Iterate over the input files for this particular source file
            int currentInput = 0;
            int totalInputs = functionToTest.getInputVectors().size();

            for (int i = 0; i < functionToTest.getInputNames().size(); i++) {
                File inputFile = functionToTest.getInputVectors().get(i);
                List<String> testInputs = functionToTest.getTestInputs().get(i);

                currentInput += 1;

                // The C output file for this test
                // String functionName = declaration.getFunctionName();
                String functionName = functionDeclaration.getFunctionName();
                String testName = SpecsIo.removeExtension(inputFile.getName());
                File cTestCaseOuputDirectory = SpecsIo.mkdir(outputFolders.getcOutputsFolder().getAbsolutePath()
                        + File.separator + functionName + File.separator + testName);

                // Check if folder exists
                if (SpecsIo.getFiles(cTestCaseOuputDirectory).isEmpty()) {
                    SpecsLogs.msgInfo(" -> Error, could not find executables for for '" + functionName + "_"
                            + testName + "'");
                    continue;
                }

                // The script file
                String inputFileName = SpecsIo.removeExtension(inputFile.getName());
                inputFileName = inputFileName.concat(".m");
                // String newScriptName = declaration.getFunctionName() + "_" + inputFileName;
                String newScriptName = functionName + "_" + inputFileName;
                File scriptFile = new File(outputFolders.getMatlabScriptsFolder(), newScriptName);

                // Create the code
                String scriptFunctionName = SpecsIo.removeExtension(scriptFile.getName());
                String scriptCode = generateTestCaseScriptCodeV2(functionDeclaration, inputFolders, inputFile,
                        testInputs,
                        cTestCaseOuputDirectory, matlabOptions, scriptFunctionName,
                        combinedAuxiliaryFolders,
                        functionToTest.hasAuxiliaryFiles(),
                        currentInput, totalInputs);

                // Write the script file to the disk
                SpecsIo.write(scriptFile, scriptCode);

            }
        }

        // The main script file
        String mainScriptName = MatlabToCTesterUtils.getNameMainMatlabScript() + ".m";
        File mainMatlabScript = new File(outputFolders.getMatlabScriptsFolder(), mainScriptName);

        // Generate the code and write it to the disk
        // String mainMatlabScriptCode = generateMainMatlabScriptCode(functionsToTest, matlabOptions);
        String mainMatlabScriptCode = generateMainMatlabScriptCodeV2(functionsToTest, matlabOptions, totalTests,
                outputFolders.getMatlabScriptsFolder());

        SpecsIo.write(mainMatlabScript, mainMatlabScriptCode);

        // Copy the script file with the comparison function 'are_equal'
        // from the resources to the folder with the scripts
        SpecsIo.resourceCopyWithName("matlabCode/are_equal.m", "are_equal.m", outputFolders.getMatlabScriptsFolder());

    }

    /**
     * Creates a single test case script.
     * 
     * @param declaration
     * @param testInputs
     * @param sourceFolder
     * @param inputsFolder
     * @param inputFileName
     * @param cTestCaseOuputFolder
     * @param absEpsilon
     * @param scriptName
     * @param totalInputs
     * @param currentInput
     * @return
     */
    // private static String generateTestCaseScriptCodeV2(FunctionDeclarationInfo declaration, InputFolders
    // inputFolders,
    private static String generateTestCaseScriptCodeV2(FunctionDeclarationSt declaration, InputFolders inputFolders,
            File inputVectorFile, List<String> testInputs, File cTestCaseOuputFolder, MatlabOptions matlabOptions,
            String scriptName, boolean combinedAuxiliaryFolder, boolean isAuxiliary, int currentInput,
            int totalInputs) {

        String body = SpecsIo.getResource(ScriptResource.TEST_SCRIPT_BODY.getResource());

        String scriptNameTag = "<SCRIPT_NAME>";
        body = body.replace(scriptNameTag, scriptName);

        String addPathsTag = "<ADD_PATHS>";
        File inputVectorFolder = inputVectorFile.getParentFile();
        File sourceFolder = inputFolders.getTestsFolder();

        File auxiliaryFile = null;
        if (isAuxiliary) {
            auxiliaryFile = MatlabToCTesterUtils.getAuxilaryFilesFolder(inputFolders.getAuxiliaryFolder(),
                    declaration.getFunctionName(),
                    combinedAuxiliaryFolder);
        }

        String addPaths = getAddTestPaths(inputVectorFolder, sourceFolder, cTestCaseOuputFolder, auxiliaryFile);

        body = body.replace(addPathsTag, addPaths);

        String relErrorTag = "<REL_ERROR>";
        body = body.replace(relErrorTag, Double.toString(matlabOptions.getRelEpsilon()));

        String absErrorTag = "<ABS_ERROR>";
        body = body.replace(absErrorTag, Double.toString(matlabOptions.getAbsEpsilon()));

        String inputValuesTag = "<INPUT_VALUES>";
        String inputValues = getInputValues(inputVectorFile);
        body = body.replace(inputValuesTag, inputValues);

        // Call the function
        String functionCallTag = "<FUNCTION_CALL>";
        // String functionCall = MatlabCodeBuilder.generateFunctionCall(declaration.getOutputNames(),
        // declaration.getFunctionName(), testInputs);
        String functionCall = MatlabCodeBuilder.generateFunctionCall(declaration.getOutputs().getNames(),
                declaration.getFunctionName(), testInputs);
        body = body.replace(functionCallTag, functionCall);

        String inputNameTag = "<INPUT_NAME>";
        String inputName = inputVectorFile.getName();
        body = body.replace(inputNameTag, inputName);

        // Run the comparison tests for every output variable
        String compTag = "<COMPARISON_TESTS>";

        String inputFileName = inputVectorFile.getName();
        String comp = generateComparissonTestsCodeV2(declaration, inputFileName);
        body = body.replace(compTag, comp);

        String removePathsTag = "<REMOVE_PATHS>";
        String removePaths = getRemoveTestPaths(inputVectorFolder, sourceFolder, cTestCaseOuputFolder, auxiliaryFile);
        body = body.replace(removePathsTag, removePaths);

        String counterTag = "<CURRENT>";
        body = body.replace(counterTag, Integer.toString(currentInput));

        String totalTag = "<TOTAL>";
        body = body.replace(totalTag, Integer.toString(totalInputs));

        return body;
    }

    /**
     * @param inputVectorFile
     * @return
     */
    public static String getInputValues(File inputVectorFile) {
        String inputFileName = inputVectorFile.getName();

        // Check if the input is a .MAT file
        boolean isMAT = SpecsIo.getExtension(inputFileName).equals("mat");
        String inputName = SpecsIo.removeExtension(inputFileName);

        // Call the input script
        if (isMAT) {
            return "load '" + inputName + "';\n";
        }

        return inputName + ";\n";

    }

    private static String getAddTestPaths(File inputVectorFolder, File sourceFolder, File cTestCaseOuputFolder,
            File auxiliaryFile) {

        StringBuilder builder = new StringBuilder();

        builder.append(addPath(inputVectorFolder, "-begin"));
        builder.append(addPath(sourceFolder, "-begin"));
        builder.append(addPath(cTestCaseOuputFolder, "-begin"));

        if (auxiliaryFile != null) {
            builder.append(addPath(auxiliaryFile, "-begin"));
        }

        return builder.toString();
    }

    private static String getRemoveTestPaths(File inputVectorFolder, File sourceFolder, File cTestCaseOuputFolder,
            File auxiliaryFile) {

        StringBuilder builder = new StringBuilder();

        builder.append(rmPath(inputVectorFolder));
        builder.append(rmPath(sourceFolder));
        builder.append(rmPath(cTestCaseOuputFolder));

        if (auxiliaryFile != null) {
            builder.append(rmPath(auxiliaryFile));
        }

        return builder.toString();
    }

    private static String generateComparissonTestsCodeV2(FunctionDeclarationSt declaration, String inputName) {

        StringBuilder builder = new StringBuilder();
        String body = SpecsIo.getResource(ScriptResource.TEST_SCRIPT_COMPARE.getResource());

        // for (String varName : declaration.getOutputNames()) {
        for (String varName : declaration.getOutputs().getNames()) {
            String varNameTag = "<VAR_NAME>";
            builder.append(body.replace(varNameTag, varName));
        }

        return builder.toString();
    }

    /**
     * Generates the code for the main matlab script. This script is responsible for calling all the other scripts and
     * for showing the results if the tests.
     * 
     * @param scriptStructures
     * @param matlabScriptsFolder
     * @return
     */
    private static String generateMainMatlabScriptCodeV2(List<TestableFunction> scriptStructures,
            MatlabOptions matlabOptions, int numTotalTests, File matlabScriptsFolder) {

        // String body = IoUtils.getResourceString(ScriptResource.MAIN_SCRIPT_BODY.getResource());
        Replacer body = new Replacer(SpecsIo.getResource(ScriptResource.MAIN_SCRIPT_BODY.getResource()));
        // String mainPathsTag = "<MAIN_PATHS>";

        // Prepare compatibility package for MATISSE primitives
        prepareCompatibilityPackage(matlabScriptsFolder, body);

        // Build paths to add
        // String mainPaths = addPath(MatlabToCUtils.getLibraryFolder(), "-end");

        // body = body.replace(mainPathsTag, mainPaths);

        int functionCounter = 1;
        int totalFunctions = scriptStructures.size();

        // Build tests
        StringBuilder runTests = new StringBuilder();
        for (TestableFunction scriptStructure : scriptStructures) {
            String runtest = generateTestFunction(scriptStructure, matlabOptions, functionCounter, totalFunctions);
            runTests.append(runtest);

            // Update counter
            functionCounter += 1;
        }

        body = body.replace("<TEST_FUNCTIONS>", runTests.toString());
        body = body.replace("<TOTAL_TESTS>", Integer.toString(numTotalTests));

        return body.toString();
    }

    private static void prepareCompatibilityPackage(File matlabScriptsFolder, Replacer body) {
        // Create MATISSE primitives compatibility folder
        File compatibilityFolder = SpecsIo.mkdir(matlabScriptsFolder,
                MatlabScriptsUtils.COMPATIBILITY_PACKAGE_FOLDERNAME);

        // Copy compatibility functions to folder
        for (ResourceProvider mfile : CompatibilityPackageResource.values()) {
            SpecsIo.resourceCopy(mfile.getResource(), compatibilityFolder, false);
        }

        // Add path to compatibility package
        // String compatibilityAddpath = "addpath '" + compatibilityFolder.getAbsolutePath() + "' -begin";
        // body.replace("<COMPABILITY_PACKAGE_PATH>", compatibilityAddpath);
        body.replace("<COMPABILITY_PACKAGE_PATH>", MatlabCodeUtils.makeString(compatibilityFolder.getAbsolutePath()));
    }

    /**
     * @param scriptStructure
     * @param matlabOptions
     * @param functionCounter
     * @param totalFunctions
     * @return
     */
    private static String generateTestFunction(TestableFunction scriptStructure, MatlabOptions matlabOptions,
            int functionCounter, int totalFunctions) {

        String body = SpecsIo.getResource(ScriptResource.MAIN_SCRIPT_TESTFUNCTION.getResource());

        // Tags
        String runtTestsTag = "<RUN_TESTS>";
        String functionNameTag = "<FUNCTION_NAME>";
        String currentTag = "<CURRENT>";
        String totalTag = "<TOTAL>";

        StringBuilder runTests = new StringBuilder();
        String functionName = scriptStructure.getFunctionName();

        // Build paths to add
        for (String inputName : scriptStructure.getInputNames()) {
            String runTest = generateRunTest(functionName, inputName, matlabOptions);
            runTests.append(runTest);
        }

        body = SpecsStrings.parseTemplate(body, null, runtTestsTag, runTests.toString(), functionNameTag, functionName,
                currentTag, Integer.toString(functionCounter), totalTag, Integer.toString(totalFunctions));

        return body;
    }

    /**
     * @param matlabOptions
     * @param testName
     * @param functionName
     * @return
     */
    public static String generateRunTest(String functionName, String testName, MatlabOptions matlabOptions) {
        String body = SpecsIo.getResource(ScriptResource.MAIN_SCRIPT_RUNTEST.getResource());

        // Tags
        String functionNameTag = "<FUNCTION_NAME>";
        String testNameTag = "<TEST_NAME>";
        String relErrorTag = "<REL_ERROR>";
        String absErrorTag = "<ABS_ERROR>";

        body = SpecsStrings.parseTemplate(body, null, functionNameTag, functionName, testNameTag, testName, relErrorTag,
                Double.toString(matlabOptions.getRelEpsilon()), absErrorTag,
                Double.toString(matlabOptions.getAbsEpsilon()));

        return body;
    }

}
