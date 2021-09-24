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

package org.specs.matlabtocl.v2.types.kernel;

import org.specs.CIR.Types.ATypes.Scalar.AScalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public class CLNativeScalar extends AScalar {

    private final CLNativeType baseType;

    CLNativeScalar(CLNativeType baseType) {
	super(baseType);

	this.baseType = baseType;
    }

    @Override
    public ScalarType setLiteral(boolean isLiteral) {
	// TODO: Implement literals
	return this.baseType;
    }

    @Override
    protected ScalarType setLiteralPrivate(boolean isLiteral) {
	// TODO
	return this.baseType;
    }

    @Override
    protected ScalarType setConstantPrivate(String constant) {
	return this.baseType.setConstant(constant);
    }

    @Override
    public ScalarType removeConstant() {
	return this.baseType.setConstant((Number) null);
    }

    @Override
    public Number getConstant() {
	return baseType.getConstant();
    }

    @Override
    public boolean hasConstant() {
	return getConstant() != null;
    }

    @Override
    public String getConstantString() {
	Number constant = getConstant();

	return constant == null ? null : constant.toString();
    }

    @Override
    public boolean isInteger() {
	return this.baseType.isInteger();
    }

    @Override
    public boolean isUnsigned() {
	return this.baseType.isUnsigned();
    }

}
