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

package org.specs.MatlabToCTester.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToCTester.MatlabToCTesterData;
import org.specs.MatlabToCTester.Auxiliary.TestableFunction;
import org.specs.MatlabToCTester.FileOperations.ExecutionUtils;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.utilities.ProgressCounter;

public class MatTester {

    private final List<TestableFunction> functionsToTest;
    private final MatlabToCTesterData data;

    public MatTester(List<TestableFunction> functionsToTest, MatlabToCTesterData data) {
        this.functionsToTest = functionsToTest;
        this.data = data;
    }

    public int test() {

        ProgressCounter functionsCounter = new ProgressCounter(functionsToTest.size());

        // Test each function
        int failedVectors = 0;
        for (TestableFunction function : functionsToTest) {
            SpecsLogs.msgInfo("Function '" + function.getFunctionName() + "' " + functionsCounter.next());
            // Test each input vector
            failedVectors += testFunction(function);
        }

        if (failedVectors == 0) {
            SpecsLogs.msgInfo("Passed all tests!");
        } else {
            SpecsLogs.msgInfo("Did not pass " + failedVectors + " input vectors.");
        }

        return failedVectors;
    }

    private int testFunction(TestableFunction function) {
        int numVectors = function.getInputVectors().size();

        // Get output vectors folder for function
        File outputVectorsFolder = new File(data.inputFolders.getOutputVectorsFolder(), function.getFunctionName());

        if (!outputVectorsFolder.isDirectory()) {
            SpecsLogs.msgInfo(" - Skipping testing of function '" + function.getFunctionName()
                    + "' Could not find corresponding output vectors folder ('" + outputVectorsFolder + "')");
            return numVectors;
        }

        File cResultsFolder = new File(data.outputFolders.getcOutputsFolder(), function.getFunctionName());
        if (!cResultsFolder.isDirectory()) {
            SpecsLogs.msgInfo(" - Skipping testing of function '" + function.getFunctionName()
                    + "' Could not find output vectors folder from C");
            return numVectors;
        }

        // Get names of output variables
        File testFile = new File(data.inputFolders.getTestsFolder(), function.getFunctionName() + ".m");
        if (!testFile.isFile()) {
            SpecsLogs.msgInfo(" - Skipping testing of function '" + function.getFunctionName()
                    + "' Could not find original test file");
            return numVectors;
        }

        List<String> outputNames = new MatlabParser().parse(testFile)
                .getMainFunction()
                .getDeclarationNode()
                .getOutputs().getNames();

        int failedVectors = 0;
        // Test each input vector
        ProgressCounter vectorCounter = new ProgressCounter(numVectors);
        for (File vectorFile : function.getInputVectors()) {
            String vectorName = SpecsIo.removeExtension(vectorFile.getName());
            SpecsLogs.msgInfo(" - " + vectorName + " " + vectorCounter.next());

            Optional<File> outputVector = getOutputVector(outputVectorsFolder, vectorName);
            if (!outputVector.isPresent()) {
                continue;
            }

            // C output
            File testOutputFolder = new File(cResultsFolder, vectorName);
            if (!testOutputFolder.isDirectory()) {
                SpecsLogs.msgInfo("  !Skipping input vector '" + vectorName
                        + "' Could not find folder " + testOutputFolder);
                continue;
            }
            File cResult = new File(testOutputFolder, ExecutionUtils.getFilenameOutputMat());
            if (!cResult.isFile()) {
                SpecsLogs.msgInfo("  !Skipping input vector '" + vectorName
                        + "' Could not find C result file");
                // failedVectors++;
                continue;
            }

            int failedVariables = testMat(outputVector.get(), cResult, outputNames);
            if (failedVariables != 0) {
                failedVectors++;
            }
        }

        return failedVectors;
    }

    private static Optional<File> getOutputVector(File outputVectorsFolder, String vectorName) {
        // List files in output vectors folder
        List<File> files = SpecsIo.getFiles(outputVectorsFolder);

        // Return first file that that the name of the vector, excluding the extension
        Optional<File> outputVector = files.stream()
                .filter(file -> SpecsIo.removeExtension(file.getName()).equals(vectorName))
                .findFirst();

        if (!outputVector.isPresent()) {
            SpecsLogs.msgInfo("  !Skipping input vector '" + vectorName
                    + "' Could not find corresponding vector mat file");

            return Optional.empty();
        }

        return outputVector;
    }

