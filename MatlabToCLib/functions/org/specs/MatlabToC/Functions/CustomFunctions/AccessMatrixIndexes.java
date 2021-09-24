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

import pt.up.fe.specs.util.SpecsIo;

/**
 * Creates an M file with a function which returns the values indexed by the given inputs, which can either be scalar or
 * matrices.
 * 
 * @author Joao Bispo
 * 
 */
public class AccessMatrixIndexes extends MatlabTemplate {

    private final int numIndexes;

    public AccessMatrixIndexes(int numIndexes) {
	this.numIndexes = numIndexes;
    }

    @Override
    public String getName() {
	return "indexes_" + numIndexes;
    }

    @Override
    public String getMCode() {
	String functionName = getName();

	String code = SpecsIo.getResource(BuiltinTemplate.MATRIX_INDEXES);
	code = code.replace("<FUNCTION_NAME>", functionName);

	StringBuilder builder = new StringBuilder();
	for (int i = 0; i < numIndexes; i++) {
	    if (i != 0) {
		builder.append(", ");
	    }
	    builder.append(getIndexName(i));
	}

	code = code.replace("<INPUT_INDEXES>", builder.toString());

	builder = new StringBuilder();

	// numel(index1)*numel(index2)...

	for (int i = 0; i < numIndexes; i++) {
	    if (i != 0) {
		builder.append("*");
	    }

	    builder.append("numel(").append(getIndexName(i)).append(")");

	}

	code = code.replace("<ELEMS_MULT>", builder.toString());

	// Build fors
	String itPrefix = "it";
	builder = new StringBuilder();
	for (int i = 0; i < numIndexes; i++) {
	    for (int j = 0; j <= i; j++) {
		builder.append("\t");
	    }

	    int index = numIndexes - i;
	    builder.append("for it" + index + "=1:numel(" + getIndexName(index - 1) + ")\n");
	}

	for (int i = 0; i <= numIndexes; i++) {
	    builder.append("\t");
	}
	builder.append("output(counter) = sub2ind(size(X), ");
	for (int i = 0; i < numIndexes; i++) {
	    if (i != 0) {
		builder.append(", ");
	    }
	    builder.append(getIndexName(i)).append("(").append(itPrefix).append(i + 1).append(")");
	}
	builder.append(");\n");
	for (int i = 0; i <= numIndexes; i++) {
	    builder.append("\t");
	}
	builder.append("counter = counter + 1;\n");

	for (int i = 0; i < numIndexes; i++) {
	    for (int j = 0; j <= numIndexes - i - 1; j++) {
		builder.append("\t");
	    }

	    builder.append("end\n");
	}

	code = code.replace("<FORS>", builder.toString());

	return code;
    }

    /**
     * @param i
     * @return
     */
    private static Object getIndexName(int i) {
	return "indexes" + (i + 1);
    }
}
