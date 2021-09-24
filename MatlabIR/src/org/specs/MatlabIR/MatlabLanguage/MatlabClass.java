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

package org.specs.MatlabIR.MatlabLanguage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum MatlabClass {

    AN_OBJECT(),
    FUNCTION_HANDLE(AN_OBJECT),
    STRUCT(FUNCTION_HANDLE),
    CELL(FUNCTION_HANDLE),
    UINT32(STRUCT, CELL),
    UINT64(STRUCT, CELL),
    INT8(STRUCT, CELL),
    INT16(STRUCT, CELL),
    INT32(STRUCT, CELL),
    INT64(STRUCT, CELL),
    UINT8(STRUCT, CELL),
    UINT16(STRUCT, CELL),
    SINGLE(UINT32, UINT64, INT8, INT16, INT32, INT64, UINT8, UINT16),
    DOUBLE(SINGLE),
    CHAR(SINGLE),
    LOGICAL(DOUBLE, CHAR);

    private static final EnumSet<MatlabClass> INTEGER_CLASSES = EnumSet.of(UINT16, UINT32, UINT64, UINT8, INT16, INT32,
	    INT64, INT8);
    private static final EnumSet<MatlabClass> UNSIGNED_CLASSES = EnumSet.of(UINT16, UINT32, UINT64, UINT8);

    private final Set<Integer> superiorClasses;

    MatlabClass(MatlabClass... superiorClasses) {
	this(Arrays.asList(superiorClasses));
    }

    MatlabClass(Collection<MatlabClass> superiorClasses) {
	if (superiorClasses.isEmpty()) {
	    this.superiorClasses = Collections.emptySet();
	} else {
	    this.superiorClasses = superiorClasses.stream()
		    .map(mclass -> mclass.ordinal())
		    .collect(Collectors.toSet());
	}

    }

    public boolean isInteger() {
	return INTEGER_CLASSES.contains(this);
    }

    public boolean isUnsigned() {
	return UNSIGNED_CLASSES.contains(this);
    }

    public Set<Integer> getSuperiorClasses() {
	return superiorClasses;
    }
}
