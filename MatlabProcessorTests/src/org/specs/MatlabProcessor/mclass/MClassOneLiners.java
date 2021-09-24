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

package org.specs.MatlabProcessor.mclass;

public enum MClassOneLiners {
    TEST1("classdef class_name\nend"),
    TEST3("classdef (attribute1 = value, attribute2 = value2) class_name\nend"),
    TEST2("classdef (attribute1 = value) class_name\nend"),
    TEST4("classdef class_name < superclass_name\nend"),
    TEST5("classdef (ConstructOnLoad) Employee < handle\nend"),
    TEST6("classdef (InferiorClasses = {?class1, ?class2}) myClass\nend"),
    TEST7("classdef (InferiorClasses = {?gwa.BaseUnits}) Units\nend");

    private final String input;
    private final String expected;

    private MClassOneLiners(String input, String expected) {
	this.input = input;
	this.expected = expected;
    }

    private MClassOneLiners(String input) {
	this.input = input;
	this.expected = input;
    }

    public String getExpected() {
	return expected;
    }

    public String getInput() {
	return input;
    }
}
