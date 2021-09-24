/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIRTypes.Types.StaticMatrix;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Types.Views.Code.ACode;

import pt.up.fe.specs.util.SpecsStrings;

public class StaticMatrixCode extends ACode {

    private final static int NUMBER_ELEMENTS_PER_LINE = 80;

    private final StaticMatrixType matrix;

    public StaticMatrixCode(StaticMatrixType staticMatrix) {
        super(staticMatrix);
        this.matrix = staticMatrix;
    }

    @Override
    public String getSimpleType() {
        // return matrix.getElementType().code().getType() + "*";
        return this.matrix.getElementType().code().getType();
    }

    /**
     * Suffixes a '*' to the end of the array.
     */
    @Override
    public String getReturnType() {
        return getType() + "*";
    }

    @Override
    public String getDeclaration(String variableName) {
        int numElements = this.matrix.matrix().getNumElements();
        if (numElements == 0) {
            numElements = 1;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(getType());
        builder.append(" ");
        builder.append(variableName);
        builder.append("[");
        builder.append(numElements);
        builder.append("]");

        return builder.toString();
    }

    /**
     * Initializes matrix statically when declaring it.
     * 
     * <p>
     * Example: int temp_m0[3] = {1, 2, 3}
     */
    @Override
    public String getDeclarationWithInputs(String variableName, List<String> values) {
        StringBuilder builder = new StringBuilder();

        // Add type of the data in the matrix

        builder.append(getDeclaration(variableName));

        // If there are no values, return
        if (values.isEmpty()) {
            return builder.toString();
        }

        // Add initialization values
        builder.append(" = ");

        builder.append("{");

        String indentation = SpecsStrings.buildLine(" ", builder.toString().length());
        String newLine = System.getProperty("line.separator");
        int counter = 0;
        int limit = StaticMatrixCode.NUMBER_ELEMENTS_PER_LINE;
        for (int i = 0; i < values.size() - 1; i++) {
            if (counter > limit) {
                builder.append(newLine);
                builder.append(indentation);
                counter = 0;
            }

            String valueString = values.get(i);
            counter += valueString.length();
            builder.append(valueString);
            builder.append(",");
        }

        // Add last number
        String valueString = values.get(values.size() - 1);
        counter += valueString.length();
        builder.append(valueString);

        builder.append("}");

        return builder.toString();
    }

    @Override
    public Set<String> getIncludes() {
        Set<String> includes = new HashSet<>();

        // Add includes for the element type
        includes.addAll(this.matrix.getElementType().code().getIncludes());

        return includes;
    }

    @Override
    public Set<FunctionInstance> getInstances() {
        return matrix.getElementType().code().getInstances();
    }
}
