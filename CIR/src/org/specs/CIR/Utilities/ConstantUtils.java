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

package org.specs.CIR.Utilities;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNativeUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;

import pt.up.fe.specs.util.SpecsStrings;

/**
 * @author Joao Bispo
 * 
 */
public class ConstantUtils {

    /**
     * Parses the given values to the type provided.
     * 
     * @param type
     * @param values
     * @return
     */
    public static List<String> parseValues(VariableType type, List<String> values) {

	if (ScalarUtils.isScalar(type)) {
	    return parseNumeric(type, values);
	}

	// For matrices, call recursively for each element

	throw new RuntimeException("Method not implemented for type '" + type + "'");
    }

    /**
     * @param numType
     * @param values
     * @return
     */
    private static List<String> parseNumeric(VariableType numType, List<String> values) {
	if (values.size() != 1) {
	    throw new RuntimeException("Given values as size diff than one (" + values.size() + ")");
	}

	String value = values.get(0);

	Number number = SpecsStrings.parseNumber(value, false);
	// CNumber cNumber = CNumberFactory.newInstance(number, numType);
	CNumber cNumber = CLiteral.newInstance(number, CNativeUtils.toCNative(numType));

	String parsedValue = cNumber.toCString();

	return Arrays.asList(parsedValue);
    }
}
