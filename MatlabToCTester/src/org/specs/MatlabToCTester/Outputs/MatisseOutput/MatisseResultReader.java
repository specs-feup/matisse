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

package org.specs.MatlabToCTester.Outputs.MatisseOutput;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToCTester.Test.ArrayReader;
import org.specs.MatlabToCTester.Test.ArrayResult;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.utilities.LineStream;

public class MatisseResultReader implements ArrayReader, Closeable {

    private final DataStore setup;
    private final File file;
    private LineStream lineReader;

    private final Map<String, Double> lowThreshold;

    public MatisseResultReader(File file, DataStore setup) {
        this.file = file;
        this.setup = setup;
        lineReader = LineStream.newInstance(file);
        lowThreshold = new HashMap<>();

        initTables();
    }

    private void initTables() {
        // Go over all lines, looking for MATISSE variables
        while (lineReader.hasNextLine()) {
            String line = lineReader.nextLine();
            String variableName = MatisseResultParser.getName(line);

            if (variableName.endsWith(getLowThresholdSuffix())) {
                // Parse line, get double
                lowThreshold.put(variableName,
                        new MatisseResultParser(new NumericFactory(setup)).parse(line).getDouble(0));
            }
        }

        // Close reader, create it again
        lineReader.close();
        lineReader = LineStream.newInstance(file);
    }

    public static Optional<ArrayResult> getVariable(File file, String variableName, DataStore setup) {
        try (MatisseResultReader reader = new MatisseResultReader(file, setup)) {
            return reader.getVariable(variableName);
        } catch (IOException e) {
            SpecsLogs.warn("Problems while getting variable from file '" + file + "'", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ArrayResult> getVariable(String variableName) {
        String firstVariable = null;

        // Ask lines until it find the variable. Stop when passed all lines
        while (true) {
            // If at the end of the reader, create a new reader again
            if (!lineReader.hasNextLine()) {
                lineReader.close();
                lineReader = LineStream.newInstance(file);
            }

            String line = lineReader.nextLine();
            // Get variable
            String currentName = MatisseResultParser.getName(line);

            // If first variable, save it. Otherwise, check if already seen
            if (firstVariable == null) {
                firstVariable = currentName;
            } else {
                if (firstVariable.equals(currentName)) {
                    // Could not find variable with given name
                    return Optional.empty();
                }
            }

            // Parse line if current variable name is the same as the given name
            if (currentName.equals(variableName)) {
                return Optional.of(new MatisseResultParser(new NumericFactory(setup)).parse(line));
            }
        }

    }

    @Override
    public Optional<Double> getLowThreshold(String variableName) {
        Double value = lowThreshold.get(variableName + getLowThresholdSuffix());
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    @Override
    public void close() throws IOException {
        lineReader.close();
    }

    @Override
    public List<String> getVariableNames() {
        // Not implemented
        return null;
    }

}
