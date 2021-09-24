/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.String;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsStrings;

public class StringTypeUtils {

    public static boolean isString(VariableType type) {
	return type instanceof StringType;
    }

    /**
     * @param lastArg
     * @return
     */
    public static String getString(VariableType type) {
	StringType string = SpecsStrings.cast(type, StringType.class);

	return string.getString();
    }
}
