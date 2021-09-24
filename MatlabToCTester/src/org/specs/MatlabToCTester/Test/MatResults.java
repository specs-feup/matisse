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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.JMatIOPlus.MatUtils;

import com.jmatio.types.MLArray;

public class MatResults implements ArrayResult {

    private final MLArray variable;
    private final List<Integer> dims;

    MatResults(MLArray variable) {
        this.variable = variable;
        dims = Arrays.stream(variable.getDimensions())
                .mapToObj(intDim -> Integer.valueOf(intDim))
                .collect(Collectors.toList());

    }

    public static Optional<ArrayResult> newInstance(File matFile, String variableName) {
        Optional<MLArray> variable = MatUtils.read(matFile, variableName);

        if (!variable.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new MatResults(variable.get()));
    }

    @Override
    public List<Integer> getDimensions() {
        return dims;
    }

    @Override
    public double getDouble(int index) {
        return variable.getDouble(index);
    }

    @Override
    public String getName() {
        return variable.name;
    }

}
