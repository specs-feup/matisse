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

package org.specs.matisselib.tests.typeinference;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum TypeInferenceResources implements ResourceProvider {
    SIMPLE("simple.m"),
    IMPLICIT("implicit.m"),
    CACHE_M("cache.m"),
    CACHE_LOG("cache.log"),
    FOR_LOOP("for_loop.m"),
    BRANCH("branch.m"),
    WHILE_LOOP("while_loop.m"),
    MATRIX_SCALAR_GET("scalar_get.m"),
    MATRIX_SCALAR_SET("scalar_set.m"),
    DEAD_BRANCH("dead_branch.m"),
    MULTIPLE_INSTANTIATION("multiple_instantiation.m"),
    MULTIPLE_INSTANTIATION_RESULT("multiple_instantiation.txt"),
    BIG_INT("big_int.m"),
    DOUBLE_IF("double_if.m"),
    NESTED_IF("nested_if.m");

    private static final String PATH = "typeinference";
    private final String name;

    private TypeInferenceResources(String name) {
	this.name = name;
    }

    @Override
    public String getResource() {
	return TypeInferenceResources.PATH + "/" + this.name;
    }
}