    /**
     * 
     * @param expected
     * @param cResult
     * @param variables
     * @return the number of variables that failed the test
     */
    private int testMat(File expected, File cResult, List<String> variables) {
        // Load each variables
        int failed = 0;

        ArrayReader expectedVariables = TestUtils.getReader(expected, data.getSettings());
        ArrayReader cResultVariables = TestUtils.getReader(cResult, data.getSettings());

        for (String variable : variables) {
            String cVariable = variable + "_C";

            // Optional<MLArray> expectedVar = MatUtils.read(expected, variable);
            // Optional<ArrayResult> expectedVar = TestUtils.readVariable(expected, variable);
            Optional<ArrayResult> expectedVar = expectedVariables.getVariable(variable);
            if (!expectedVar.isPresent()) {
                SpecsLogs.msgInfo(" - Skipping output varialbe '" + variable
                        + "' Could not find variable in output vector mat file");
                failed++;
                continue;
            }

            Optional<ArrayResult> cResultVar = cResultVariables.getVariable(cVariable);
            if (!cResultVar.isPresent()) {
                SpecsLogs.msgInfo(" - Skipping output varialbe '" + variable
                        + "' Could not find variable in C result file. List of variables: "
                        + cResultVariables.getVariableNames());
                failed++;
                continue;
            }

            // Check if there is a minimum threshold
            // Optional<Double> lowThreshold = TestUtils.readDouble(expected, variable + "_low_threshold");
            Optional<Double> lowThreshold = expectedVariables.getLowThreshold(variable);

            Optional<Map<Integer, Double>> failedMap = areEqual(expectedVar.get(), cResultVar.get(), lowThreshold);

            if (!failedMap.isPresent()) {
                failed++;
            } else if (!failedMap.get().isEmpty()) {
                failed++;

                SpecsLogs.msgInfo(" !" + variable + ": C values different from expected values");
                File errorFile = new File(cResult.getParent(), variable + "_errors.txt");
                SpecsIo.write(errorFile, formatMap(variable, failedMap.get()));
            }

        }

        return failed;
    }

    private static String formatMap(String variable, Map<Integer, Double> map) {
        StringJoiner joiner = new StringJoiner("\n");
        for (Integer key : map.keySet()) {
            // Use MATLAB indexes
            joiner.add(variable + "_error(" + (key + 1) + ") = " + map.get(key));
        }

        return joiner.toString();
    }

    private Optional<Map<Integer, Double>> areEqual(ArrayResult expected, ArrayResult result,
            Optional<Double> lowThreshold) {
        // Check sizes
        if (!expected.getDimensions().equals(result.getDimensions())) {
            SpecsLogs.msgInfo(" !" + expected.getName() + ": Array size mismatch, expected '"
                    + expected.getDimensions() + "', found '" + result.getDimensions() + "'");
            return Optional.empty();
        }

        // Calculate harmonic mean of expected values, for values diff than zero
        double hmean = calcHarmonicMean(expected);

        // Calculate absolute error
        int numElements = expected.getNumElements();
        double[] err = new double[numElements];
        for (int i = 0; i < numElements; i++) {
            err[i] = Math.abs(result.getDouble(i) - expected.getDouble(i));
        }

        // Get minimum threshold, if present
        double minimumValue = -1;
        if (lowThreshold.isPresent()) {
            minimumValue = lowThreshold.get();
        }

        // Compare each expected value
        Map<Integer, Double> failedMap = new HashMap<>();
        for (int i = 0; i < numElements; i++) {
            // If expected value below minimum, ignore value
            if (lowThreshold.isPresent()) {
                if (Math.abs(expected.getDouble(i)) < minimumValue) {
                    continue;
                }
            }

            // If expected value is zero, use absolute error
            if (expected.getDouble(i) == 0.0) {
                boolean pass = err[i] < data.matlabOptions.getAbsEpsilon();
                if (!pass) {
                    failedMap.put(i, err[i]);
                }

                continue;
            }

            // Use relative error
            boolean pass = err[i] / expected.getDouble(i) < data.matlabOptions.getRelEpsilon();

            // If did not pass, check again with the harmonic mean
            if (!pass) {
                boolean pass2 = err[i] < hmean * data.matlabOptions.getRelEpsilon();
                if (!pass2) {
                    failedMap.put(i, err[i]);
                }
            }
        }

        return Optional.of(failedMap);
    }

    private static double calcHarmonicMean(ArrayResult matrix) {
        // Get all values that are diff than zero, and abs them
        double[] values = new double[matrix.getNumElements()];
        int count = 0;
        for (int i = 0; i < matrix.getNumElements(); i++) {

            double value = matrix.getDouble(i);
            if (value == 0.0) {
                continue;
            }

            values[count] = Math.abs(value);
            count++;
        }

        // Calculate harmonic mean
        double total = 0;
        for (int i = 0; i < count; i++) {
            total = total + (1 / values[i]);
        }

        return count / total;
    }
}
