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

package org.specs.CIR.Language;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIRFunctions.LibraryFunctions.CMathFunction;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;
import org.specs.CIRFunctions.LibraryFunctions.CStdlibFunction;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunction;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsCollections;

public class IncludesUtils {

    /**
     * Maps names of C library functions to the corresponding include.
     */
    private static final Map<String, SystemInclude> functionsToInclude;
    static {
	functionsToInclude = new HashMap<>();

	// Add CLibraryFunction
	addCLibrary(CMathFunction.class);
	addCLibrary(CStdioFunction.class);
	addCLibrary(CStdlibFunction.class);

	// Add function not yet present in CLibraryFunction
	functionsToInclude.put("fopen", SystemInclude.Stdio);
	functionsToInclude.put("fprintf", SystemInclude.Stdio);
	functionsToInclude.put("fclose", SystemInclude.Stdio);
    }

    private static <C extends Enum<C> & CLibraryFunction> void addCLibrary(Class<C> aClass) {

	for (CLibraryFunction function : aClass.getEnumConstants()) {
	    functionsToInclude.put(function.getFunctionName(), function.getLibrary());
	}
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param functionNames
     * @return
     */
    public static Collection<SystemInclude> getIncludes(String... functionNames) {
	return getIncludes(Arrays.asList(functionNames));
    }

    /**
     * Given a list of names of functions, returns the system includes of those functions.
     * 
     * <p>
     * If any name is not found in the includes list, shows a warning.
     * 
     * @param functionNames
     * @return
     */
    public static Collection<SystemInclude> getIncludes(List<String> functionNames) {
	Set<SystemInclude> includes = new HashSet<>();

	functionNames.forEach(functionName -> SpecsCollections.addOptional(includes, getInclude(functionName)));

	return includes;
    }

    public static Optional<SystemInclude> getInclude(String functionName) {
	SystemInclude include = functionsToInclude.get(functionName);
	if (include == null) {
	    SpecsLogs.warn("No system include found for function '" + functionName + "'");
	    return Optional.empty();
	}

	return Optional.of(include);
    }
}
