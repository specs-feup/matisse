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

package org.specs.matisselib.ssa;

import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;

import com.google.common.base.Preconditions;

public class NumberInput implements Input {
    private final boolean negative;
    private final MatlabNumber input;

    public NumberInput(boolean negative, MatlabNumber input) {
	Preconditions.checkArgument(input != null);

	this.negative = negative;
	this.input = input;
    }

    public NumberInput(int input) {
	boolean negative = input < 0;

	this.input = MatlabNumber.getMatlabNumber(Integer.toString(negative ? -input : input));
	this.negative = negative;
    }

    public double getNumber() {
	return input.getFloatValue() * (negative ? -1 : 1);
    }

    public String getNumericString() {
	return (negative ? "-" : "") + input.toMatlabString();
    }

    @Override
    public String toString() {
	return getNumericString();
    }
}
