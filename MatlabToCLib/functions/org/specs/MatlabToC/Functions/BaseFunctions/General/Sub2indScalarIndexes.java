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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import org.specs.CIR.Options.MemoryLayout;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseTemplate;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;

import pt.up.fe.specs.util.SpecsIo;

/**
 * @author Joao Bispo
 * 
 */
public class Sub2indScalarIndexes extends MatlabTemplate {

    private final int totalIndexes;
    private final MemoryLayout memoryLayout;

    public Sub2indScalarIndexes(int totalIndexes, MemoryLayout memoryLayout) {
	this.totalIndexes = totalIndexes;
	this.memoryLayout = memoryLayout;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.MFileInstance.MatlabTemplate#getMCode()
     */
    @Override
    public String getMCode() {
	StringBuilder builder = null;

	String code = getTemplate();

	code = code.replace("<FUNCTION_NAME>", getName());

	code = code.replace("<NUM_INDEXES>", Integer.toString(totalIndexes));

	builder = new StringBuilder();
	for (int i = 0; i < totalIndexes; i++) {
	    builder.append(", ").append(getIndexName(i));
	}
	code = code.replace("<INDEXES>", builder);

	builder = new StringBuilder();
	for (int i = 0; i < totalIndexes; i++) {
	    builder.append("\t").append("indexes(").append(i + 1).append(") = ");
	    builder.append(getIndexName(i)).append(";\n");
	}
	code = code.replace("<INDEX_ASSIGN>", builder);
	// System.out.println("CODE:\n"+code);
	return code;
    }

    /**
     * @return
     */
    private String getTemplate() {

	// Subscripts are still MATLAB subscripts, so the MATLAB formula should be used
	// return IoUtils.getResource(BaseTemplate.SUB2IND_SCALAR_INDEXES_COLUMN_MAJOR);

	if (memoryLayout == MemoryLayout.ROW_MAJOR) {
	    return SpecsIo.getResource(BaseTemplate.SUB2IND_SCALAR_INDEXES_ROW_MAJOR);
	}

	if (memoryLayout == MemoryLayout.COLUMN_MAJOR) {
	    return SpecsIo.getResource(BaseTemplate.SUB2IND_SCALAR_INDEXES_COLUMN_MAJOR);
	}

	throw new RuntimeException("");

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
	return "sub2ind_" + totalIndexes;
    }

}
