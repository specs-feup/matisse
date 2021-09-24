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

package org.specs.MatlabProcessor.oneliners;

public enum OneLinersError {
    ERROR1("a(3, 4))"),
    ERROR2("if (c=0)"),
    ERROR3("\u001a"),
    ERROR4("error('Model type should be ''glm'');"),
    ERROR5("SCALE, actualN, actualM, graphSparsity, C)"),
    ERROR7("0 1"),
    ERROR8("classdef \nproperties (Dependent)"),
    ERROR9("xs = reshape( mesh.nodes(mesh.tricellvxs(:,1:3)',1),"),
    ERROR10("fprintf('***ERPLAB polydetrend Warning***  for %s', EEG.)"),
    ERROR11("xi=xi. -dx;");

    private final String input;

    private OneLinersError(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
