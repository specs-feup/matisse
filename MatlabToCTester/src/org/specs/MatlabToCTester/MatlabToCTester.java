/*
 * Copyright 2012 SPeCS.
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

package org.specs.MatlabToCTester;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.MatlabToCTester.Auxiliary.InputFolders;
import org.specs.MatlabToCTester.Auxiliary.MatlabSession;
import org.specs.MatlabToCTester.Auxiliary.TestableFunction;
import org.specs.MatlabToCTester.Auxiliary.TesterState;
import org.specs.MatlabToCTester.FileOperations.CFilesUtils;
import org.specs.MatlabToCTester.FileOperations.ExecutionUtils;
import org.specs.MatlabToCTester.FileOperations.GeneratedCompileScripts;
import org.specs.MatlabToCTester.FileOperations.MatlabScriptsUtils;
import org.specs.MatlabToCTester.Outputs.TestCaseOutput;
import org.specs.MatlabToCTester.Outputs.TestOutput;
import org.specs.MatlabToCTester.Test.MatTester;
import org.specs.MatlabToCTester.Weaver.WeaverEngine;
import org.suikasoft.CMainFunction.Builder.TestMainOptions;
import org.suikasoft.CMainFunction.Builder.TestMainSetup;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.properties.SpecsProperty;

/**
 * Automatically tests the C code that is generated by the MatlabToC application.
 * 
 * @author Pedro Pinto
 */
public class MatlabToCTester {

    // The configuration data
    private final MatlabToCTesterData data;
    // Keeps track if there was a problems in any of the stages
    private boolean problemsOccurred;

    /**
     * 
     * @param data
     */
    public MatlabToCTester(MatlabToCTesterData data) {
        this.data = data;
        problemsOccurred = false;
    }

