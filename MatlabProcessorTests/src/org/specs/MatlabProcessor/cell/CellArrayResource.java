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

package org.specs.MatlabProcessor.cell;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum CellArrayResource implements ResourceProvider {
    SIMPLE_CELL,

    MULTICOL1,
    MULTICOL2,

    MULTIROW1,
    MULTIROW2,

    NESTED,
    SPACE,

    EMPTY;

    private static final String LOCATION = "cell/";
    private final String name;

    private CellArrayResource() {
	this.name = name().toLowerCase();
    }

    @Override
    public String getResource() {
	return LOCATION + name + ".m";
    }

    public String getResultResource() {
	return LOCATION + name + ".txt";
    }
}
