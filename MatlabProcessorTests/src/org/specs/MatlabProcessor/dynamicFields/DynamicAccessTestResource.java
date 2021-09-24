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

package org.specs.MatlabProcessor.dynamicFields;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum DynamicAccessTestResource implements ResourceProvider {
    CONSTANT(0, 2),
    EXPRESSION(0, 1),
    GET(1, 2),
    CHAINED_CALL1(1, 2),
    CHAINED_CALL2(0, 1);

    private static final String LOCATION = "dynamicFields/";
    private final String name;
    private final int accessIndex;
    private final int totalStatementChildren;

    private DynamicAccessTestResource(int accessIndex, int totalStatementChildren) {
	this.name = name().toLowerCase();

	this.accessIndex = accessIndex;
	this.totalStatementChildren = totalStatementChildren;
    }

    @Override
    public String getResource() {
	return LOCATION + name + ".m";
    }

    public String getResultResource() {
	return LOCATION + name + ".txt";
    }

    public int getAccessIndex() {
	return accessIndex;
    }

    public int getTotalStatementChildren() {
	return totalStatementChildren;
    }
}