    public int execute() {
        // At the beginning, there were no problems...
        problemsOccurred = false;
        long tic, toc;

        SpecsProperty.ShowStackTrace.applyProperty("true");
        // SuikaProperty.LoggingLevel.applyProperty("700");
        // Add default options of MatlabToC
        addDefaultOptions();

        // Clean output folder
        if (data.deleteOutputContents) {
            SpecsLogs.msgInfo("Cleaning output folder...");
            SpecsIo.deleteFolderContents(data.outputFolders.getBaseOutputFolder());
        }

        // Weave MATLAB files, if a LARA aspect is present
        if (isLaraFile(data)) {
            SpecsLogs.msgInfo("Weaving MATLAB files...");
        }

        tic = System.nanoTime();
        InputFolders inputFolders = new WeaverEngine(data).execute();
        toc = System.nanoTime();
        if (isLaraFile(data)) {
            SpecsLogs.msgInfo("... Weaving took " + SpecsStrings.parseTime(toc - tic));
        }

        if (data.testerOptions.getTesterState() == TesterState.WeaveMatlabFiles) {
            return done();
        }

        // Generate the C source test files
        SpecsLogs.msgInfo("Generating C source files...");
        tic = System.nanoTime();
        // List<FunctionToTest> functionsToTest = CFilesUtils.createCTests(data.inputFolders, data.outputFolders,
        CFilesUtils matlabToC = new CFilesUtils(data);
        List<TestableFunction> functionsToTest = matlabToC.createCTests(inputFolders);
        toc = System.nanoTime();
        SpecsLogs.msgInfo("... C generation took " + SpecsStrings.parseTime(toc - tic));

        int numTotalTests = matlabToC.getTotalTests();

        // Calculate number of generated programs
        int numCreatedTests = functionsToTest.stream()
                // Get the number of input vectors in each test
                .map(testFunction -> testFunction.getInputVectors().size())
                // Sum all
                .reduce(0, (acc, item) -> acc + item).intValue();

        // System.out.println("TOTAL NUMBER:" + numTotalTests);
        // System.out.println("CREATED NUMBER:" + numCreatedTests);

        if (numTotalTests != numCreatedTests) {
            reportProblem("There was a total of " + numTotalTests + ", but could only create C code for "
                    + numCreatedTests
                    + " tests.");
        }

        CFilesUtils cFilesUtils = new CFilesUtils(data);

        GeneratedCompileScripts compileScripts = cFilesUtils.createCompilationScripts(functionsToTest);

        if (data.testerOptions.getTesterState() == TesterState.GenerateCSources) {
            return done();
        }

        // Check if there is anything to compile
        if (functionsToTest.size() == 0) {
            SpecsLogs.msgInfo("No functions to compile, stopping execution.");
            return done();
        }

        SpecsLogs.msgInfo("Copying resource files");

        for (TestableFunction function : functionsToTest) {
            for (String inputName : function.getInputNames()) {
                File executableFolder = MatlabToCTesterUtils.getTestExecutablePath(data, function.getFunctionName(),
                        inputName);

                if (data.inputFolders.getResourceFolder() != null) {
                    SpecsIo.copyFolder(data.inputFolders.getResourceFolder(), executableFolder, true);
                }
            }
        }

        if (data.testerOptions.getTesterState() == TesterState.CopyResources) {
            return done();
        }

        // Generate the binaries for each test file
        cFilesUtils.createExecutableBinaries(compileScripts);

        // Check if there are as many .exe files as there are test vectors
        int numExecutableFiles = getExecutableFiles(data.testerOptions.getRunOnlyOneTest()).size();
        if (numCreatedTests != numExecutableFiles) {
            reportProblem("There were " + numCreatedTests + " tests, but could only generate " + numExecutableFiles
                    + " executables.");
        }

        if (data.testerOptions.getTesterState() == TesterState.Compile) {
            return done();
        }

        TestMainOptions mainOptions = TestMainSetup.newData(data.mainOptionsData);
        // Can only proceed if option for return outputs is disabled
        /*
        if (mainOptions.returnOutputs) {
        
            LoggingUtils.msgWarn(" -> Option for main() '" + TestMainSetup.PrintExecutionTime.getParsedString()
        	    + "' is enabled. Cannot proceed");
            return done();
        }
        */

        // Run all the binaries and catch their output
        SpecsLogs.msgInfo("Running C binaries...");
        // MemoryLayout memLayout = data.setup.getMultipleChoice(CirOption.MEMORY_LAYOUT, MemoryLayout.class);
        // List<TestOutput> executedBinaries = ExecutionUtils.runBinaries(data.outputFolders, data.setup.getSetup());
        List<TestOutput> executedBinaries = ExecutionUtils.runBinaries(data.outputFolders, data.getSettings(),
                data.testerOptions.getRunOnlyOneTest(),
                !mainOptions.addCodeToMeasureExecution);

        int numExecutedBinaries = executedBinaries.stream()
                .map(output -> output.getTestCaseOutputs().size())
                .reduce(0, (acc, item) -> acc + item).intValue();

        if (numExecutableFiles != numExecutedBinaries) {
            reportProblem("There were " + numExecutableFiles + " executables, but could only execute "
                    + numExecutedBinaries + ".");
        }

        if (data.testerOptions.getTesterState() == TesterState.RunCompiledFiles) {
            return done();
        }

        // Can only proceed if option for printing outputs is enable
        if (!mainOptions.printOutputs) {
            SpecsLogs.warn(" -> Option for main() '" + TestMainSetup.PrintExecutionTime.getParsedString()
                    + "' is enabled. Cannot save outputs to a MAT file, finishing");
            return done();
        }

        // Write the C outputs as matlab data files
        SpecsLogs.msgInfo("Saving C output as a MAT file...");
        ExecutionUtils.writeCOutputs(executedBinaries, data.outputFolders);

        // Get number of cOutput.mat files
        File cOutputsFolder = data.outputFolders.getcOutputsFolder();
        if (data.testerOptions.getRunOnlyOneTest() != null) {
            cOutputsFolder = new File(cOutputsFolder, data.testerOptions.getRunOnlyOneTest());
        }
        long numberOfCOutputs = SpecsIo.getFilesRecursive(cOutputsFolder).stream()
                .filter(file -> file.getName().equals(ExecutionUtils.getFilenameOutputMat()))
                .count();

        if (numExecutedBinaries != numberOfCOutputs) {
            reportProblem("The number of executed programs was  " + numExecutedBinaries + ", but could only generate "
                    + numberOfCOutputs + " outputs.");
        }

        if (data.testerOptions.getTesterState() == TesterState.SaveOutputToMat) {
            return done();
        }

        // Remove tests that could not run
        functionsToTest = removeUnparsedTests(functionsToTest, executedBinaries,
                data.inputFolders.getAuxiliaryFolder(), data.testerOptions.isCombinedAuxiliaryFolders());

        if (data.matlabOptions.testWithMatlab()) {
            /*
             * Generate matlab script files that will run
             * the functions to test with the corresponding
             * inputs and will compare the outputs
             * to the C executable binaries outputs
             */
            MatlabScriptsUtils.createMatlabScripts(data.languageMode, functionsToTest, data.matlabOptions,
                    data.inputFolders,
                    data.outputFolders,
                    data.testerOptions.isCombinedAuxiliaryFolders());

            // Run the matlab scripts if the user wants it
            if (data.testerOptions.getTesterState() == TesterState.CreateMatlabScripts) {
                return done();
            }

            SpecsLogs.msgInfo("Launching MATLAB...");
            // MatlabSession matlabSession = MatlabSession.newInstance();
            MatlabSession matlabSession = MatlabSession.getGlobalSession();
            if (matlabSession == null) {
                SpecsLogs.msgInfo("Could not execute MATLAB script.");
                problemsOccurred = true;
            } else {
                boolean success = matlabSession.runScript(data.outputFolders.getMatlabScriptsFolder(),
                        MatlabToCTesterUtils.getNameMainMatlabScript());

                if (!success) {
                    SpecsLogs.msgInfo("Problems while running MATLAB tests.");
                    problemsOccurred = true;
                }
            }
        } else {
            SpecsLogs.msgInfo("Testing outputs...");
            MatTester matTest = new MatTester(functionsToTest, data);
            int failedVectors = matTest.test();

            if (failedVectors != 0) {
                reportProblem("Problems while testing outputs.");
            }
        }

        // ExecutionUtils.runMatlabScript(data.outputFolders.getMatlabScriptsFolder(),
        // MatlabToCTesterUtils.getNameMainMatlabScript());

        return done();
    }

