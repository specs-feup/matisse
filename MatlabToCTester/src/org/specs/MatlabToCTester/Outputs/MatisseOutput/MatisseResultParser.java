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

import java.util.List;
import java.util.Scanner;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIRTypes.Types.BaseTypes;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.MatlabToCTypesUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

public class MatisseResultParser {

    private static final String DELIMITER_HEADER = ":";

    private final TypeDecoder decoder;

    public MatisseResultParser(NumericFactory numerics) {
        decoder = BaseTypes.newTypeDecode(numerics);
    }

    public static String getName(String arrayString) {
        int separatorIndex = arrayString.indexOf(MatisseResultParser.DELIMITER_HEADER);

        if (separatorIndex == -1) {
            throw new RuntimeException("Malformed array string, should have a '" + MatisseResultParser.DELIMITER_HEADER
                    + "' delimiting the variable name.");
        }

        return arrayString.substring(0, separatorIndex);
    }

    public MatisseResult parse(String arrayString) {

        String variableName = null;
        String variableDimensions = null;
        String variableValues = null;
        // NumericClassName variableType = null;
        VariableType variableType = null;

        try (Scanner variableScanner = new Scanner(arrayString)) {
            variableScanner.useDelimiter("=");

            // The first token is the variable header
            if (variableScanner.hasNext()) {

                try (Scanner headerScanner = new Scanner(variableScanner.next())) {

                    headerScanner.useDelimiter(":");
                    // First the name
                    if (headerScanner.hasNext()) {
                        variableName = headerScanner.next();
                    }
                    // Then the dimensions
                    if (headerScanner.hasNext()) {
                        variableDimensions = headerScanner.next();
                    }
                    // Then the variable type
                    if (headerScanner.hasNext()) {
                        String cType = headerScanner.next();

                        variableType = MatlabToCTypesUtils.getType(cType, decoder);
                        // variableType = MatlabToCTypesUtils.getNumericClass(type);

                    }
                }
                // headerScanner.close();
            }

            // The second token are the values
            if (variableScanner.hasNext()) {
                variableValues = variableScanner.next();
            }

        }
        // variableScanner.close();

        // Process the dimensions
        List<Integer> dimensions = processDimensions(variableDimensions);

        // Process the values
        List<Number> values = processValues(variableValues);

        return new MatisseResult(dimensions, values, variableName, variableType);
    }

    /**
     * Takes a string with the values and returns a list of doubles with the same information.
     * 
     * @param variableValues
     *            - the string
     * @param variableType
     * @return the list
     */
    private static List<Number> processValues(String variableValues) {

        List<Number> values = SpecsFactory.newArrayList();

        // Trim string
        variableValues = variableValues.trim();

        try (Scanner scanner = new Scanner(variableValues)) {
            scanner.useDelimiter(" ");

            while (scanner.hasNext()) {
                String numberString = scanner.next();

                // Number newNumber = variableType.parseNumber(numberString);
                Number newNumber = SpecsStrings.parseNumber(numberString);
                values.add(newNumber);
            }
        }
        // scanner.close();
        return values;
    }

    /**
     * Takes a string with the dimensions and returns a list of integers with the same information.
     * 
     * @param variableDimensions
     *            - the string
     * @return the list
     */
    private static List<Integer> processDimensions(String variableDimensions) {

        List<Integer> dimensions = SpecsFactory.newArrayList();

        variableDimensions = variableDimensions.substring(1, variableDimensions.length() - 1);
        variableDimensions = variableDimensions.replaceAll(" ", "");

        try (Scanner scanner = new Scanner(variableDimensions)) {
            scanner.useDelimiter(",");
            while (scanner.hasNextInt()) {

                dimensions.add(scanner.nextInt());
            }
        }
        // scanner.close();

        return dimensions;
    }

}
