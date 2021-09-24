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
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.suikasoft.CMainFunction.Builder.MainFunctionTarget;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.system.ProcessOutputAsString;
import pt.up.fe.specs.util.utilities.AverageType;

/**
 * Runs a program which accepts as first argument the number of runs of the kernel, and outputs the execution time in
 * seconds.
 * 
 * @author Joao Bispo
 * 
 */
public class KernelUtils {

    // private static final double MINIMUM_TIME_SECONDS = 1;
    private static final double MAXIMUM_EXPLORE_ARGUMENT = 1e10;

    private static final NumberFormat NUMBER_FORMAT;

    static {
        NUMBER_FORMAT = NumberFormat.getInstance(Locale.UK);
        NUMBER_FORMAT.setMaximumFractionDigits(3);
        NUMBER_FORMAT.setMinimumFractionDigits(3);
    }

    // private static final int PRECISION_FACTOR = 1000;

    // private static final int NUMBER_OF_EXECUTIONS = 5;

    /**
     * Tests a kernel to discover how many runs it should have, according to the machine it is running on.
     * 
     * @param executable
     * @param minimumRuntime
     *            the minimum runtime, in seconds
     * @return
     */
    public static int discoverNumberOfRuns(File executable, double minimumRuntime) {

        double firstTime = run(executable, 1);

        // If first run is greater than the minimum run time, one repeat once
        if (firstTime > minimumRuntime) {
            return 1;
        }

        return exploreNumberOfRuns(executable, minimumRuntime);
    }

    /**
     * Runs the programs passing as argument 'numberOfRuns'.
     * 
     * @param executable
     * @param numberOfRuns
     * @return
     */
    private static double run(File executable, int numberOfRuns) {
        // Build command with 1 run
        List<String> firstRun = Arrays.asList(SpecsIo.getPath(executable), Integer.toString(numberOfRuns));
        // String workingDir = IoUtils.getPath(IoUtils.getParent(executable));
        // ProcessOutput output = ProcessUtils.run(firstRun, workingDir, true, false);
        ProcessOutputAsString output = SpecsSystem.runProcess(firstRun, executable.getParentFile(), true, false);

        return MainFunctionTarget.decodeTimeMeasure(output.getOutput());
    }

    /**
     * @param executable
     * @return
     */
    private static int exploreNumberOfRuns(File executable, double minExecutionTime) {

        // Start with 10 runs
        int currentNumber = 10;
        double currentTime = 0.0;

        while (currentNumber < MAXIMUM_EXPLORE_ARGUMENT) {
            currentTime = run(executable, currentNumber);

            if (currentTime < minExecutionTime) {
                currentNumber *= 2;
                continue;
            }

            // Current time is different than 0 for the first time, multiply by 1000 and return it
            // return currentNumber * PRECISION_FACTOR;
            return currentNumber;

        }

        // If number of runs is greater than maximum, warn uses
        SpecsLogs.msgInfo("Exceeded maximum number of runs (" + MAXIMUM_EXPLORE_ARGUMENT + "), returning -1.");
        return -1;
    }

    /**
     * Helper method which uses the parent of the executable as the basePath.
     * 
     * @param executable
     * @param minimumRuntime
     * @param geometricmean
     * @return
     */
    public static ExecutionResults measureExecution(File executable, int iterations, double minimumRuntime,
            int numberOfExecutions, AverageType geometricmean) {

        return measureExecution(executable, executable.getParentFile(), iterations, minimumRuntime, numberOfExecutions,
                geometricmean);
    }

    public static ExecutionResults measureExecution(File executable, File basePath, int iterations,
            double minimumRuntime, int numberOfExecutions, AverageType geometricmean) {

        int numberOfRuns = -1;
        if (iterations > 0) {
            numberOfRuns = iterations;
        } else {
            numberOfRuns = discoverNumberOfRuns(executable, minimumRuntime);
        }
        // int numberOfRuns = discoverNumberOfRuns(executable, minimumRuntime);
        SpecsLogs.msgInfo("Number of kernel runs per execution: " + numberOfRuns);

        // CsvWriter csvFile = newCsvFile(numberOfExecutions);

        List<Double> runtimes = SpecsFactory.newArrayList();
        // List<String> csvLine = FactoryUtils.newArrayList();
        // csvLine.add(executable.getName());

        // Executime one time, to warm-up
        run(executable, numberOfRuns); // TODO should execute just one time?
        SpecsLogs.msgInfo("Warm-up execution");

        for (int i = 0; i < numberOfExecutions; i++) {
            Double runtime = run(executable, numberOfRuns);

            // Adjust to the number of runs
            double realRuntime = runtime / numberOfRuns;
            runtimes.add(realRuntime);
            // LoggingUtils.msgInfo("Execution " + (i + 1) + " : " + realRuntime);
            SpecsLogs.msgInfo("Execution " + (i + 1) + " : " + KernelUtils.getFormat().format(realRuntime));
        }

        String programName = SpecsIo.getRelativePath(executable, basePath);
        // Sanitize string
        programName = programName.replace('/', '_');

        // return new ExecutionResults(executable.getName(), runtimes, geometricmean);
        return new ExecutionResults(programName, runtimes, geometricmean);
    }

    public static NumberFormat getFormat() {
        return NUMBER_FORMAT;
    }

}
