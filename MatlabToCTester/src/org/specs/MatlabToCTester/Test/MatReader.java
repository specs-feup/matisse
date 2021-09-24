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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;

import pt.up.fe.specs.util.SpecsLogs;

public class MatReader implements ArrayReader {

    private final MatFileReader matReader;

    MatReader(MatFileReader matReader) {
	this.matReader = matReader;
    }

    public static Optional<ArrayReader> newInstance(File file) {
	MatFileReader reader = null;
	try {
	    reader = new MatFileReader(file);
	} catch (IOException e) {
	    SpecsLogs.warn("Could not create MatReader from file '" + file + "'", e);
	    return Optional.empty();
	}

	return Optional.of(new MatReader(reader));
    }

    @Override
    public Optional<ArrayResult> getVariable(String variableName) {
	MLArray array = matReader.getMLArray(variableName);
	if (array == null) {
	    return Optional.empty();
	}

	return Optional.of(new MatResults(array));
    }

    @Override
    public List<String> getVariableNames() {
	return new ArrayList<>(matReader.getContent().keySet());
    }

}
