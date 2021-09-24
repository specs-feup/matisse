/**
 * Copyright 2012 SPeCS Research Group.
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

import java.util.HashSet;
import java.util.Set;

import pt.up.fe.specs.util.providers.KeyStringProvider;

/**
 * @author Joao Bispo
 * 
 */
// public enum SystemInclude implements KeyProvider<String> {
public enum SystemInclude implements KeyStringProvider {
    // public enum SystemInclude {

    Assert("assert.h"),
    Errno("errno.h"),
    IntTypes("inttypes.h"),
    Math("math.h"),
    Stdint("stdint.h"),
    Stdbool("stdbool.h"),
    Stdio("stdio.h"),
    Stdlib("stdlib.h"),
    String("string.h"),
    Time("time.h"),
    Windows("windows.h"),
    Blas("cblas.h");

    private final String includeName;

    /**
     * System includes (e.g., stdio.h)
     */
    private static final Set<String> systemIncludes;

    static {
	systemIncludes = new HashSet<>();

	for (SystemInclude include : SystemInclude.values()) {
	    SystemInclude.systemIncludes.add(include.getIncludeName());
	}
    }

    /**
     * Constructor
     */
    private SystemInclude(String includeName) {
	this.includeName = includeName;
    }

    /**
     * @return the includeName
     */
    public String getIncludeName() {
	return this.includeName;
    }

    /**
     * 
     * @param includeName
     * @return true if the given name is a system include. False otherwise.
     */
    public static boolean isSystemInclude(String includeName) {
	/*
	if(includeName.equals(Stdint.getIncludeName())) {
	    return false;
	}
	*/
	return SystemInclude.systemIncludes.contains(includeName);
    }

    /**
     * @return the include name
     */
    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.Interfaces.KeyProvider#getKey()
     */
    @Override
    public String getKey() {
	return getIncludeName();
    }

    /**
     * Returns the includeName, as if calling getIncludeName().
     */
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
	return getIncludeName();
    }
}
