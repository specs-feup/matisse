/*
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

package org.suikasoft.CMainFunction.Tester.App;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.suikasoft.CMainFunction.Tester.ExecutionResults;
import org.suikasoft.CMainFunction.Tester.KernelUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.csv.BufferedCsvWriter;
import pt.up.fe.specs.util.csv.CsvWriter;
import pt.up.fe.specs.util.utilities.AverageType;
import pt.up.fe.specs.util.utilities.ProgressCounter;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Builds and deploys Eclipse projects.
 * 
 * @author Joao Bispo
 */
public class AppTester {

    private final AppTesterData data;

    private int numAddedExecutions;

    public AppTester(AppTesterData data) {
        this.data = data;
        this.numAddedExecutions = 0;
    }

    public int execute() {
        File outputFile = new File(data.output, "csv_results.buffer");
        CsvWriter csvWriter = newCsvFile(data.numRepeats, outputFile);
        String csvFilename = null;

        // If single file
        // if (filepath.isFile()) {
        if (data.inputs.inputFiles.size() == 1) {

            File filepath = data.inputs.inputFiles.get(0);

            csvFilename = SpecsIo.removeExtension(filepath.getName()) + getCsvEpilogue(data.minimumTimeSec);

            ExecutionResults results = KernelUtils.measureExecution(filepath, data.inputs.inputPath, data.iterations,
                    data.minimumTimeSec, data.numRepeats, AverageType.GEOMETRIC_MEAN);

            addExecution(results, csvWriter);

            SpecsLogs.msgInfo(results.toString());
        }

        // Otherwise, its a folder
        else {

            csvFilename = data.inputs.inputPath.getName() + getCsvEpilogue(data.minimumTimeSec);

            List<File> executables = data.inputs.inputFiles;

            // Sort executable
            Collections.sort(executables);

            // List<File> executables = IoUtils.getFiles(filepath);
            ProgressCounter progress = new ProgressCounter(executables.size());
            SpecsLogs.msgInfo("Found " + executables.size() + " executables");
            for (File executable : executables) {
                // LoggingUtils.msgInfo("Processing " + IoUtils.getRelativePath(executable, data.inputs.inputPath) + " "
                SpecsLogs.msgInfo("Processing " + executable.getName() + " "
                        + progress.next());

                ExecutionResults results = KernelUtils.measureExecution(executable, data.inputs.inputPath,
                        data.iterations, data.minimumTimeSec, data.numRepeats, AverageType.GEOMETRIC_MEAN);

                addExecution(results, csvWriter);

                SpecsLogs.msgInfo(results.toString());
            }
        }

        // Build csv
        SpecsIo.write(new File(data.output, csvFilename), csvWriter.buildCsv());

        return 0;
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
    private void addExecution(ExecutionResults results, CsvWriter csvFile) {
        List<String> resultLine = SpecsFactory.newArrayList();

        resultLine.add(results.getProgramName());

        for (Number number : results.getRuntimes()) {
            resultLine.add(number.toString());
        }

        // Create geomean value
        resultLine.add(geomeanValue(data.numRepeats, numAddedExecutions));

        // Create std value
        resultLine.add(stdValue(data.numRepeats, numAddedExecutions));

        csvFile.addLine(resultLine);

        // Increment number of added executions
        numAddedExecutions++;

    }

    /**
     * First result is in column B, last result is in column 1 + numRepeats, if A is 0.
     * 
     * @param numRepeats
     * @return
     */
    /*
    private static String getLastResultColumn(int numRepeats) {
    
    return ParseUtils.getAlphaId(1 + numRepeats);
    }
    */

    /**
     * Line is number of added executions + 2 (1-based indexing + header)
     * 
     * @param numAddedExecutions
     * @return
     */
    private static String getLine(int numAddedExecutions) {

        return String.valueOf(numAddedExecutions + 2);
    }

    private static String geomeanValue(int numRepeats, int numAddedExecutions) {
        // String lastColumn = getLastResultColumn(numRepeats);
        String lastColumn = SpecsStrings.getAlphaId(numRepeats);
        String line = getLine(numAddedExecutions);

        Replacer geomean = new Replacer("=GEOMEAN(B<LINE>:<COLUMN><LINE>)");

        geomean.replace("<COLUMN>", lastColumn).replace("<LINE>", line);

        return geomean.toString();
    }

    private String stdValue(int numRepeats, int numAddedExecutions2) {
        // String lastColumn = getLastResultColumn(numRepeats);
        String lastColumn = SpecsStrings.getAlphaId(numRepeats);

        String line = getLine(numAddedExecutions);
        // Geo column is lastColumn + 1
        String geoColumn = SpecsStrings.getAlphaId(numRepeats + 1);

        Replacer stdev = new Replacer("=STDEV.P(B<LINE>:<LASTCOL><LINE>)/<GEOCOL><LINE>");

        stdev.replace("<LINE>", line).replace("<LASTCOL>", lastColumn).replace("<GEOCOL>", geoColumn);

        return stdev.toString();
    }

    /**
     * @param executable
     * @param numberOfExecutions
     * @return
     */
    private static CsvWriter newCsvFile(int numberOfExecutions, File outputFile) {
        List<String> header = SpecsFactory.newArrayList();

        header.add("Program");

        for (int i = 0; i < numberOfExecutions; i++) {
            String exec = "Exec " + (i + 1);
            header.add(exec);
        }

        // Column for geometric mean
        header.add("Geometric Mean");

        // Column for standard deviation
        header.add("StdDev / GeoMean");

        CsvWriter writer = new BufferedCsvWriter(outputFile, header);

        return writer;
    }

}
