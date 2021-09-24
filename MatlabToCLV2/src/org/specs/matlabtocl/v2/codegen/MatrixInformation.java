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

package org.specs.matlabtocl.v2.codegen;

public final class MatrixInformation {
    private boolean needsNumel;
    private boolean needsFullShape;
    private int needsAtLeastShape;

    public MatrixInformation() {
    }

    public void requireNumel() {
	this.needsNumel = true;
    }

    public void requireFullShape() {
	this.needsFullShape = true;
    }

    public void requireAtLeastShape(int numDims) {
	this.needsAtLeastShape = Math.max(this.needsAtLeastShape, numDims);
    }

    public boolean needsNumel() {
	return this.needsNumel;
    }

    public boolean needsFullShape() {
	return this.needsFullShape;
    }

    public int needsAtLeastShape() {
	return this.needsAtLeastShape;
    }
}
