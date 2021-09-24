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

package org.specs.CIRTypes.Types.StdInd;

import java.util.Collection;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.TypeDecoder;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * Only EXACT_WIDTH currently supported.
 * 
 * @author Joao Bispo
 * 
 */
public class StdIntDecoder implements TypeDecoder {

    private List<String> supportedTypes;
    private final boolean verbose;

    public StdIntDecoder() {
	this(false);
    }

    public StdIntDecoder(boolean verbose) {
	this.supportedTypes = null;
	this.verbose = verbose;
    }

    @Override
    public VariableType decode(String typeString) {
	String workString = typeString;

	// Check if unsigned
	boolean isUnsigned = false;
	if (workString.startsWith("u")) {
	    isUnsigned = true;
	    workString = workString.substring(1, workString.length());
	}

	if (!workString.startsWith("i")) {
	    if (verbose) {
		SpecsLogs.msgInfo("Expecting 'i' in string '" + workString + "'. Could not decode '" + typeString
			+ "'");
	    }

	    return null;

	}
	workString = workString.substring(1, workString.length());

	// Check if it has "nt", for 'int'
	if (workString.startsWith("nt")) {
	    workString = workString.substring("nt".length());
	}

	// Remove final "_t"
	if (workString.endsWith("_t")) {
	    workString = workString.substring(0, workString.length() - "_t".length());
	}

	// Parse number of bits
	Integer bits = SpecsStrings.parseInteger(workString);
	if (bits == null) {
	    if (verbose) {
		SpecsLogs.msgInfo("Expecting an integer in string '" + workString + "'. Could not decode '"
			+ typeString + "'");
	    }

	    return null;
	}

	return StdIntType.newInstance(bits, isUnsigned);
    }

    @Override
    public Collection<String> supportedTypes() {
	if (supportedTypes == null) {
	    supportedTypes = initSupportedTypes();
	}

	// return supportedTypes();
	return supportedTypes;
    }

    private static List<String> initSupportedTypes() {
	List<String> supportedTypes = SpecsFactory.newArrayList();

	supportedTypes.add("i<NUM_BITS>");
	supportedTypes.add("ui<NUM_BITS>");

	return supportedTypes;
    }
}
