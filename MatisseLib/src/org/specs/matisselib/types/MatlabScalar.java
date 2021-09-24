/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.types;

import org.specs.CIR.Types.ATypes.Scalar.AScalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public class MatlabScalar extends AScalar {

    private final MatlabElementType type;

    public MatlabScalar(MatlabElementType type) {
	super(type);

	this.type = type;
    }

    @Override
    public ScalarType removeConstant() {
	return this.type.setConstantString(null);
    }

    @Override
    public boolean hasConstant() {
	return this.type.getConstantString() != null;
    }

    @Override
    public String getConstantString() {
	return this.type.getConstantString();
    }

    @Override
    protected ScalarType setLiteralPrivate(boolean isLiteral) {
	return this.type;
    }

    @Override
    protected ScalarType setConstantPrivate(String constant) {
	return this.type.setConstantString(constant);
    }

    @Override
    public boolean isInteger() {
	return this.type.isInteger();
    }

}
