/**
 * Copyright 2015 SPeCS.
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

package org.specs.matlabtocl.v2.ssa;

import com.google.common.base.Preconditions;

public final class ParallelRegionId {
    private final String baseName;
    private final int numericPart;

    public ParallelRegionId(String baseName, int numericPart) {
	Preconditions.checkArgument(baseName != null);
	Preconditions.checkArgument(numericPart != 0);

	this.baseName = baseName;
	this.numericPart = numericPart;
    }

    @Override
    public String toString() {
	return this.baseName + "_" + this.numericPart;
    }

    @Override
    public boolean equals(Object other) {
	return other instanceof ParallelRegionId && equals((ParallelRegionId) other);
    }

    public boolean equals(ParallelRegionId other) {
	return other != null &&
		other.baseName.equals(this.baseName) &&
		this.numericPart == other.numericPart;
    }

    @Override
    public int hashCode() {
	return this.baseName.hashCode() ^ this.numericPart;
    }
}
