/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabProcessor.outputs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum OutputsTestResource implements ResourceProvider {

    SINGLE1("x"),
    SINGLE2("x"),
    SINGLE3("x"),
    SINGLE4("x"),
    SINGLE5("x"),
    TWO1("x", "y"),
    TWO2("x", "y"),
    UNUSED1("~"),
    UNUSED2("~", "~"),
    MIX1("~", "b"),
    MIX2("a", "~"),
    FIELD_ACCESS("a.b"),
    CELL_ACCESS("a{1}");

    private static final String LOCATION = "outputs/";
    private final String name;
    private final String[] expected;

    private OutputsTestResource(String... expected) {
	this.name = name().toLowerCase();
	this.expected = expected;
    }

    @Override
    public String getResource() {
	return LOCATION + name + ".m";
    }

    public List<String> getExpectedOutputs() {
	return Collections.unmodifiableList(Arrays.asList(expected));
    }
}
