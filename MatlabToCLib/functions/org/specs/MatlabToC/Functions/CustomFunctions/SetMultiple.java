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

package org.specs.MatlabToC.Functions.CustomFunctions;

import org.specs.MatlabToC.MFileInstance.MatlabTemplate;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;

import pt.up.fe.specs.util.SpecsIo;

/**
 * Function call receives: <br>
 * - A matrix, which will be accessed; <br>
 * - A number of indexes, either scalar or matrices, in number equal to totalIndexes <br>
 * - The value to be set, which can be either a scalar or a matrix
 * 
 * @author Joao Bispo
 * 
 */
public class SetMultiple extends MatlabTemplate {

    private final int totalIndexes;
    private final boolean isValueScalar;
    private final MatlabToCEngine engine;

    public SetMultiple(int totalIndexes, boolean isValueScalar, MatlabToCEngine engine) {
	this.totalIndexes = totalIndexes;
	this.isValueScalar = isValueScalar;
	this.engine = engine;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.MFileInstance.MatlabTemplate#getMCode()
     */
    @Override
    public String getMCode() {
	StringBuilder builder = null;

	String code = SpecsIo.getResource(BuiltinTemplate.SET_MULTIPLE);

	// Function name
	code = code.replace("<FUNCTION_NAME>", getName());

	// Indexes
	builder = new StringBuilder();
	for (int i = 0; i < this.totalIndexes; i++) {
	    builder.append(", ").append(getIndexName(i));

	}

	code = code.replace("<INDEX_LIST>", builder.toString());

	// Access Matrix - e.g., access_3index(X, 1:size(X,1), index2, 1:lastIndex)
	builder = new StringBuilder();

	// Add function
	MatlabTemplate accessMatrixTemplate = new AccessMatrixIndexes(this.totalIndexes);
	this.engine.forceLoad(accessMatrixTemplate);
	String functionName = accessMatrixTemplate.getName();
	builder.append(functionName).append("(X");

	// Indexes but the last
	for (int i = 0; i < this.totalIndexes; i++) {
	    builder.append(", ").append(getIndexName(i));

	}
	builder.append(")");

	code = code.replace("<INDEXES_CALL>", builder.toString());

	// Is scalar
	String access = "";
	if (!this.isValueScalar) {
	    access = "(i)";
	}
	code = code.replace("<IS_SCALAR>", access);

	return code;
	// return builder.toString();
    }

    /**
     * @param i
     * @return
     */
    private static String getIndexName(int i) {
	return "index" + (i + 1);
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.MFileInstance.MatlabTemplate#getName()
     */
    @Override
    public String getName() {
	return "set_multiple_" + this.totalIndexes;
    }
}
