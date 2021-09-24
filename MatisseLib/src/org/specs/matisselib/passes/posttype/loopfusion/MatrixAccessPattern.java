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

import java.util.List;

import com.google.common.base.Preconditions;

public class MatrixAccessPattern {
    private final String sourceMatrix;
    private final String builtMatrix;
    private final String setMatrix;
    private List<MatrixIndex> indices;

    public MatrixAccessPattern(String sourceMatrix, String builtMatrix, String setMatrix) {
	this.sourceMatrix = sourceMatrix;
	this.builtMatrix = builtMatrix;
	this.setMatrix = setMatrix;
    }

    public String getSourceMatrix() {
	return this.sourceMatrix;
    }

    public String getBuiltMatrix() {
	return this.builtMatrix;
    }

    public String getSetMatrix() {
	return this.setMatrix;
    }

    public boolean hasIndices() {
	return this.indices != null;
    }

    public int numIndices() {
	Preconditions.checkState(this.indices != null);

	return this.indices.size();
    }

    public void setIndices(List<MatrixIndex> indices) {
	Preconditions.checkState(this.indices == null);

	this.indices = indices;
    }

    public void setIndices(MatrixAccessPattern other) {
	Preconditions.checkArgument(other != null);
	Preconditions.checkArgument(other.indices != null);
	Preconditions.checkState(this.indices == null);

	setIndices(other.indices);
    }

    public MatrixIndex getIndexAt(int i) {
	Preconditions.checkState(this.indices != null);

	return this.indices.get(i);
    }

    @Override
    public String toString() {
	return "[MatrixAccessPattern " + this.sourceMatrix + ", " + this.builtMatrix + ", " + this.indices + "]";
    }
}
