/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.passes.posttype.loopfusion;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixIndex {
    public static enum IndexType {
	ITER,
	VARIABLE
    }

    private IndexType type;
    private int depth;
    private String var;

    private MatrixIndex() {
    }

    public static MatrixIndex newIter(int depth) {
	MatrixIndex idx = new MatrixIndex();
	idx.type = IndexType.ITER;
	idx.depth = depth;

	return idx;
    }

    public static MatrixIndex newVar(String index) {
	MatrixIndex idx = new MatrixIndex();
	idx.type = IndexType.VARIABLE;
	idx.var = index;

	return idx;
    }

    public IndexType getType() {
	return this.type;
    }

    public int getDepth() {
	Preconditions.checkState(this.type == IndexType.ITER);

	return this.depth;
    }

    public String getVar() {
	Preconditions.checkState(this.type == IndexType.VARIABLE);

	return this.var;
    }

    @Override
    public String toString() {
	switch (this.type) {
	case ITER:
	    return "[ITER " + this.depth + "]";
	case VARIABLE:
	    return "[VAR " + this.var + "]";
	default:
	    throw new NotImplementedException(this.type.toString());
	}
    }
}
