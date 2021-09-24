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

package org.specs.CIR.TypesOld.MatrixUtils;

import java.util.Arrays;
import java.util.Set;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * TODO: Replaced by "usesDynamicAllocation"
 * 
 * @author Joao Bispo
 * @deprecated
 */
@Deprecated
public enum MatrixImplementation {
    // The names DECLARED and ALLOCATED are here for compatibility (still used in MATISSE website)
    DECLARED("static", "DECLARED"),
    ALLOCATED("dynamic", "ALLOCATED");

    // private final String id;
    private final Set<String> ids;

    /*
    private static final Map<String, MatrixImplementation> nameMap;
    static {
    nameMap = FactoryUtils.newHashMap();
    
    for (MatrixImplementation impl : values()) {
        nameMap.put(impl.getSmallId(), impl);
    }
    }
    */

    /**
     * @param smallId
     */
    // private MatrixImplementation(String smallId) {
    private MatrixImplementation(String... ids) {
	this.ids = SpecsFactory.newHashSet(Arrays.asList(ids));
    }

    /**
     * @return
     */
    /*
    public String getSmallId() {
    return id;
    }
    */

    /**
     * Converts the string to lower case, then upper-cases the first letter, to correspond to the name of the option.
     * 
     * @param implementationName
     * @return
     */
    public static MatrixImplementation parse(String implementationName) {
	if (implementationName.length() < 1) {
	    return null;
	}

	for (MatrixImplementation impl : values()) {
	    // Check if implementation name is part of the option
	    if (impl.ids.contains(implementationName)) {
		return impl;
	    }
	}

	SpecsLogs.msgInfo("could not decode option '" + implementationName
		+ "'. Available options: ");
	for (MatrixImplementation impl : values()) {
	    SpecsLogs.msgInfo(impl.ids + "; ");
	}

	return null;
	/*
		// If could not find option, return null
	
		// Pass to lower case
		implementationName = implementationName.toLowerCase();
	
		// Up first character
		// char firstUpper = Character.toUpperCase(implementationName.charAt(0));
	
		// Return name with first upper case letter
		// String name = firstUpper + implementationName.substring(1);
	
		// MatrixImplementation impl = EnumUtils.valueOf(MatrixImplementation.class, name);
		MatrixImplementation impl = nameMap.get(implementationName);
	
		if (impl == null) {
		    // System.out.println("NAME:"+name);
		    System.out.println("could not decode option '" + implementationName
			    + "'. Available options:" + nameMap.keySet());
		    return null;
		}
	
		return impl;
		*/
    }

}