    private List<File> getExecutableFiles(String runOnlyOneTest) {
        File executablesFolder = data.outputFolders.getcExecutablesFolder();

        if (runOnlyOneTest != null) {
            executablesFolder = new File(executablesFolder, runOnlyOneTest);
        }

        return SpecsIo.getFilesRecursive(executablesFolder)
                .stream()
                .filter(MatlabToCTester::isExecutableFile)
                .collect(Collectors.toList());
    }

    private static boolean isExecutableFile(File file) {
        String name = file.getName();
        int dotSeparator = name.lastIndexOf('.');

        return dotSeparator == -1 || // Linux targets
                name.substring(dotSeparator).equals(".exe"); // Windows targets
    }

    private void reportProblem(String message) {
        problemsOccurred = true;
        SpecsLogs.msgInfo(" ! " + message);
    }

    private void addDefaultOptions() {
        DataStore defaultSettings = MatlabToCOptionUtils.newDefaultSettings();
        DataStore currentSettings = data.getSettings();
        Set<String> currentKeysWithValues = new HashSet<>(currentSettings.getKeysWithValues());

        for (String defaultKeyWithValue : defaultSettings.getKeysWithValues()) {
            if (currentKeysWithValues.contains(defaultKeyWithValue)) {
                continue;
            }
            // System.out.println("ADDING:" + defaultKeyWithValue + " -> " + defaultSettings.get(defaultKeyWithValue));
            currentSettings.setRaw(defaultKeyWithValue, defaultSettings.get(defaultKeyWithValue));
        }

        /*
        // Build set with current keys
        Set<String> currentKeys = new HashSet<>(data.getGeneralSetup().getKeys());
        
        // Add default options for keys that are not present
        MatisseSetup defaultSetup = MatlabToCOptionUtils.newDefaultSetup();
        for (Option option : defaultSetup.getOptions()) {
            // If option already in the setup, ignore
            if (currentKeys.contains(option.getDefinition().getName())) {
                continue;
            }
        
            // Add option
            data.getGeneralSetup().setOption(option);
        }
        */

    }

