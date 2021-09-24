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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex;

import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;

/**
 * @author Joao Bispo
 * 
 */
public abstract class ArrayIndex {

    private static final String INDEX_NAME_PREFIX = "index_";

    private final int position;

    /**
     * @param position
     */
    public ArrayIndex(int position) {
	this.position = position;
    }

    public int getPosition() {
	return position;
    }

    protected String getIndexName() {
	return ArrayIndex.INDEX_NAME_PREFIX + getPosition();
    }

    protected VariableNode getIndexVar() {
	return CNodeFactory.newVariable(getIndexName(), StdIntFactory.newInt32());
    }

    public abstract CNode getFor(List<CNode> forInstructions);

    /**
     * Index to be used to set the matrix
     * 
     * @return a CToken representing a zero-based C index
     */
    public abstract CNode getIndex();

    /**
     * Returns the inputs needed by this index, if to be used inside a function
     * 
     * @return
     */
    public abstract List<CNode> getFunctionInputs();

    /**
     * Creates an ArrayIndex that can be used inside a function.
     * 
     * @return
     */
    public abstract ArrayIndex convertToFunction();

    /**
     * Id that can be used in the function name.
     * 
     * @return
     */
    public abstract String getSmallId();

    /**
     * The number of elements represented by this index.
     * 
     * @return
     */
    public abstract CNode getSize();

    /**
     * Returns the names of the inputs needed by this index, if called from a function
     * 
     * @return
     */
    // public abstract List<String> getFunctionInputNames();

    @Override
    public String toString() {
	return getIndex().getCode();
    }
}
