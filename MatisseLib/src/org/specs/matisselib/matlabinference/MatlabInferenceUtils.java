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

package org.specs.matisselib.matlabinference;

import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.matlabinference.utils.SystemFunctionTypes;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class MatlabInferenceUtils {

    public static MatlabFunctionType getFunctionType(String functionName, DataStore data) {

	// Function name resolution not implemented yet, check how it should be done in "Taming MATLAB".
	// Right now, only supporting system functions
	SystemFunctionTypes types = data.get(MatisseInit.SYSTEM_FUNCTION_TYPES);
	return types.getType(functionName)
		.orElseThrow(() -> new RuntimeException("Could not find system function '" + functionName
			+ "', currently not supporting user functions"));
    }

}
