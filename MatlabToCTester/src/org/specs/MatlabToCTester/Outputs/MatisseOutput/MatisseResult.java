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

package org.specs.MatlabToCTester.Outputs.MatisseOutput;

import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToCTester.Test.ArrayResult;

public class MatisseResult implements ArrayResult {

    private final List<Integer> dimensions;
    private final List<Number> values;
    private final String name;
    private final VariableType variableType;

    public MatisseResult(List<Integer> dimensions, List<Number> values, String name, VariableType variableType) {
	this.dimensions = dimensions;
	this.values = values;
	this.name = name;
	this.variableType = variableType;
    }

    @Override
    public List<Integer> getDimensions() {
	return dimensions;
    }

    public List<Number> getValues() {
	return values;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public double getDouble(int index) {
	return values.get(index).doubleValue();
    }

    public VariableType getVariableType() {
	return variableType;
    }

}
