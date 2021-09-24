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
import java.util.Optional;

import org.specs.MatlabToCTester.Outputs.MatisseOutput.MatisseResultReader;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

public class TestUtils {

    private static final String EXTENSION_MAT = "mat";
    private static final String EXTENSION_MATISSE = "txt";

    /**
     * Reads a variable from a file, uses the extension to decide how to read the file.
     * 
     * <p>
     * Currently supports .mat and .txt (MATISSE formate).
     * 
     * @param file
     * @param variableName
     * @return
     */
    public static Optional<ArrayResult> readVariable(File file, String variableName, DataStore setup) {
        // Choose type according to extension
        String extension = SpecsIo.getExtension(file);

        if (extension.equals(TestUtils.EXTENSION_MAT)) {
            return MatResults.newInstance(file, variableName);
        }

        if (extension.equals(TestUtils.EXTENSION_MATISSE)) {
            return MatisseResultReader.getVariable(file, variableName, setup);
        }

        SpecsLogs.warn("Could not recognize extension '" + extension + "' in '" + file
                + "', using MATISSE text format as default");

        return MatisseResultReader.getVariable(file, variableName, setup);
    }

    public static Optional<Double> readDouble(File file, String variableName, DataStore setup) {
        // Get variable
        Optional<ArrayResult> variable = readVariable(file, variableName, setup);
        if (!variable.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(variable.get().getDouble(0));
    }

    public static ArrayReader getReader(File file, DataStore setup) {
        // Choose type according to extension
        String extension = SpecsIo.getExtension(file);

        if (extension.equals(TestUtils.EXTENSION_MAT)) {
            return MatReader.newInstance(file).get();
        }

        if (extension.equals(TestUtils.EXTENSION_MATISSE)) {
            return new MatisseResultReader(file, setup);
        }

        SpecsLogs.warn("Could not recognize extension '" + extension + "' in '" + file
                + "', using MATISSE text format as default");

        return new MatisseResultReader(file, setup);
    }
}
