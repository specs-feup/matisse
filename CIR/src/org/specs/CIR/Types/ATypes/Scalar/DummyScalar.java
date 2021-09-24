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

package org.specs.CIR.Types.ATypes.Scalar;

import org.specs.CIR.Types.VariableType;

public final class DummyScalar extends ScalarType {

    @Override
    public Scalar scalar() {
	return new AScalar(null) {

	    @Override
	    // protected VariableType setConstantPrivate(String constant) {
	    protected ScalarType setConstantPrivate(String constant) {
		return null;
	    }

	    @Override
	    // protected VariableType removeConstant() {
	    public ScalarType removeConstant() {
		return null;
	    }

	    @Override
	    public String getCodeNumber(String number) {
		return number;
	    }

	    @Override
	    protected ScalarType setLiteralPrivate(boolean isLiteral) {
		return null;
	    }

	};
    }

    @Override
    public boolean strictEquals(VariableType type) {
	return type instanceof DummyScalar;
    }

}
