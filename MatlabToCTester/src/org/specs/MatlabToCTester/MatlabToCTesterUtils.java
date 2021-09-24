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

package org.specs.MatlabToCTester;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabToCTesterUtils {

    private final static String M_EXTENSION = "m";
    private final static String MAT_EXTENSION = "mat";

    private static final String MAIN_SCRIPT_NAME = "main";

    /**
     * The name of the MATLAB main script.
     * 
     * @return
     */
    public static String getNameMainMatlabScript() {
        return MatlabToCTesterUtils.MAIN_SCRIPT_NAME;
    }

    /**
     * Returns the test files from the given folder.
     * 
     * <p>
     * (returns all .M files in the given folder)
     * 
     * @param srcFolder
     * @param runOnlyOneTest
     * @return
     */
    public static List<File> getTests(File srcFolder, String runOnlyOneTest) {
        // Get M-files to test
        List<File> testFiles = SpecsIo.getFiles(srcFolder, MatlabToCTesterUtils.M_EXTENSION);
        // System.out.println("SRC FOLDER:" + srcFolder);
        // System.out.println("ONLY ONE:" + runOnlyOneTest);
        // System.out.println("TEST FILES:" + testFiles);
        if (runOnlyOneTest == null) {
            return testFiles;
        }

        // If test ends with .m or .mat, extract folder name
        if (isSingleTestFile(runOnlyOneTest)) {
            int folderEndIndex = runOnlyOneTest.indexOf('/');
            if (folderEndIndex == -1) {
                showTestNotFoundMessage(runOnlyOneTest, testFiles);
                return Collections.emptyList();
            }

            runOnlyOneTest = runOnlyOneTest.substring(0, folderEndIndex);
        }

        for (File testFile : testFiles) {
            String testName = SpecsIo.removeExtension(testFile.getName());
            if (testName.equals(runOnlyOneTest)) {
                return Arrays.asList(testFile);
            }
        }

        showTestNotFoundMessage(runOnlyOneTest, testFiles);
        return Collections.emptyList();
    }

    private static boolean isSingleTestFile(String runOnlyOneTest) {
        return runOnlyOneTest.endsWith("." + MatlabToCTesterUtils.M_EXTENSION)
                || runOnlyOneTest.endsWith("." + MatlabToCTesterUtils.MAT_EXTENSION);
    }

    private static void showTestNotFoundMessage(String runOnlyOneTest, List<File> testFiles) {
        // Could not find test file, warn user
        SpecsLogs.msgInfo(" -> Could not find test '" + runOnlyOneTest + "' in source folder.");
        SpecsLogs.msgInfo(" Available tests:");
        Collections.sort(testFiles);
        for (File testFile : testFiles) {
            String testName = SpecsIo.removeExtension(testFile.getName());
            SpecsLogs.msgInfo("   " + testName);
        }
    }

    /**
     * Returns the input vectors files from the given folder.
     * 
     * <p>
     * (returns all .M and .MAT files in the given folder)
     * 
     * @param srcFolder
     * @return
     */
    public static List<File> getInputVectors(File inputVectorsFolder, Optional<String> testFile) {

        if (inputVectorsFolder == null) {
            return Collections.emptyList();
        }

        // If test files exists, return only the input test corresponding to the file
        if (testFile.isPresent() && isSingleTestFile(testFile.get())) {
            String testFileName = getTestFileName(testFile.get());

            Optional<File> file = SpecsIo.getFiles(inputVectorsFolder).stream()
                    .filter(afile -> afile.getName().equals(testFileName))
                    .findFirst();

            if (!file.isPresent()) {
                throw new RuntimeException("Could not find input '" + testFileName + "' in '" + testFile.get() + "'");
            }

            return Arrays.asList(file.get());
        }

        List<File> testFiles = SpecsFactory.newArrayList();

        // Get M-files with input vectors
        testFiles.addAll(SpecsIo.getFiles(inputVectorsFolder, MatlabToCTesterUtils.M_EXTENSION));

        // Get MAT-files with input vectors
        testFiles.addAll(SpecsIo.getFiles(inputVectorsFolder, MatlabToCTesterUtils.MAT_EXTENSION));

        return testFiles;
    }

    private static String getTestFileName(String singleTestFile) {
        int folderEndIndex = singleTestFile.lastIndexOf('/');
        if (folderEndIndex == -1) {
            throw new RuntimeException("Could not find the name of the test in '" + singleTestFile + "'");
        }

        String name = singleTestFile.substring(folderEndIndex + 1);

        if (name.isEmpty()) {
            throw new RuntimeException("Found test name, but could not find input name in '" + singleTestFile + "'");
        }

        return name;

    }

    /**
     * Maps each input vector folder to the name of the folder.
     * 
     * @param inputVectorsBaseFolder
     * @param runOnlyOneTest
     * @return
     */
    // public static Map<String, File> getInputVectorsMap(File inputVectorsBaseFolder, Optional<String> runOnlyOneTest)
    // {
    public static Map<String, File> getInputVectorsMap(File inputVectorsBaseFolder) {
        List<File> inputVectorsFolders = SpecsIo.getFolders(inputVectorsBaseFolder);

        /*
        	// Check if present, and if ends with a .m or .mat
        	Optional<String> inputName = Optional.empty();
        	if (runOnlyOneTest.isPresent()) {
        	    String testName = runOnlyOneTest.get();
        	    if (testName.endsWith(MAT_EXTENSION) || testName.endsWith(M_EXTENSION)) {
        		int lastSeparatorIndex = IoUtils.normalizePath(testName).lastIndexOf(IoUtils.getFolderSeparator());
        		Preconditions.checkArgument(lastSeparatorIndex != -1);
        		inputName = Optional.of(testName.substring(lastSeparatorIndex + 1));
        	    }
        	}
        */
        Map<String, File> inputVectorsMap = SpecsFactory.newHashMap();
        for (File inputVectorsFolder : inputVectorsFolders) {

            inputVectorsMap.put(inputVectorsFolder.getName(), inputVectorsFolder);
        }

        return inputVectorsMap;
    }

    /**
     * @param auxiliaryFolder
     * @param functionName
     * @return
     */
    public static List<File> getAuxiliaryFiles(File auxiliaryFolder, String functionName,
            boolean combinedAuxiliarFolders) {
        File functionAuxFolder = getAuxilaryFilesFolder(auxiliaryFolder, functionName, combinedAuxiliarFolders);

        /* check if auxFolder\function  exists */
        if (!functionAuxFolder.isDirectory()) {
            return Collections.emptyList();
        }

        return SpecsIo.getFiles(functionAuxFolder, "m");
    }

    public static File getAuxilaryFilesFolder(File auxiliaryFolder, String functionName,
            boolean combinedAuxiliarFolders) {

        if (combinedAuxiliarFolders) {
            return auxiliaryFolder;
        }

        return new File(auxiliaryFolder, functionName);
    }

    public static List<File> getInputVectors(File testFunction, Map<String, File> inputVectorsMap) {

        // Get name of function
        String functionFilename = testFunction.getName();
        String functionName = SpecsIo.removeExtension(functionFilename);

        // Get input vectors folder
        File inputVectorsFolder = inputVectorsMap.get(functionName);
        if (inputVectorsFolder == null) {
            SpecsLogs.msgInfo("!Skipping test file '" + functionName
                    + "': could not find corresponding input vectors folder.");
            return Collections.emptyList();
        }

        return MatlabToCTesterUtils.getInputVectors(inputVectorsFolder, Optional.empty());
    }

    public static File getTestExecutablePath(MatlabToCTesterData data, String functionName, String inputName) {
        File functionFile = new File(data.outputFolders.getcExecutablesFolder().getAbsolutePath(),
                functionName);
        return new File(functionFile, inputName);
    }
}
