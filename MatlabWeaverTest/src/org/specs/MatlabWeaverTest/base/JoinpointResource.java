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

import java.util.Arrays;
import java.util.List;

import org.specs.MatlabWeaverTest.Resources.MatlabResource;
import org.specs.MatlabWeaverTest.utils.MWeaverResource;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum JoinpointResource implements MWeaverResource {
    FILE(BaseMatlabResource.TEST_CASE_1),
    FUNCTION(BaseMatlabResource.TEST_CASE_1),
    SECTION(BaseMatlabResource.TEST_CASE_1),
    VAR(BaseMatlabResource.TEST_CASE_1),
    CALL(MatlabResource.CLOSURE);

    private static final String BASE_FOLDER = "lara/base/";

    private final String lara;
    private final String result;
    private final List<ResourceProvider> matlab;

    private JoinpointResource(ResourceProvider matlab) {
	this.lara = BASE_FOLDER + name().toLowerCase() + ".lara";
	this.result = BASE_FOLDER + name().toLowerCase() + ".result";

	this.matlab = Arrays.asList(matlab);
    }

    @Override
    public ResourceProvider getLara() {
	return () -> lara;
    }

    @Override
    public List<ResourceProvider> getMatlab() {
	return matlab;
    }

    @Override
    public ResourceProvider getResult() {
	return () -> result;
    }

}
