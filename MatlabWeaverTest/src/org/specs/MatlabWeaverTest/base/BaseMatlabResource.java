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

package org.specs.MatlabWeaverTest.base;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum BaseMatlabResource implements ResourceProvider {
    TEST_CASE_1,
    CLOSURE;

    private static final String BASE_FOLDER = "matlab/base/";

    private final String lara;

    private BaseMatlabResource() {
	this.lara = BASE_FOLDER + name().toLowerCase() + ".m";
    }

    private BaseMatlabResource(String lara) {
	this.lara = BASE_FOLDER + lara;
    }

    @Override
    public String getResource() {
	return lara;
    }

}