    /**
     * @param data2
     * @return
     */
    private static boolean isLaraFile(MatlabToCTesterData data) {
        if (data.inputFolders.getAspectsFiles().size() != 1) {
            return false;
        }

        if (!SpecsIo.getExtension(data.inputFolders.getAspectsFiles().get(0)).equals("lara")) {
            return false;
        }

        return true;
    }

    /**
     * @return
     */
    private int done() {
        if (!problemsOccurred) {
            return 0;
        }

        return 1;
    }

    /**
     * @param functionsToTest
     * @param executedBinaries
     * @return
     */
    private static List<TestableFunction> removeUnparsedTests(List<TestableFunction> functionsToTest,
            List<TestOutput> executedBinaries, File auxiliaryFolder, boolean combinedAuxiliaryFolder) {

        // Create map for easier accessing
        Map<String, TestOutput> outputsMap = SpecsFactory.newHashMap();
        for (TestOutput testOutput : executedBinaries) {
            outputsMap.put(testOutput.getTestName(), testOutput);
        }

        List<TestableFunction> newFunctionsToTest = SpecsFactory.newArrayList();

        for (TestableFunction functionToTest : functionsToTest) {
            // Get corresponding test output
            TestOutput testOutput = outputsMap.get(functionToTest.getFunctionName());
            if (testOutput == null) {
                continue;
                // throw new RuntimeException("Could not find output for function '"
                // + functionToTest.getFunctionName() + "'");
            }

            // Create map for easier accessing
            Map<String, TestCaseOutput> testCasesMap = SpecsFactory.newHashMap();
            for (TestCaseOutput testCase : testOutput.getTestCaseOutputs()) {
                testCasesMap.put(testCase.getInputName(), testCase);

            }

            List<File> inputVectors = SpecsFactory.newArrayList();
            List<List<String>> testInputs = SpecsFactory.newArrayList();
            // for (File inputVector : functionToTest.getInputVectors()) {
            for (int i = 0; i < functionToTest.getInputVectors().size(); i++) {
                File inputVector = functionToTest.getInputVectors().get(i);
                String inputName = SpecsIo.removeExtension(inputVector.getName());

                // Get corresponding test input
                TestCaseOutput testCaseOutput = testCasesMap.get(inputName);
                if (testCaseOutput == null) {
                    continue;
                    // throw new RuntimeException("Could not find output test for test '"
                    // + functionToTest.getFunctionName() + "/" + inputName + "'");
                }

                inputVectors.add(inputVector);
                testInputs.add(functionToTest.getTestInputs().get(i));
            }

            if (inputVectors.isEmpty()) {
                continue;
            }

            TestableFunction newFunctionToTest = new TestableFunction(functionToTest.getFunctionToTest(),
                    inputVectors,
                    testInputs,
                    auxiliaryFolder,
                    combinedAuxiliaryFolder);
            newFunctionsToTest.add(newFunctionToTest);
        }

        return newFunctionsToTest;
    }
}
