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

package org.specs.MatlabProcessor.matrix;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum MatrixResource implements ResourceProvider {
    SIMPLE_MATRIX,
    NESTED_MATRIX,

    MULTICOLS1,
    MULTICOLS2,
    MULTICOLS3,
    MULTICOLS4,
    MULTIROWS1,
    MULTIROWS2,
    MULTIROWS3,
    MULTIROWS4,

    LEADING_SEMICOLON,
    TRAILING_SEMICOLON,

    EMPTY1,
    EMPTY2,
    EMPTY3,
    EMPTY4,

    TABS1,

    EXTRA_COMMA1,
    EXTRA_COMMA2,

    LINEBREAK,
    STRING_LINEBREAK;

    private static final String LOCATION = "matrix/";
    private final String name;

    private MatrixResource() {
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
