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

package org.specs.CIR.Tree.CToCRules;

import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Collects data during C-to-C process.
 * 
 * @author Joao Bispo
 * 
 */
public class CToCData {

    private final static String TEMP_MATRIX_PREFIX = "temp_m";

    private final FunctionType functionTypes;

    private final Set<String> outputAsInputNames;

    private final boolean isFunction;
    private final boolean snippetMode;

    private int declaredMatrixCounter;
    // private static AtomicInteger declaredMatrixCounter = new AtomicInteger(0);

    /**
     * <p>
     * If function types is null, assumes script.
     * 
     * @param functionTypes
     */
    public CToCData(FunctionType functionTypes, boolean snippetMode) {
	this.functionTypes = functionTypes;
	if (functionTypes == null) {
	    isFunction = false;
	    outputAsInputNames = null;
	} else {
	    isFunction = true;
	    outputAsInputNames = SpecsFactory.newHashSet(functionTypes.getOutputAsInputNames());
	}

	this.snippetMode = snippetMode;
	// CToCData.declaredMatrixCounter = 0;
    }

    /*
    public CToCData(FunctionType functionTypes2, boolean snippetMode) {
    	// TODO Auto-generated constructor stub
    }
    */
    /**
     * @return the functionTypes
     */
    public FunctionType getFunctionTypes() {
	return functionTypes;
    }

    /**
     * @param name
     * @return
     */
    public boolean isOutputAsInput(String name) {
	return outputAsInputNames.contains(name);
    }

    public boolean isFunction() {
	return isFunction;
    }

    /**
     * @return the declaredMatrixCounter
     */
    /*
    public int getDeclaredMatrixCounter() {
    return declaredMatrixCounter;
    }
    */
    /*
        public void incrementDeclaredMatrixCounter() {
    	declaredMatrixCounter += 1;
        }
    */
    /**
     * @return
     */
    public String nextTemporaryName() {
	// Build name
	String name = CToCData.TEMP_MATRIX_PREFIX + declaredMatrixCounter;
	// String name = CToCData.TEMP_MATRIX_PREFIX + CToCData.declaredMatrixCounter.getAndIncrement();

	// Increment counter
	declaredMatrixCounter += 1;

	// Return name
	return name;
    }

    public boolean isSnippetMode() {
	return snippetMode;
    }
}
