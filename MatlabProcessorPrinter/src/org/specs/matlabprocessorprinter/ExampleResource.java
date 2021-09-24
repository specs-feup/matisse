/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabprocessorprinter;

public enum ExampleResource {
    SIMPLE("simple.m", "SmartDefineTypes.lara"),
    PARALLEL_SECTIONS("parallel_sections.m", "SmartDefineTypes.lara"),
    COOPERATIVE_TEST("cooperative_test.m", "SmartDefineTypes.lara");

    private static final String PATH = "examples/";

    private final String matlab;
    private final String lara;

    ExampleResource(String matlab, String lara) {
        this.matlab = matlab;
        this.lara = lara;
    }

    public String getName() {
        return matlab;
    }

    public String getMatlabPath() {
        return PATH + matlab;
    }

    public String getLaraPath() {
        return PATH + lara;
    }
}
