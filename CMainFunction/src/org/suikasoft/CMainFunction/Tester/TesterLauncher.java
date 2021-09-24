/**
 * Copyright 2013 SPeCS Research Group.
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

package org.suikasoft.CMainFunction.Tester;

import java.io.File;
import java.util.Collections;
import java.util.List;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.csv.BufferedCsvWriter;
import pt.up.fe.specs.util.csv.CsvWriter;
import pt.up.fe.specs.util.utilities.AverageType;
import pt.up.fe.specs.util.utilities.ProgressCounter;

/**
 * @author Joao Bispo
 * @deprecated
 * 
 */
@Deprecated
public class TesterLauncher {

    private static final double DEFAULT_MINIMUM_RUNTIME = 0.1;
    private static final int DEFAULT_NUMBER_REPEATS = 5;

    /**
     * @param args
     */
    public static void main(String[] args) {

        // Standard init
        SpecsSystem.programStandardInit();

        // If not arguments, show help
        if (args.length == 0) {
            SpecsLogs.msgInfo(help());
            return;
        }

        // Check if given path is either a file or a folder
        File filepath = new File(args[0]);

        // File executable = IoUtils.existingFile(args[0]);
        // if (executable == null) {
        if (!filepath.isFile() && !filepath.isDirectory()) {
            SpecsLogs.warn("First argument '" + filepath
                    + "' is neither an existing file or and existing folder.");
            return;
        }

        double minimumRuntime = DEFAULT_MINIMUM_RUNTIME;
        if (args.length > 1) {
            double userValue = Double.parseDouble(args[1]);

            minimumRuntime = userValue;
        }

        int numberOfRepeats = DEFAULT_NUMBER_REPEATS;
        if (args.length > 2) {
            int userValue = SpecsStrings.parseInt(args[2]);

            numberOfRepeats = userValue;
        }

        File bufferFile = new File(filepath.getParentFile(), "csv_results.buffer");
        CsvWriter csvWriter = newCsvFile(numberOfRepeats, bufferFile);
        String csvFilename = null;

        if (filepath.isFile()) {
            csvFilename = SpecsIo.removeExtension(filepath.getName()) + getCsvEpilogue(minimumRuntime);

            ExecutionResults results = KernelUtils.measureExecution(filepath, 0, minimumRuntime, numberOfRepeats,
                    AverageType.GEOMETRIC_MEAN);

            addExecution(results, csvWriter);

            SpecsLogs.msgInfo(results.toString());
        } else {

            // Otherwise, its a folder
            csvFilename = filepath.getName() + getCsvEpilogue(minimumRuntime);

            List<File> executables = SpecsIo.getFilesRecursive(filepath, "exe");

            // Sort executables
            Collections.sort(executables);

            ProgressCounter progress = new ProgressCounter(executables.size());
            SpecsLogs.msgInfo("Found " + executables.size() + " executables");
            for (File executable : executables) {
                SpecsLogs.msgInfo("Processing " + executable.getName() + " " + progress.next());
                ExecutionResults results = KernelUtils.measureExecution(executable, 0, minimumRuntime, numberOfRepeats,
                        AverageType.GEOMETRIC_MEAN);

                addExecution(results, csvWriter);

                SpecsLogs.msgInfo(results.toString());
            }
        }

        // Build csv
        SpecsIo.write(new File(csvFilename), csvWriter.buildCsv());

        return;
    }

    /**
     * @param minimumRuntime
     * @return
     */
    private static String getCsvEpilogue(double minimumRuntime) {
        return "-" + Double.toString(minimumRuntime) + ".csv";
    }

    /**
     * @param results
     * @param csvFile
     */
    private static void addExecution(ExecutionResults results, CsvWriter csvFile) {
        List<String> resultLine = SpecsFactory.newArrayList();

        resultLine.add(results.getProgramName());

        for (Number number : results.getRuntimes()) {
            resultLine.add(number.toString());
        }

        csvFile.addLine(resultLine);
    }

    /**
     * @param executable
     * @param numberOfExecutions
     * @return
     */
    private static CsvWriter newCsvFile(int numberOfExecutions, File bufferFile) {
        List<String> header = SpecsFactory.newArrayList();

        header.add("Program");

        for (int i = 0; i < numberOfExecutions; i++) {
            String exec = "Exec " + (i + 1);
            header.add(exec);
        }

        // CsvWriter writer = new CsvWriter();
        CsvWriter writer = new BufferedCsvWriter(bufferFile, header);

        return writer;
    }

    public static String help() {
        return "Command-line arguments: <executable_file> [<output_folder>] [minimum_runtime_seconds ("
                + DEFAULT_MINIMUM_RUNTIME + ")] [number_of_executions (" + DEFAULT_NUMBER_REPEATS + ")]";
    }

}
